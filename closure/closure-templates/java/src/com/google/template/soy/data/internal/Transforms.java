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

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.template.soy.data.SoyValue;
import com.google.template.soy.data.SoyValueProvider;

/**
 * Transforms on value types used in the implementation.
 *
 */
final class Transforms {
  static final Function<SoyValueProvider, SoyValue> RESOLVE_FUNCTION =
      new Function<SoyValueProvider, SoyValue>() {
        @Override public SoyValue apply(SoyValueProvider provider) {
          Preconditions.checkNotNull(provider);
          return provider.resolve();
        }
      };
}