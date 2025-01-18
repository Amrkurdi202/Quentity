package com.quentity.controllers;


import com.quentity.entity.Entity;
import com.quentity.services.GenericService;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.Field;
import java.util.*;

@RestController
@RequestMapping("/api/{entityName}")
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

    @GetMapping("/")
    public ResponseEntity<?> queryEntities(
            @PathVariable String entityName,
            @RequestParam Map<String, String> filters,
            @RequestParam(defaultValue = "AND") String logic,
            @RequestParam(defaultValue = "0") int start,
            @RequestParam(defaultValue = "10") int pageSize
    ) {
        Map<String, Object> response = new LinkedHashMap<>();
        try {
            Class<?> entityClass = getEntityClass(entityName);
            List<?> results = genericService.getEntitiesWithDynamicQuery(entityClass, filters, logic, start, pageSize);
            response.put("status", "success");
            response.put("message", "Entities fetched successfully");
            response.put("result", results);
            response.put("count", results.size());
            return ResponseEntity.ok(response);
        } catch (ClassNotFoundException e) {
            response.put("status", "error");
            response.put("message", "Entity type not found: " + entityName);
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", e.getCause().getMessage());
            return ResponseEntity.status(500).body("An error occurred: " + e.getMessage());
        }
    }


    @PostMapping("/")
    @PutMapping("/")
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

    @DeleteMapping("/")
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

    private static Class<?> getEntityClass(String entityName) throws ClassNotFoundException {
        Class<?> clazz;
        try {
            clazz = entitiesClasses.get(entityName.toLowerCase().trim());
            if (clazz == null) {
                throw new ClassNotFoundException("Class not found: " + entityName);
            }
        } catch (Exception e) {
            throw new ClassNotFoundException("Class not found: " + entityName);
        }
        return clazz;
    }
}
