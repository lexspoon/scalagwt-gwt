/*
 * Copyright 2010 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.google.gwt.validation.client.impl;

import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.Set;

import javax.validation.MessageInterpolator;
import javax.validation.metadata.BeanDescriptor;
import javax.validation.metadata.ConstraintDescriptor;

/**
 * Context for a {@link com.google.gwt.validation.client.GwtValidation}.
 *
 * <p>
 * NOTE: This class is not thread safe.
 *
 * @param <T> the type of the root bean.
 *
 */
public class GwtValidationContext<T> {

  private final BeanDescriptor beanDescriptor;
  private PathImpl path = new PathImpl();
  private final T rootBean;
  private final MessageInterpolator messageInterpolator;
  private final AbstractGwtValidator validator;

  /**
   * The set of validated object.
   * <p>
   * This set is shared with and updated by children contexts created by
   * {@link #append(String)}, {@link #appendIndex(String, int)} and
   * {@link #appendKey(String, String)}.
   */
  private final Set<Object> validatedObjects;

  public GwtValidationContext(T rootBean, BeanDescriptor beanDescriptor,
      MessageInterpolator messageInterpolator, AbstractGwtValidator validator) {
    this(rootBean, beanDescriptor, messageInterpolator, validator,
        new HashSet<Object>());
  }

  private GwtValidationContext(T rootBean, BeanDescriptor beanDescriptor,
      MessageInterpolator messageInterpolator, AbstractGwtValidator validator,
      Set<Object> validatedObjects) {
    this.rootBean = rootBean;
    this.beanDescriptor = beanDescriptor;
    this.messageInterpolator = messageInterpolator;
    this.validator = validator;
    this.validatedObjects = validatedObjects;
  }

  public final void addValidatedObject(Object o) {
    validatedObjects.add(o);
  }

  public final boolean alreadyValidated(Object o) {
    return validatedObjects.contains(o);
  }

  /**
   * Append a node named name to the path..
   *
   * @param name
   * @return the new GwtValidationContext.
   */
  public GwtValidationContext<T> append(String name) {
    GwtValidationContext<T> temp = new GwtValidationContext<T>(rootBean,
        beanDescriptor, messageInterpolator, validator, validatedObjects);
    temp.path = path.append(name);
    return temp;
  }

  /**
   * Append a indexed node to the path.
   *
   * @param name
   * @return the new GwtValidationContext.
   */
  public GwtValidationContext<T> appendIndex(String name, int index) {
    GwtValidationContext<T> temp = new GwtValidationContext<T>(rootBean,
        beanDescriptor, messageInterpolator, validator, validatedObjects);
    temp.path = path.appendIndex(name, index);
    return temp;
  }

  /**
   * Append a keyed node to the path.
   *
   * @param name
   * @return the new GwtValidationContext.
   */
  public GwtValidationContext<T> appendKey(String name, String key) {
    GwtValidationContext<T> temp = new GwtValidationContext<T>(rootBean,
        beanDescriptor, messageInterpolator, validator, validatedObjects);
    temp.path = path.appendKey(name, key);
    return temp;
  }

  public <A extends Annotation, V> ConstraintValidatorContextImpl<A, V> createConstraintValidatorContext(
      ConstraintDescriptor<A> descriptor) {
    return new ConstraintValidatorContextImpl<A, V>(path, descriptor);
  }

  public MessageInterpolator getMessageInterpolator() {
    return messageInterpolator;
  }

  public T getRootBean() {
    return rootBean;
  }

  @SuppressWarnings("unchecked")
  public Class<T> getRootBeanClass() {
    return (Class<T>) rootBean.getClass();
  }

  public AbstractGwtValidator getValidator() {
    return validator;
  }
}
