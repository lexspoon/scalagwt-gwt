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
package com.google.gwt.valuestore.shared.impl;

import com.google.gwt.valuestore.shared.Property;
import com.google.gwt.valuestore.shared.Record;
import com.google.gwt.valuestore.shared.RecordChangedEvent;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Used by {@link Record} implementations generated by
 * {@link com.google.gwt.requestfactory.rebind.RequestFactoryGenerator
 * RequestFactoryGenerator}. Defines the set of properties for a class of
 * Record, and serves as a factory for these records and their
 * {@link RecordChangedEvent}s.
 * 
 * @param <R> the type of the Records this schema describes
 */
public abstract class RecordSchema<R extends Record> {
  private final Set<Property<?>> allProperties;
  {
    Set<Property<?>> set = new HashSet<Property<?>>();
    set.add(Record.id);
    set.add(Record.version);
    allProperties = Collections.unmodifiableSet(set);
  }

  public Set<Property<?>> allProperties() {
    return allProperties;
  }

  public abstract R create(RecordJsoImpl jso);

  public abstract RecordChangedEvent<?, ?> createChangeEvent(Record record);

  public RecordChangedEvent<?, ?> createChangeEvent(RecordJsoImpl jsoRecord) {
    R record = create(jsoRecord);
    return createChangeEvent(record);
  }
}