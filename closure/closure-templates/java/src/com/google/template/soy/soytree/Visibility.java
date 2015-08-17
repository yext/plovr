/*
 * Copyright 2014 Google Inc.
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

package com.google.template.soy.soytree;

import com.google.common.collect.ImmutableMap;

import java.util.Set;

public enum Visibility {
  // {template .foo visibility="private"}
  PRIVATE("private"),
  // {template .foo private="true"}, or {template .foo visibility="legacy-private"}
  LEGACY_PRIVATE("legacy-private"),
  // {template .foo visibility="public"} or just {template .foo}
  PUBLIC("public");

  private final String attributeValue;

  Visibility(String attributeValue) {
    this.attributeValue = attributeValue;
  }

  public String getAttributeValue() {
    return this.attributeValue;
  }

  private static final ImmutableMap<String, Visibility> attrValuesToVisibilityLevels;

  static {
    ImmutableMap.Builder<String, Visibility> builder = ImmutableMap.builder();
    for (Visibility v : Visibility.values()) {
      builder.put(v.attributeValue, v);
    }
    attrValuesToVisibilityLevels = builder.build();
  }

  public static Set<String> getAttributeValues() {
    return attrValuesToVisibilityLevels.keySet();
  }

  public static Visibility forAttributeValue(String attributeValue) {
    return attrValuesToVisibilityLevels.get(attributeValue);
  }
}
