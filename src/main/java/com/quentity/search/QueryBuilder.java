package com.quentity.search;


import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.metamodel.EntityType;
import org.hibernate.SessionFactory;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.mapping.PersistentClass;
import org.hibernate.metamodel.spi.MetamodelImplementor;
import org.hibernate.persister.entity.EntityPersister;
import org.hibernate.persister.entity.UnionSubclassEntityPersister;
import org.springframework.stereotype.Service;

import java.util.Map;

import static com.quentity.entity.EntityService.getColumn;

@Service
public class QueryBuilder {
    @PersistenceContext
    private EntityManager entityManager;

    public String buildQueryWithFilters(
            Class<?> entityClass,
            Map<String, String> filters,
            String logic
    ) {
        StringBuilder query = new StringBuilder("SELECT * FROM ");

        String tableName = getTableName(entityClass);
        query.append(tableName);

        if (!logic.equalsIgnoreCase("AND") && !logic.equalsIgnoreCase("OR")) {
            throw new IllegalArgumentException("Invalid logic type: " + logic + ". Use 'AND' or 'OR'.");
        }

        if (!filters.isEmpty()) {
            query.append(" WHERE ");
            filters.forEach((fieldWithOperation, value) -> {
                String[] parts = parseFieldWithOperation(fieldWithOperation);
                if (parts.length != 2 || parts[1].isEmpty()) {
                    throw new IllegalArgumentException("Invalid field or operation: " + fieldWithOperation);
                }
                String fieldName = parts[0];
                String operator = parts[1];

                query.append(validateFieldName(getDatabaseColumnName(fieldName, entityClass)))
                        .append(" ")
                        .append(operator)
                        .append(" ?")
                        .append(" ")
                        .append(logic.toUpperCase())
                        .append(" ");
            });

            query.setLength(query.length() - (logic.length() + 1));
        }


        return query.toString();
    }

    public String getDatabaseColumnName(String fieldName, Class<?> entityClass) {
        return getColumn(fieldName, entityClass, entityManager);
    }


    private String[] parseFieldWithOperation(String fieldWithOperation) {

        String regex = "^(\\w+)([><=!]+)$";
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(regex);
        java.util.regex.Matcher matcher = pattern.matcher(fieldWithOperation);

        if (matcher.find()) {
            String fieldName = matcher.group(1);
            String operator = matcher.group(2);
            return new String[]{fieldName, operator};
        }

        throw new IllegalArgumentException("Invalid field or operation: " + fieldWithOperation);
    }


    private String validateFieldName(String fieldName) {
        if (!fieldName.matches("^[a-zA-Z_][a-zA-Z0-9_]*$")) {
            throw new IllegalArgumentException("Invalid field name: " + fieldName);
        }
        return fieldName;
    }

    public String getTableName(Class<?> entityClass) {
        EntityType<?> entityType = entityManager.getMetamodel().entity(entityClass);
        if (entityType != null) {
            return entityType.getName();
        }
        throw new IllegalArgumentException("Unable to resolve table name for entity: " + entityClass.getName());
    }


}
