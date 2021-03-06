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
package com.google.gwt.user.client.rpc.core.java.lang;

import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.client.rpc.SerializationStreamReader;
import com.google.gwt.user.client.rpc.SerializationStreamWriter;

/**
 * Custom field serializer for {@link java.lang.Void}.
 */
public final class Void_CustomFieldSerializer {

  @SuppressWarnings("unused")
  public static void deserialize(SerializationStreamReader streamReader,
      Void instance) {
  }

  @SuppressWarnings("unused")
  public static Void instantiate(SerializationStreamReader streamReader)
      throws SerializationException {
    return null;
  }

  @SuppressWarnings("unused")
  public static void serialize(SerializationStreamWriter streamWriter,
      Void instance) throws SerializationException {
  }
}
