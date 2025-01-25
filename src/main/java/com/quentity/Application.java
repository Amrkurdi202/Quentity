package com.quentity;

import com.quentity.data.QUser;
import com.quentity.data.User;
import com.quentity.entity.Entity;
import com.quentity.entity.ServiceFactory;
import com.quentity.misc.EntityManagerProvider;
import com.quentity.reflection.Reflector;
import com.quentity.views.myview.Entities;
import com.quentity.views.myview.QEntities;
import com.quentity.views.myview.QQueries;
import com.quentity.views.myview.Queries;
import com.querydsl.jpa.impl.JPAQuery;
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
import java.util.*;
import java.util.function.Consumer;

/**
 * The entry point of the Spring Boot application.
 * <p>
 * Use the @PWA annotation make the application installable on phones, tablets
 * and some desktop browsers.
 */
@SpringBootApplication
@EnableJpaRepositories(considerNestedRepositories = true)
@Theme(value = "server")
@PWA(name = "Server", shortName = "Server", offlineResources = {"icons/logo.png"})
@Push
public class Application implements AppShellConfigurator {
    public static final String LOCAL = "en";
    public static final HashMap<String, Properties> LOCAL_PROPERTIES = new HashMap<>();
    private static final Reflector REFLECTOR = new Reflector();


    public static void main(String[] args) throws IOException {

        SpringApplication.run(Application.class, args);
    }

    @Bean
    SqlDataSourceScriptDatabaseInitializer dataSourceScriptDatabaseInitializer(DataSource dataSource,
                                                                               SqlInitializationProperties properties) {
        // This bean ensures the database is only initialized when empty
        return new SqlDataSourceScriptDatabaseInitializer(dataSource, properties) {
            @Override
            public boolean initializeDatabase() {
                addEntites();
                addQueries();
                JPAQuery<Object> jpaQuery;

                jpaQuery = new JPAQuery<>(EntityManagerProvider.getEntityManager());
                User user = jpaQuery.select(QUser.user).from(QUser.user).limit(1).fetchOne();
                if (user == null) {
                    return super.initializeDatabase();
                }
                return false;
            }

            private static void addQueries() {
                Set<Class<? extends Entity>> entitySubclasses = Reflector.getEntities();

                for (Class<? extends Entity> entitySubclass : entitySubclasses) {
                    Entity entity = Entity.newEntity(entitySubclass);
                    ServiceFactory.define(entity);
                    Map<String, Consumer<Entity>> queryEditors = entity.getQueryEditors();
                    for (Map.Entry<String, Consumer<Entity>> entry : queryEditors.entrySet()) {
                        QQueries queries = QQueries.queries;
                        JPAQuery<Object> jpaQuery = new JPAQuery<>(EntityManagerProvider.getEntityManager());
                        if (jpaQuery.
                                select(queries).
                                from(queries).
                                where(queries.name.textValue.
                                        eq(entry.getKey())).
                                limit(1).
                                fetchOne() == null) {
                            Queries queriesToAdd = Entity.newEntity(Queries.class);
                            queriesToAdd.
                                    name.
                                    setFieldValue(entry.getKey());
                            queriesToAdd.save();
                        }
                    }
                }
                JPAQuery<Object> jpaQuery = new JPAQuery<>(EntityManagerProvider.getEntityManager());
                List<Entities> entitiesList = jpaQuery.select(QEntities.entities).from(QEntities.entities).limit(entitySubclasses.size()).fetch();
                if (entitiesList.size() != entitySubclasses.size()) {
                    entitySubclasses.stream().filter(e -> !entitiesList.contains(e.getSimpleName()))
                            .forEach(e -> {
                                Entities entities = Entity.newEntity(Entities.class);
                                entities.getName().setFieldValue(e.getSimpleName());
                                entities.save();
                            });
                }
            }

            private static void addEntites() {
                JPAQuery<Object> jpaQuery = new JPAQuery<>(EntityManagerProvider.getEntityManager());
                Set<Class<? extends Entity>> entitySubclasses = Reflector.getEntities();
                List<Entities> entitiesList = jpaQuery.select(QEntities.entities).from(QEntities.entities).limit(entitySubclasses.size()).fetch();
                if (entitiesList.size() != entitySubclasses.size()) {
                    entitySubclasses.stream().filter(e -> !entitiesList.contains(e.getSimpleName()))
                            .forEach(e -> {
                                Entities entities = Entity.newEntity(Entities.class);
                                entities.getName().setFieldValue(e.getSimpleName());
                                entities.setFullName(e.getName());
                                entities.save();
                            });
                }
            }
        };
    }
}
