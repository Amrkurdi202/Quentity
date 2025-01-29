package com.quentity.misc;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.page.Page;
import com.vaadin.flow.component.page.WebStorage;
import com.vaadin.flow.dom.ThemeList;
import com.vaadin.flow.theme.lumo.Lumo;

public class ThemeController {
    public static final String THEME = "theme";

    public static void setDarkTheme(Component component) {
        ThemeList themeList = component.getElement().getThemeList();
        themeList.clear();
        themeList.add(Lumo.DARK);
        WebStorage.setItem(THEME, Lumo.DARK);
    }

    public static void setLightTheme(Component component) {
        ThemeList themeList = component.getElement().getThemeList();
        themeList.clear();
        themeList.add(Lumo.LIGHT);
        WebStorage.setItem(THEME, Lumo.LIGHT);
    }

    public static boolean isLightTheme(Component component) {
        ThemeList themeList = component.getElement().getThemeList();
        return themeList.isEmpty() || themeList.contains(Lumo.LIGHT);
    }

    public static void syncTheme(Component component) {
        // Fetch theme synchronously from localStorage
        Page page = UI.getCurrent().getPage();
        String theme = page.executeJs("return localStorage.getItem($0)", THEME).toString();

        if (Lumo.LIGHT.equals(theme)) {
            setLightTheme(component);
        } else {
            setDarkTheme(component);
        }
    }
}
