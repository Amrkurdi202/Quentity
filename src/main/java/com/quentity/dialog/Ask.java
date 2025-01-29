package com.quentity.dialog;

import com.quentity.misc.LanguageUtil;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import lombok.Builder;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class Ask {
    private ConfirmDialog confirmationDialog;
    private Map<String, Object> contextData;

    @Builder
    public Ask(String header,
               String message,
               Consumer<Map<String, Object>> initContextData,
               Consumer<Map<String, Object>> onConfirm,
               Consumer<Map<String, Object>> onCancel,
               Consumer<Map<String, Object>> onReject,
               String confirmText,
               String cancelText,
               String rejectText) {
        contextData = new ConcurrentHashMap<>();
        if (initContextData != null)
            initContextData.accept(contextData);
        confirmationDialog = new ConfirmDialog();
        confirmationDialog.setHeader(header == null ? "" : header);
        validate(message, onConfirm, onCancel, onReject, confirmText, cancelText, rejectText);
        confirmationDialog.setText(message);
        confirmationDialog.setConfirmText(confirmText);
        if (cancelText != null) {
            confirmationDialog.setCancelable(true);
            confirmationDialog.setCancelText(cancelText);
            confirmationDialog.addCancelListener(event -> onCancel.accept(contextData));
        }
        if (rejectText != null) {
            confirmationDialog.setRejectable(true);
            confirmationDialog.setRejectText(rejectText);
            confirmationDialog.addRejectListener(event -> onReject.accept(contextData));
        }
        confirmationDialog.addConfirmListener(event -> onConfirm.accept(contextData));
    }

    public void show() {
        confirmationDialog.open();
    }

    private static void validate(String message, Consumer<Map<String, Object>> onConfirm, Consumer<Map<String, Object>> onCancel, Consumer<Map<String, Object>> onReject, String confirmText, String cancelText, String rejectText) {
        if (message == null)
            throw new IllegalArgumentException("message is required");
        if (confirmText == null)
            throw new IllegalArgumentException("confirmText is required");
        if (onConfirm == null)
            throw new IllegalArgumentException("onConfirm is required");
        if (rejectText != null && onReject == null)
            throw new IllegalArgumentException("onReject is required");
    }

    @Builder
    public static Ask sure(String message,
                           Consumer<Map<String, Object>> initContextData,
                           Consumer<Map<String, Object>> onConfirm,
                           Consumer<Map<String, Object>> onCancel) {
        String areYouSure = LanguageUtil.get("areYouSure");
        return Ask.builder().
                header(areYouSure).
                message(message != null && !message.isEmpty() ? message : areYouSure).
                confirmText(LanguageUtil.get("Yes")).
                cancelText(LanguageUtil.get("No")).
                initContextData(initContextData).
                onConfirm(onConfirm).
                onCancel(onCancel).
                build();
    }
}
