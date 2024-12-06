package com.quentity.field.events;

@FunctionalInterface
public interface FieldChanged<T> {
  void onFieldChanged(T oldValue, T newValue);
}
