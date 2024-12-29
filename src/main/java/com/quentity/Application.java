package com.quentity;

import com.quentity.data.UserRepository;
import com.quentity.reflection.Reflector;
import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.server.PWA;
import com.vaadin.flow.theme.Theme;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.sql.init.SqlDataSourceScriptDatabaseInitializer;
import org.springframework.boot.autoconfigure.sql.init.SqlInitializationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Properties;

/**
 * The entry point of the Spring Boot application.
 *
 * Use the @PWA annotation make the application installable on phones, tablets
 * and some desktop browsers.
 *
 */
@SpringBootApplication
@EnableJpaRepositories( considerNestedRepositories = true )
@Theme(value = "server")
@PWA(name = "Server", shortName = "Server", offlineResources = {"icons/logo.png"})
@Push
public class Application implements AppShellConfigurator {
    public static final String LOCAL = "en";
    public static final HashMap <String, Properties> LOCAL_PROPERTIES = new HashMap<>();
    private static final Reflector REFLECTOR = new Reflector();

    public static Properties loadProperties(String resourcePath) throws IOException {
        Properties properties = new Properties();
        try (InputStream inputStream = Application.class.getClassLoader().getResourceAsStream(resourcePath)) {
            if (inputStream == null) {
                throw new IOException("Resource not found: " + resourcePath);
            }
            properties.load(inputStream);
        }
        return properties;
    }

    public static void main(String[] args) throws IOException {
        Properties enProperties = loadProperties("strings/en.properties");
//        Properties arProperties = loadProperties("strings/ar.properties");
        LOCAL_PROPERTIES.put("en", enProperties);
//        LOCAL_PROPERTIES.put("ar", arProperties);
        SpringApplication.run(Application.class, args);
    }

    @Bean
    SqlDataSourceScriptDatabaseInitializer dataSourceScriptDatabaseInitializer(DataSource dataSource,
            SqlInitializationProperties properties, UserRepository repository) {
        // This bean ensures the database is only initialized when empty
        return new SqlDataSourceScriptDatabaseInitializer(dataSource, properties) {
            @Override
            public boolean initializeDatabase() {
                if (repository.count() == 0L) {
                    return super.initializeDatabase();
                }
                return false;
            }
        };
    }
}
