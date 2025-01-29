package com.quentity.misc;


import com.quentity.dialog.Ask;
import com.quentity.entity.Entity;
import com.quentity.entity.EntityView;
import com.quentity.entity.SelfView;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.dnd.DragSource;
import com.vaadin.flow.component.dnd.DropEffect;
import com.vaadin.flow.component.dnd.DropTarget;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.theme.lumo.LumoIcon;

import java.util.HashMap;
import java.util.stream.Collectors;

public class Utils {
    private static final HashMap<String, Boolean> inheritanceMap = new HashMap<>();

    public static void addToTabs(String suffix, Component content, TabSheet tabs, Class<? extends Entity>... clazz) {
        Span closeTabSpan = new Span(LumoIcon.CROSS.create());
        Class contentClass = null;
        if (clazz != null && clazz.length > 0)
            contentClass = clazz[0];
        if (contentClass == null) {
            if (content instanceof EntityView<?>)
                contentClass = ((EntityView<?>) content).getClazz();
            else
                contentClass = content.getClass();
        }
        Tab tab = new Tab(
                new Span(LanguageUtil.
                        get(contentClass.getName()) + suffix),
                closeTabSpan);
        if (content instanceof SelfView)
            ((SelfView) content).addRunnable(() -> close(content, tabs, tab));
        tab.setTooltipText(LanguageUtil.get(contentClass.getName()) + suffix);
        closeTabSpan.addClickListener(
                e -> {
                    if (content instanceof SelfView) {
                        Entity entity = ((SelfView) content).getEntity();
                        if (entity != null && entity.isEntityEdited()) {
                            Ask.builder().
                                    header(LanguageUtil.get("unsavedChanges")).
                                    message(LanguageUtil.get("unsavedChangesMessage")).
                                    confirmText(LanguageUtil.get("save")).
                                    cancelText(LanguageUtil.get("cancel")).
                                    rejectText(LanguageUtil.get("discard")).
                                    onConfirm(e1 -> {
                                        entity.save();
                                        close(content, tabs, tab);
                                    }).
                                    onReject(e1 -> {
                                        close(content, tabs, tab);
                                    }).
                                    build().
                                    show();
                        } else close(content, tabs, tab);
                    } else
                        close(content, tabs, tab);
                }
        );
        DragSource<Tab> dragSource = DragSource.create(tab);
        dragSource.setDraggable(true);

        DropTarget<Tab> dropTarget = DropTarget.create(tab);
        dropTarget.setDropEffect(DropEffect.LINK);
        dropTarget.setActive(true);

        dropTarget.addDropListener(e -> {
            Tab droppedTab = (Tab) e.getDragSourceComponent().orElse(null);
            tabs.setSelectedTab(droppedTab);
            Component droppedTabComponent = tabs.getComponent(droppedTab);
            tabs.remove(droppedTab);

            tabs.setSelectedTab(tab);
            Component component = tabs.getComponent(tab);
            tabs.remove(tab);

            SplitLayout splitLayout = new SplitLayout(component, droppedTabComponent);

            tabs.add(tab, splitLayout);
            //Merge names
            String addedTabSpanText = ((Span) droppedTab.getChildren().
                    filter(com -> com.getChildren().collect(Collectors.toSet()).isEmpty()).
                    toList().getFirst()).getText();
            Span firstTabSpan = (Span) tab.getChildren().
                    filter(com -> com.getChildren().collect(Collectors.toSet()).isEmpty()).
                    toList().getFirst();

            firstTabSpan.
                    add(" | " + addedTabSpanText);
            tab.setTooltipText(firstTabSpan.getText());
        });

        tabs.add(tab, content);
        tabs.setSelectedTab(tab);
    }

    private static void close(Component content, TabSheet tabs, Tab tab) {
        tabs.remove(tab);
        if (tabs.getSelectedTab() == null) {
            content.getUI().ifPresent(ui -> ui.navigate(""));
        }
    }

    public static boolean isInheritedFrom(Class<?> subClass, Class<?> superClass) {
        String key = subClass.getName() + superClass.getName();
        Boolean value = inheritanceMap.get(key);
        if (value != null)
            return value;

        Class<?> currentClass = subClass;
        while (currentClass != null) {
            if (currentClass == superClass) {
                inheritanceMap.put(key, true);
                return true;
            }
            currentClass = currentClass.getSuperclass();
        }
        inheritanceMap.put(key, false);
        return false;
    }

    public static boolean isInheritedFrom(Class<?> subClass, Class<?>... superClasses) {
        for (Class<?> superClass : superClasses) {
            if (isInheritedFrom(subClass, superClass))
                return true;
        }
        return false;
    }
}
