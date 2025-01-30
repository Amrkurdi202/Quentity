package com.quentity.misc;

import com.quentity.project.adminstrator.User;
import com.quentity.security.AuthenticatedUser;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.contextmenu.ContextMenu;

import java.util.Map;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicReference;

public class LanguageSelectorWidget extends Button {

    public LanguageSelectorWidget(AuthenticatedUser authenticatedUser) {
        super();
        AtomicReference<User> user = new AtomicReference<>(authenticatedUser.get().orElse(null));
        String currentLanguage = "en";
        if (user.get() != null)
            currentLanguage = user.get().getLang();

        setText(currentLanguage);
        getStyle()
                .set("background", "transparent")
                .set("color", "var(--lumo-body-text-color)")
                .set("width", "50px")
                .set("height", "50px")
                .set("border-radius", "5px");

        ContextMenu contextMenu = new ContextMenu(this);
        contextMenu.setOpenOnClick(true);

        Map<String, Properties> languages = LanguageUtil.LANGUAGES;
        languages.keySet().forEach(locale ->
                contextMenu.addItem(locale, event -> {
                    setText(locale);
                    if (user.get() != null) {
                        user.get().setLang(locale);
                        user.set(authenticatedUser.update(user.get()));
                        LanguageUtil.setCurrentLanguage(locale);
                    }
                    UI.getCurrent().getPage().reload();
                }));
    }
}
