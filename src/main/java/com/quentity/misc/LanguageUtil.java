package com.quentity.misc;

import com.vaadin.flow.component.Direction;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.server.VaadinSession;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
public class LanguageUtil {
    private static final String SESSION_LANG_KEY = "currentLang";
    private static final String DEFAULT_LANG = "en";
    public static final String QUENTITY_LANGUAGE_DIRECTION = "Quentity.language.direction";
    private static final ExecutorService WATCHER_SERVICE = Executors.newSingleThreadExecutor();
    public static Map<String, Properties> LANGUAGES = loadAllLanguages("strings");

    static {
        startWatchingForChanges("strings");
    }

    public static Map<String, Properties> loadAllLanguages(String directoryPath) {
        HashMap<String, Properties> langs = new HashMap<>();
        Path stringsDir;
        try {
            stringsDir = Paths.get(LanguageUtil.class.getClassLoader().getResource(directoryPath).toURI());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }

        try {
            Files.walk(stringsDir)
                    .filter(Files::isRegularFile)
                    .filter(file -> file.toString().endsWith(".properties"))
                    .forEach(file -> {
                        String fileName = file.getFileName().toString();
                        boolean directionIncluded = fileName.contains("_");
                        String languageCode = fileName.substring(0, directionIncluded
                                ? fileName.indexOf('_')
                                : fileName.indexOf('.')).trim();
                        String direction = directionIncluded
                                ? fileName.substring(fileName.indexOf('_') + 1, fileName.indexOf('.')).trim()
                                : "ltr";

                        try (InputStream inputStream = Files.newInputStream(file);
                             InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8)) {
                            Properties properties = new Properties();
                            properties.load(reader);
                            if (!properties.contains(QUENTITY_LANGUAGE_DIRECTION)) {
                                properties.put(QUENTITY_LANGUAGE_DIRECTION, direction);
                            }
                            langs.put(languageCode, properties);
                        } catch (IOException e) {
                            throw new RuntimeException("Error loading properties file: " + file, e);
                        }
                    });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return langs;
    }

    private static void startWatchingForChanges(String directoryPath) {
        WATCHER_SERVICE.submit(() -> {
            Path stringsDir;
            try {
                stringsDir = Paths.get(LanguageUtil.class.getClassLoader().getResource(directoryPath).toURI());
            } catch (URISyntaxException e) {
                throw new RuntimeException("Invalid directory path: " + directoryPath, e);
            }

            try (WatchService watchService = FileSystems.getDefault().newWatchService()) {
                stringsDir.register(watchService, StandardWatchEventKinds.ENTRY_MODIFY);

                while (true) {
                    WatchKey key = watchService.take(); // Wait for an event
                    for (WatchEvent<?> event : key.pollEvents()) {
                        if (event.kind() == StandardWatchEventKinds.ENTRY_MODIFY) {
                            String fileName = event.context().toString();
                            if (fileName.endsWith(".properties")) {
                                synchronized (LANGUAGES) {
                                    LANGUAGES = loadAllLanguages(directoryPath);
                                }
                                LanguageUtil.log.info("Language files updated. Map refreshed.");
                            }
                        }
                    }
                    key.reset(); // Reset the key to receive further events
                }
            } catch (IOException | InterruptedException e) {
                throw new RuntimeException("Error watching directory: " + directoryPath, e);
            }
        });
    }

    public static void setCurrentLanguage(String lang) {
        VaadinSession.getCurrent().setAttribute(SESSION_LANG_KEY, lang);
        UI.getCurrent().setDirection(
                LanguageUtil.get(LanguageUtil.QUENTITY_LANGUAGE_DIRECTION).equals("rtl")
                        ? Direction.RIGHT_TO_LEFT
                        : Direction.LEFT_TO_RIGHT
        );
    }

    public static String getCurrentLanguage() {
        VaadinSession current = VaadinSession.getCurrent();
        String lang = current != null ? (String) current.getAttribute(SESSION_LANG_KEY) : null;
        return lang != null ? lang : DEFAULT_LANG;
    }

    public static Properties getCurrentLanguageProperties() {
        String lang = getCurrentLanguage();
        synchronized (LANGUAGES) {
            return LANGUAGES.getOrDefault(lang, LANGUAGES.get(DEFAULT_LANG));
        }
    }

    public static String get(String key) {
        return getCurrentLanguageProperties().getProperty(key);
    }
}
