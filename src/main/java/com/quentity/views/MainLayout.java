package com.quentity.views;

import com.quentity.Application;
import com.quentity.data.User;
import com.quentity.entity.Entity;
import com.quentity.entity.GridView;
import com.quentity.entity.annotions.handler.IconHandler;
import com.quentity.entity.annotions.handler.MainEntityViewHandler;
import com.quentity.misc.LanguageUtil;
import com.quentity.security.AuthenticatedUser;
import com.quentity.views.myview.Main;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.shared.Tooltip;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.component.tabs.TabSheetVariant;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.server.auth.AccessAnnotationChecker;
import com.vaadin.flow.spring.annotation.UIScope;
import com.vaadin.flow.theme.lumo.LumoIcon;
import com.vaadin.flow.theme.lumo.LumoUtility;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import com.vaadin.flow.component.textfield.TextField;
import java.io.ByteArrayInputStream;
import java.util.Optional;
import java.util.Set;

import static com.quentity.misc.Utils.addToTabs;

/**
 * The main view is a top-level placeholder for other views.
 */
@org.springframework.stereotype.Component
@UIScope
public class MainLayout extends AppLayout {

  public final TabSheet tabs;
  private H1 viewTitle;

  private AuthenticatedUser authenticatedUser;
  private AccessAnnotationChecker accessChecker;
  private final ApplicationContext applicationContext;
  private boolean firstTime;

  @Autowired
  public MainLayout(ApplicationContext applicationContext, AuthenticatedUser authenticatedUser, AccessAnnotationChecker accessChecker) {
    Optional<User> user = authenticatedUser.get();
    user.ifPresent(value -> LanguageUtil.setCurrentLanguage(value.getLang()));
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
    if(screenWidth > 700){ // not sure if this is the best way to do it , this requires the page to be updated.
      setPrimarySection(Section.NAVBAR);
      getElement().getStyle().set("--vaadin-app-layout-drawer-overlay", "false");
    }else {
      setPrimarySection(Section.DRAWER);
      getElement().getStyle().set("--vaadin-app-layout-drawer-overlay", "true");
    }
  }


  private void addHeaderContent() {
    DrawerToggle toggle = new DrawerToggle();
    toggle.setAriaLabel("Menu toggle");

    viewTitle = new H1();
    viewTitle.addClassNames(LumoUtility.FontSize.LARGE, LumoUtility.Margin.NONE);

    LanguageSelectorWidget languageSelectorWidget = new LanguageSelectorWidget(authenticatedUser);

    addToNavbar(true, toggle, viewTitle, languageSelectorWidget);
  }

  private void addDrawerContent() {
    TextField searchDrawerTxt = new TextField();
    searchDrawerTxt.setPlaceholder(LanguageUtil.getCurrentLanguageProperties().getProperty("search"));
    searchDrawerTxt.setWidth("100%");
//    searchDrawerTxt.getStyle().set("--vaadin-input-field-background", "var(--lumo-base-color)");
    searchDrawerTxt.setSuffixComponent(VaadinIcon.SEARCH.create());
    searchDrawerTxt.setClearButtonVisible(true);
    Header header = new Header(searchDrawerTxt);
    header.getElement().getStyle().set("align-items", "center");
    Scroller scroller = new Scroller(createNavigation());

    addToDrawer(header, scroller, createFooter());
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

      Avatar avatar = new Avatar(user.getName());
      StreamResource resource = new StreamResource("profile-pic",
              () -> new ByteArrayInputStream(user.getProfilePicture()));
      avatar.setImageResource(resource);
      avatar.setThemeName("xsmall");
      avatar.getElement().setAttribute("tabindex", "-1");

      MenuBar userMenu = new MenuBar();
      userMenu.setThemeName("tertiary-inline contrast");

      MenuItem userName = userMenu.addItem("");
      Div div = new Div();
      div.add(avatar);
      div.add(user.getName());
      div.add(new Icon("lumo", "dropdown"));
      div.getElement().getStyle().set("display", "flex");
      div.getElement().getStyle().set("align-items", "center");
      div.getElement().getStyle().set("gap", "var(--lumo-space-s)");
      userName.add(div);
      userName.getSubMenu().addItem("Sign out", e -> {
        authenticatedUser.logout();
      });

      layout.add(userMenu);
    } else {
      Anchor loginLink = new Anchor("login", "Sign in");
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
    if (content instanceof com.quentity.views.myview.Main) {
      ((Main) content).setMainLayout(this);
      tabs.setSelectedTab(null);
      super.setContent(content);
      return;
    }

    addToTabs("", content, tabs);
    super.setContent(tabs);
  }

  private String getCurrentPageTitle() {
    if (getContent() instanceof com.quentity.views.myview.Main) {
      return LanguageUtil.getCurrentLanguageProperties()
              .getProperty("main");
    }
    TabSheet content = (TabSheet) getContent();
    if (content.getSelectedTab() == null) {
      return "";
    }
    return LanguageUtil.getCurrentLanguageProperties()
            .getProperty(content.getComponent(content.getSelectedTab()).getClass().getName());
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
    // Create a Reflections object configured to scan the entire classpath
    Reflections reflections = new Reflections(new ConfigurationBuilder()
            .setUrls(ClasspathHelper.forClassLoader(ClasspathHelper.contextClassLoader()))
            .setScanners(Scanners.SubTypes.filterResultsBy(c -> true))
    );

    // Get all subclasses of Entity
    Set<Class<? extends Entity>> entitySubclasses = reflections.getSubTypesOf(Entity.class);

    // Loop through each subclass
    for (Class<? extends Entity> entityClass : entitySubclasses) {
      // Check access before adding to navigation
      if (accessChecker.hasAccess(entityClass)) {
        CustomSideNavItem item = new CustomSideNavItem(
                LanguageUtil.getCurrentLanguageProperties()
                        .getProperty(entityClass.getPackageName() + "." + entityClass.getSimpleName()),
                IconHandler.getIcon(entityClass),
                (event) -> {
                  getUI().ifPresent(ui -> {
                    Entity bean = applicationContext.getBean(entityClass);

                    VerticalLayout content = MainEntityViewHandler.getMainEntityView(bean);

                    if (!firstTime) {
                      firstTime = true;
                    }
                    setContent(content);
                  });
                  Notification.show("Click", 1000, Notification.Position.BOTTOM_END);
                }
        );
        nav.add(item);
      }
    }
  }
}
