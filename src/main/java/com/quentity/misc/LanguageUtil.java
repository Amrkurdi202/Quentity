package com.quentity.misc;

import com.vaadin.flow.component.Direction;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.server.VaadinSession;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class LanguageUtil {
    private static final String SESSION_LANG_KEY = "currentLang";

    // Default language
    private static final String DEFAULT_LANG = "en";

    public static final Map<String, Properties> LANGUAGES = loadAllLanguages("strings");
    public static final String QUENTITY_LANGUAGE_DIRECTION = "Quentity.language.direction";

    public static Map<String, Properties> loadAllLanguages(String directoryPath) {
        HashMap<String, Properties> langs = new HashMap<>();
        // Get the directory path inside resources
        Path stringsDir = null;
        try {
            stringsDir = Paths.get(LanguageUtil.class.getClassLoader().getResource(directoryPath).toURI());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }

        // Filter all .properties files and load them
        try {
            Files.walk(stringsDir)
                    .filter(Files::isRegularFile)
                    .filter(file -> file.toString().endsWith(".properties"))
                    .forEach(file -> {
                        String fileName = file.getFileName().toString();

                        boolean directionIncluded = fileName.contains("_");

                        String languageCode = fileName.
                                substring(0, directionIncluded
                                        ? fileName.indexOf('_') :
                                        fileName.indexOf('.')).
                                trim(); // Extract language code (e.g., en, fr)

                        String direction = directionIncluded ?
                                (fileName.
                                        substring(fileName.indexOf('_') + 1, fileName.indexOf('.')).
                                        trim()) :
                                "ltr";// Extract direction (e.g., rtl, ltr)

                        try (InputStream inputStream = Files.newInputStream(file)) {
                            try (InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8)) {
                                Properties properties = new Properties();
                                properties.load(reader);
                                if (!properties.contains(QUENTITY_LANGUAGE_DIRECTION))
                                    properties.put(QUENTITY_LANGUAGE_DIRECTION, direction);

                                langs.put(languageCode, properties);
                            }
                        } catch (IOException e) {
                            throw new RuntimeException("Error loading properties file: " + file, e);
                        }
                    });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return langs;
    }

    // Set the current language in the session
    public static void setCurrentLanguage(String lang) {
        VaadinSession.getCurrent().setAttribute(SESSION_LANG_KEY, lang);
        UI.getCurrent().setDirection(LanguageUtil.
                get(LanguageUtil.QUENTITY_LANGUAGE_DIRECTION).equals("rtl") ?
                Direction.RIGHT_TO_LEFT : Direction.LEFT_TO_RIGHT);
    }

    // Get the current language from the session
    public static String getCurrentLanguage() {
        VaadinSession current = VaadinSession.getCurrent();
        String lang = current != null ? ((String) current.getAttribute(SESSION_LANG_KEY)) : null;
        return lang != null ? lang : DEFAULT_LANG;
    }

    // Get the current language properties
    public static Properties getCurrentLanguageProperties() {
        String lang = getCurrentLanguage();
        return LANGUAGES.getOrDefault(lang, LANGUAGES.get(DEFAULT_LANG));
    }

    public static String get(String key) {
        return getCurrentLanguageProperties().getProperty(key);
    }

}
