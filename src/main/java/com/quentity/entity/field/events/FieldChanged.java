package com.quentity.entity.field.events;

@FunctionalInterface
public interface FieldChanged<T> {
  void onFieldChanged(T oldValue, T newValue);
}
