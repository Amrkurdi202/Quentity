package com.quentity.views;

import com.quentity.data.User;
import com.quentity.misc.LanguageUtil;
import com.quentity.security.AuthenticatedUser;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.contextmenu.ContextMenu;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import java.util.Map;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicReference;

public class LanguageSelectorWidget extends VerticalLayout {

    public LanguageSelectorWidget(AuthenticatedUser authenticatedUser) {
        AtomicReference<User> user = new AtomicReference<>(authenticatedUser.get().orElse(null));
        String currentLanguage = "en";
        if (user.get() != null)
            currentLanguage = user.get().getLang();

        Button languageButton = new Button(currentLanguage);
        languageButton.getStyle()
                .set("background", "transparent")
                .set("color", "black")
                .set("width", "50px")
                .set("height", "50px")
                .set("border-radius", "5px");

        ContextMenu contextMenu = new ContextMenu(languageButton);
        contextMenu.setOpenOnClick(true);

        Map<String, Properties> languages = LanguageUtil.LANGUAGES;
        languages.keySet().forEach(locale ->
                contextMenu.addItem(locale, event -> {
                    languageButton.setText(locale);
                    if (user.get() != null) {
                        user.get().setLang(locale);
                        user.set(authenticatedUser.update(user.get()));
                        LanguageUtil.setCurrentLanguage(locale);
                    }
                    UI.getCurrent().getPage().reload();
                }));


        Div container = new Div(languageButton);
        add(container);
    }
}
