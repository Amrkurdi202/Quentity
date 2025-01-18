package com.quentity.entity.annotions.handler;

import com.quentity.entity.Entity;
import com.quentity.entity.annotions.Icon;
import com.vaadin.flow.component.icon.AbstractIcon;
import com.vaadin.flow.component.icon.SvgIcon;
import com.vaadin.flow.server.StreamResource;

import java.io.IOException;
import java.io.InputStream;

public class IconHandler {

    private static final String DOC_SVG = "doc.svg";

    private static final String DEFAULT_PATH = "META-INF/resources/icons/";

    private static AbstractIcon getIcon(String name) {
        String fileName = getName(name);
        StreamResource iconResource;
        try {
            iconResource = new StreamResource(fileName,
                    () -> IconHandler.class.getClassLoader().getResourceAsStream(DEFAULT_PATH + fileName));
        } catch (Exception e) {
            throw new IllegalArgumentException("Could not find icon " + fileName);
        }
        return new SvgIcon(iconResource);
    }

    private static String getName(String name) {
        int indexOfFileExt = name.lastIndexOf(".");
        if (indexOfFileExt == -1) return name + ".svg";
        else if (name.substring(indexOfFileExt + 1).equals("svg")) return name;
        else throw new IllegalArgumentException("Invalid file extension it should only be svg");
    }

    private static AbstractIcon getIcon(Icon icon) {
        return getIcon(icon.value());
    }

    public static AbstractIcon getIcon(Class<? extends Entity> entityClass) {
        Icon icon = entityClass.getAnnotation(Icon.class);
        if (icon == null) return getIcon(DOC_SVG);
        else return getIcon(icon.value());
    }
}
