package com.quentity.misc;

import com.quentity.data.Role;
import com.quentity.entity.Entity;
import com.quentity.entity.annotions.handler.IconHandler;
import com.quentity.entity.annotions.handler.MainEntityViewHandler;
import com.quentity.project.adminstrator.User;
import com.quentity.project.education.Main;
import com.quentity.reflection.Reflector;
import com.quentity.security.AuthenticatedUser;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.details.Details;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.shared.Tooltip;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.component.tabs.TabSheetVariant;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.server.auth.AccessAnnotationChecker;
import com.vaadin.flow.spring.annotation.UIScope;
import com.vaadin.flow.theme.lumo.LumoUtility;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import java.io.ByteArrayInputStream;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static com.quentity.misc.ThemeController.*;
import static com.quentity.misc.Utils.addToTabs;

/**
 * The main view is a top-level placeholder for other views.
 */
@org.springframework.stereotype.Component
@UIScope
public class MainLayout extends AppLayout {
    public final TabSheet tabs;
    private H3 viewTitle;

    private AuthenticatedUser authenticatedUser;
    private AccessAnnotationChecker accessChecker;
    private final ApplicationContext applicationContext;
    private boolean firstTime;
    private Div themeSwitch;

    @Autowired
    public MainLayout(ApplicationContext applicationContext, AuthenticatedUser authenticatedUser, AccessAnnotationChecker accessChecker) {
        Optional<User> user = authenticatedUser.get();

        syncTheme(this);
        user.ifPresent(value -> {
            LanguageUtil.setCurrentLanguage(value.getLang());
            VaadinSession current = VaadinSession.getCurrent();
            if (current != null) {
                current.setAttribute(User.class, authenticatedUser.get().get());
            }
        });
        getElement().getStyle().set("height", "100%");
        this.authenticatedUser = authenticatedUser;
        this.accessChecker = accessChecker;
        this.applicationContext = applicationContext;
        tabs = new TabSheet();
        tabs.getElement().getStyle().set("height", "100%");
        tabs.addThemeVariants(TabSheetVariant.LUMO_TABS_SMALL);
        tabs.addSelectedChangeListener(
                event -> {
                    Tab selectedTab = event.getSelectedTab();
                    if (selectedTab == null)
                        viewTitle.setText("");
                    else {
                        Tooltip tooltip = selectedTab.getTooltip();
                        if (tooltip != null)
                            viewTitle.setText(tooltip.getText());
                    }
                }
        );

        UI.getCurrent().getPage().retrieveExtendedClientDetails(details -> {
            handleScreenWidth(details.getScreenWidth());
        });
        addDrawerContent();
        addHeaderContent();
    }

    private void handleScreenWidth(int screenWidth) {
        if (screenWidth > 700) { // not sure if this is the best way to do it , this requires the page to be updated.
            setPrimarySection(Section.NAVBAR);
            getElement().getStyle().set("--vaadin-app-layout-drawer-overlay", "false");
        } else {
            setPrimarySection(Section.DRAWER);
            getElement().getStyle().set("--vaadin-app-layout-drawer-overlay", "true");
        }
    }


    private void addHeaderContent() {
        DrawerToggle toggle = new DrawerToggle();
        toggle.setAriaLabel("Menu toggle");

        viewTitle = new H3();
        viewTitle.addClassNames(LumoUtility.FontSize.SMALL, LumoUtility.Margin.NONE);

        LanguageSelectorWidget languageSelectorWidget = new LanguageSelectorWidget(authenticatedUser);
        HorizontalLayout div1 = new HorizontalLayout(toggle, viewTitle);
        div1.setAlignItems(FlexComponent.Alignment.CENTER);
        div1.setJustifyContentMode(FlexComponent.JustifyContentMode.START);
        themeSwitch = themeSwitch();
        HorizontalLayout div2 = new HorizontalLayout(languageSelectorWidget, themeSwitch);
        div2.getStyle().set("margin-right", "var(--lumo-space-l)");
        div2.getStyle().set("margin-left", "var(--lumo-space-l)");
        div2.setAlignItems(FlexComponent.Alignment.CENTER);
        div2.setJustifyContentMode(FlexComponent.JustifyContentMode.START);
        HorizontalLayout div = new HorizontalLayout(div1, div2);
        div.setWidthFull();
        div.setMaxHeight("50px");
        div.setAlignItems(FlexComponent.Alignment.CENTER);
        div.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        addToNavbar(true, div);
    }

    private void addDrawerContent() {
//        TextField searchDrawerTxt = new TextField();
//        searchDrawerTxt.setPlaceholder(LanguageUtil.get("search"));
//        searchDrawerTxt.setWidth("100%");
//    searchDrawerTxt.getStyle().set("--vaadin-input-field-background", "var(--lumo-base-color)");
//        searchDrawerTxt.setSuffixComponent(VaadinIcon.SEARCH.create());
//        searchDrawerTxt.setClearButtonVisible(true);
//        Header header = new Header(searchDrawerTxt);
//        header.getElement().getStyle().set("align-items", "center");
        Scroller scroller = new Scroller(createNavigation());

        addToDrawer(/*header,*/ scroller, createFooter());
    }

    private VerticalLayout createNavigation() {
        VerticalLayout nav = new VerticalLayout();

        addEntitySubclassesToNav(nav);

        return nav;
    }

    private Footer createFooter() {
        Footer layout = new Footer();

        Optional<User> maybeUser = authenticatedUser.get();
        if (maybeUser.isPresent()) {
            User user = maybeUser.get();

            Avatar avatar = new Avatar(user.getUsername());
            StreamResource resource = new StreamResource("profile-pic",
                    () -> new ByteArrayInputStream(user.getProfilePicture()));
            avatar.setImageResource(resource);
            avatar.setThemeName("xsmall");
            avatar.getElement().setAttribute("tabindex", "-1");

            MenuBar userMenu = new MenuBar();
            userMenu.setThemeName("tertiary-inline contrast");

            MenuItem userName = userMenu.addItem("");
            Div div = new Div();
            div.getStyle().set("margin-right", "var(--lumo-space-l)");
            div.getStyle().set("margin-left", "var(--lumo-space-l)");
            div.add(avatar);
            div.add(user.getUsername());
            div.add(new Icon("lumo", "dropdown"));
            div.getElement().getStyle().set("display", "flex");
            div.getElement().getStyle().set("align-items", "center");
            div.getElement().getStyle().set("gap", "var(--lumo-space-s)");
            userName.add(div);
            userName.getSubMenu().addItem(LanguageUtil.get("logout"), e -> {
                authenticatedUser.logout();
            });

            layout.add(userMenu);
        } else {
            Anchor loginLink = new Anchor("login", LanguageUtil.get("signin"));
            layout.add(loginLink);
        }

        return layout;
    }

    @Override
    protected void afterNavigation() {
        super.afterNavigation();
        viewTitle.setText(getCurrentPageTitle());
    }

    @Override
    public void setContent(Component content) {
        content.removeFromParent();
        if (content instanceof com.quentity.project.education.Main) {
            ((Main) content).setMainLayout(this);
            tabs.setSelectedTab(null);
            super.setContent(content);
            return;
        }

        addToTabs("", content, tabs);
        super.setContent(tabs);
    }

    private String getCurrentPageTitle() {
        if (getContent() instanceof com.quentity.project.education.Main) {
            return LanguageUtil.
                    get("main");
        }
        TabSheet content = (TabSheet) getContent();
        if (content.getSelectedTab() == null) {
            return "";
        }
        return LanguageUtil.
                get(content.getComponent(content.getSelectedTab()).getClass().getName());
    }

    /**
     * Adds side navigation items for each subclass of {@link Entity} that is accessible.
     *
     * <p>This method uses reflection to scan the entire classpath for subclasses of {@link Entity}.
     * It creates a {@link CustomSideNavItem} for each subclass that is accessible according to
     * the {@link AccessAnnotationChecker} and adds it to the provided {@link VerticalLayout}.
     *
     * <p>When a {@link CustomSideNavItem} is clicked, it retrieves an instance of the corresponding
     * {@link Entity} from the application context and sets it as the content of the main layout.
     *
     * @param nav The {@link VerticalLayout} to add navigation items to.
     */
    public void addEntitySubclassesToNav(VerticalLayout nav) {
        Set<Class<? extends Entity>> entitySubclasses = Reflector.getEntities();
        User user = authenticatedUser.get().orElse(null);
        //This check to prevent adding access group to admin
        boolean isAdmin = user != null && user.getRoles().contains(Role.ADMIN);
        Map<String, VerticalLayout> packageEntitiesListMap = new LinkedHashMap<>();
        // Loop through each subclass
        for (Class<? extends Entity> entityClass : entitySubclasses) {
            // Check access before adding to navigation
            if ((isAdmin && accessChecker.hasAccess(entityClass)) ||
                    (accessChecker.hasAccess(entityClass) &&
                            User.getCurrentUserDefaultQuery(entityClass) != null)) {
                String packageName = entityClass.getPackageName();
                VerticalLayout verticalLayout = packageEntitiesListMap.computeIfAbsent(packageName, p -> new VerticalLayout());
                CustomSideNavItem item = new CustomSideNavItem(
                        LanguageUtil.
                                get(packageName + "." + entityClass.getSimpleName()),
                        IconHandler.getIcon(entityClass),
                        (event) -> {
                            getUI().ifPresent(ui -> {
                                VerticalLayout content = MainEntityViewHandler.
                                        getMainEntityView(authenticatedUser.get().orElse(null), entityClass);

                                if (!firstTime) {
                                    firstTime = true;
                                }
                                setContent(content);
                            });
                        }
                );
                verticalLayout.add(item);
            }
        }

        outer:
        for (Map.Entry<String, VerticalLayout> entry : packageEntitiesListMap.entrySet()) {
            String key = entry.getKey();
            String key2 = key;
            while (true) {
                int index = key2.lastIndexOf(".");
                if (index == -1) break;
                key2 = key.substring(0, index);
                VerticalLayout verticalLayout = packageEntitiesListMap.get(key2);
                if (verticalLayout != null) {
                    verticalLayout.add(new Details(LanguageUtil.get(key), entry.getValue()));
                    continue outer;
                }
            }
            nav.add(new Details(LanguageUtil.get(key), entry.getValue()));
        }
    }

    private void toggleTheme() {
        if (isLightTheme(this)) {
            setDarkTheme(this);
        } else {
            setLightTheme(this);
        }
    }

    private Div themeSwitch() {
        String sun = "☀\uFE0F";
        String moon = "\uD83C\uDF19";
        Span moonOrSunIcon = new Span(isLightTheme(this) ? moon : sun);
        moonOrSunIcon.addSingleClickListener(e -> {
            toggleTheme();
            moonOrSunIcon.setText(isLightTheme(this) ? moon : sun);
        });

        return new Div(moonOrSunIcon);
    }


}
