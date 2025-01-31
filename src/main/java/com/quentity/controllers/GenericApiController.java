package com.quentity.controllers;


import com.quentity.entity.Entity;
import com.quentity.entity.EntityService;
import com.quentity.entity.ServiceFactory;
import com.quentity.refGenPlug.FieldPojo;
import com.quentity.reflection.Reflector;
import com.quentity.services.GenericService;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.*;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@RestController
@RequestMapping("/api/v1/{entityName}")
public class GenericApiController {
    @Autowired
    private GenericService genericService;
    private static final Map<String, Class<? extends Entity>> entitiesClasses;

    static {
        Reflections reflections = new Reflections(new ConfigurationBuilder()
                .setUrls(ClasspathHelper.forClassLoader(ClasspathHelper.contextClassLoader()))
                .setScanners(Scanners.SubTypes.filterResultsBy(c -> true))
        );
        Set<Class<? extends Entity>> classes = reflections.getSubTypesOf(Entity.class);
        entitiesClasses = new HashMap<>();
        for (Class<? extends Entity> clazz : classes) {
            entitiesClasses.put(clazz.getSimpleName().toLowerCase(), clazz);
        }
    }

    @GetMapping()
    public ResponseEntity<?> queryEntities(
            @PathVariable String entityName,
            @RequestParam MultiValueMap<String, String> params,
            Pageable pageable
    ) {
        Map<String, Object> response = new LinkedHashMap<>();
        try {
            Class<? extends Entity> entityClass = getEntityClass(entityName);
            Predicate predicate = buildPredicate(entityClass, params);
            EntityService service = ServiceFactory.getService(entityClass);
            Iterable<?> results = service.findAll(predicate, pageable);
            List<?> collect = StreamSupport.stream(results.spliterator(), false)
                    .collect(Collectors.toList());
            response.put("status", "success");
            response.put("message", "Entities fetched successfully");
            response.put("result", collect);
            response.put("count", collect.size());
            return ResponseEntity.ok(response);
        } catch (ClassNotFoundException e) {
            response.put("status", "error");
            response.put("message", "Entity type not found: " + entityName);
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            response.put("status", "error");
            Throwable cause = e.getCause();
            response.put("message", cause != null ? cause.getMessage() : e.getMessage());
            return ResponseEntity.status(500).body("An error occurred: " + e.getMessage());
        }
    }


    @PostMapping()
    @PutMapping()
    public ResponseEntity<?> addUpdateEntity(
            @PathVariable String entityName,
            @RequestBody List<Map<String, Object>> entities
    ) {
        Map<String, Object> response = new LinkedHashMap<>();
        try {
            Class<?> entityClass = getEntityClass(entityName);
            ArrayList<Entity> savedEntities = genericService.saveEntities(entityClass, entities);
            response.put("status", "success");
            response.put("message", "Entities updated successfully");
            response.put("result", savedEntities);
            response.put("count", savedEntities.size());
            return ResponseEntity.ok(response);
        } catch (ClassNotFoundException e) {
            response.put("status", "error");
            response.put("message", "Entity type not found: " + entityName);
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", e.getCause().getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    @DeleteMapping()
    public ResponseEntity<?> deleteEntities(
            @PathVariable String entityName,
            @RequestBody List<Long> ids
    ) {
        Map<String, Object> response = new LinkedHashMap<>();
        try {
            Class<?> entityClass = getEntityClass(entityName);
            genericService.deleteEntities(entityClass, ids);
            response.put("status", "success");
            response.put("message", "Entities Deleted successfully");
            response.put("result", ids);
            response.put("count", ids.size());
            return ResponseEntity.ok(response);
        } catch (ClassNotFoundException e) {
            response.put("status", "error");
            response.put("message", "Entity type not found: " + entityName);
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", e.getCause().getMessage());
            return ResponseEntity.status(500).body(response);
        }

    }

    @GetMapping("/struct")
    public ResponseEntity<?> getClassStructure(@PathVariable String entityName) {
        Map<String, Object> response = new LinkedHashMap<>();
        try {
            Class<?> clazz = getEntityClass(entityName);
            List<Map<String, String>> fields = new ArrayList<>();
            for (Field field : clazz.getDeclaredFields()) {
                Map<String, String> fieldInfo = new LinkedHashMap<>();
                fieldInfo.put("name", field.getName());
                fieldInfo.put("type", field.getType().getSimpleName());
                fields.add(fieldInfo);
            }
            response.put("status", "success");
            response.put("message", "Entity found successfully");
            response.put("result", fields);
            response.put("count", fields.size());
            return ResponseEntity.ok(response);
        } catch (ClassNotFoundException e) {
            response.put("status", "error");
            response.put("message", e.getCause().getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    private static <E extends Entity> Class<E> getEntityClass(String entityName) throws ClassNotFoundException {
        Class<E> clazz;
        try {
            clazz = (Class<E>) entitiesClasses.get(entityName.toLowerCase().trim());
            if (clazz == null) {
                throw new ClassNotFoundException("Class not found: " + entityName);
            }
        } catch (Exception e) {
            throw new ClassNotFoundException("Class not found: " + entityName);
        }
        return clazz;
    }

    private Predicate buildPredicate(Class<? extends Entity> entityClass, MultiValueMap<String, String> params) {
        String name = entityClass.getSimpleName();
        PathBuilder<?> entityPath = new PathBuilder<>(entityClass, camelCase(name));
        BooleanBuilder predicate = new BooleanBuilder();

        params.forEach((key, values) -> {
            if (!values.isEmpty()) {

                String operation = extractOperation(key);


                String fieldName = key.replaceAll("[<>|]=?|\\.like|\\.contains|\\.startsWith|\\.endsWith", "");

                FieldPojo field = Reflector.getField(entityClass.getName(), fieldName);
                if (field == null) return;

                String inner = fieldInnerTypes.get(field.getType());

                for (String value : values) {
                    if (key.contains("|")) {
                        predicate.or(applyComparisonPredicate(entityPath, fieldName, inner, operation, value));
                    } else {
                        predicate.and(applyComparisonPredicate(entityPath, fieldName, inner, operation, value));
                    }
                }
            }
        });

        return predicate;
    }


    private String camelCase(String input) {
        return input.substring(0, 1).toLowerCase() + input.substring(1);
    }

    private final static Map<String, String> fieldInnerTypes;

    static {
        fieldInnerTypes = new HashMap<>();
        fieldInnerTypes.put("FldString", "textValue");
        fieldInnerTypes.put("FldNumber", "value");
        fieldInnerTypes.put("FldBool", "boolValue");
        fieldInnerTypes.put("FldDate", "dateValue");
        fieldInnerTypes.put("MultiEntitiesReferences", "entities");
        fieldInnerTypes.put("SingleEntityReference", "entity");
    }

    private Predicate applyComparisonPredicate(PathBuilder<?> entityPath, String fieldName, String inner, String key, String value) {
        Predicate predicate;

        if (inner != null) {

            if (key.contains(">") || key.contains("<")) {
                NumberPath<Double> numberPath = entityPath.get(fieldName).getNumber(inner, Double.class);
                predicate = applyNumberComparison(numberPath, key, value);
            } else if (key.contains("like") || key.contains("startsWith") || key.contains("endsWith")) {
                StringPath stringPath = entityPath.get(fieldName).getString(inner);
                predicate = applyStringComparison(stringPath, key, value);
            } else if (key.contains("contains")) {
                SimplePath<Long> entityIdPath = entityPath.get(fieldName).getList(inner, Entity.class).any().getSimple("entityId", Long.class);
                predicate = entityIdPath.in(Long.parseLong(value));
            } else {
                SimpleExpression simplePath = entityPath.get(fieldName, Object.class).get(inner, Object.class);
                predicate = simplePath.eq(value);
            }
        } else {

            if (key.contains(">") || key.contains("<")) {
                NumberPath<Double> numberPath = entityPath.getNumber(fieldName, Double.class);
                predicate = applyNumberComparison(numberPath, key, value);
            } else if (key.contains("like") || key.contains("startsWith") || key.contains("endsWith")) {
                StringPath stringPath = entityPath.getString(fieldName);
                predicate = applyStringComparison(stringPath, key, value);
            } else if (key.contains("contains")) {
                SimplePath<Long> entityIdPath = entityPath.getSimple(fieldName, Long.class);
                predicate = entityIdPath.in(Long.parseLong(value));
            } else {
                SimpleExpression simplePath = entityPath.get(fieldName, Object.class);
                predicate = simplePath.eq(value);
            }
        }

        return predicate;
    }

    private Predicate applyNumberComparison(NumberPath<Double> path, String key, String value) {
        double numericValue = Double.parseDouble(value);
        if (key.contains(">=")) {
            return path.goe(numericValue);
        } else if (key.contains("<=")) {
            return path.loe(numericValue);
        } else if (key.contains(">")) {
            return path.gt(numericValue);
        } else {
            return path.lt(numericValue);
        }
    }

    private Predicate applyStringComparison(StringPath path, String key, String value) {
        if (key.contains("like")) {
            return path.containsIgnoreCase(value);
        } else if (key.contains("startsWith")) {
            return path.startsWithIgnoreCase(value);
        } else {
            return path.endsWithIgnoreCase(value);
        }
    }


    private String extractOperation(String key) {
        if (key.endsWith(">=")) return ">=";
        if (key.endsWith("<=")) return "<=";
        if (key.endsWith(">")) return ">";
        if (key.endsWith("<")) return "<";
        if (key.endsWith(".like")) return "like";
        if (key.endsWith(".contains")) return "contains";
        if (key.endsWith(".startsWith")) return "startsWith";
        if (key.endsWith(".endsWith")) return "endsWith";
        return "=";
    }

}
