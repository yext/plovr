/*
 * Copyright 2013 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.template.soy.data.internal;

import com.google.template.soy.data.SoyAbstractRecord;
import com.google.template.soy.data.SoyValueProvider;

import java.io.IOException;

import javax.annotation.Nonnull;


/**
 * Internal-use param store for passing data in subtemplate calls.
 *
 * <p> Important: Do not use outside of Soy code (treat as superpackage-private).
 *
 */
public abstract class ParamStore extends SoyAbstractRecord {


  /**
   * Sets a field (i.e. param) in this ParamStore.
   * @param name The field name to set.
   * @param valueProvider A provider of the field value.
   */
  public abstract ParamStore setField(String name, @Nonnull SoyValueProvider valueProvider);


  @Override public boolean coerceToBoolean() {
    throw new UnsupportedOperationException();
  }


  @Override public String coerceToString() {
    throw new UnsupportedOperationException();
  }

  @Override public void render(Appendable appendable) throws IOException {
    throw new UnsupportedOperationException();
  }

  @Override public boolean equals(Object other) {
    throw new UnsupportedOperationException();
  }


  // -----------------------------------------------------------------------------------------------
  // Empty instance.


  public static final ParamStore EMPTY_INSTANCE = new ParamStore() {

    @Override public ParamStore setField(String name, @Nonnull SoyValueProvider valueProvider) {
      throw new UnsupportedOperationException();
    }

    @Override public boolean hasField(String name) {
      return false;
    }

    @Override public SoyValueProvider getFieldProvider(String name) {
      return null;
    }
  };

}
