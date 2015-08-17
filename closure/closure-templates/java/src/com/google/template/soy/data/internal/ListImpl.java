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

import com.google.template.soy.data.SoyValueHelper;
import com.google.template.soy.data.SoyValueProvider;

import java.util.List;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Internal implementation of SoyList backed by a list of SoyValueProviders. Do not use directly;
 * instead, use {@link SoyValueHelper#convert}.
 *
 * <p>Important: Do not use outside of Soy code (treat as superpackage-private).
 *
 */
@ParametersAreNonnullByDefault
public final class ListImpl extends ListBackedList {


  /** Creates a Soy list implementation backed by the given map. */
  public static ListImpl forProviderList(List<? extends SoyValueProvider> providerList) {
    return new ListImpl(providerList);
  }


  ListImpl(List<? extends SoyValueProvider> providerList) {
    super(providerList);
  }
}
