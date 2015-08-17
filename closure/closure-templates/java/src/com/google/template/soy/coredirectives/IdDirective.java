/*
 * Copyright 2009 Google Inc.
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

package com.google.template.soy.coredirectives;

import com.google.common.collect.ImmutableSet;
import com.google.template.soy.data.SoyValue;
import com.google.template.soy.data.restricted.StringData;
import com.google.template.soy.jssrc.restricted.JsExpr;
import com.google.template.soy.jssrc.restricted.SoyJsSrcPrintDirective;
import com.google.template.soy.shared.restricted.SoyJavaPrintDirective;
import com.google.template.soy.shared.restricted.SoyPurePrintDirective;

import java.util.List;
import java.util.Set;
import javax.inject.Inject;
import javax.inject.Singleton;


/**
 * A directive that marks an identifier such as an HTML id or CSS class name. This directive turns
 * off autoescape for the 'print' tag (if it's on for the template).
 *
 */
@Singleton
@SoyPurePrintDirective
public class IdDirective implements SoyJavaPrintDirective, SoyJsSrcPrintDirective {


  public static final String NAME = "|id";


  @Inject
  public IdDirective() {}


  @Override public String getName() {
    return NAME;
  }


  @Override public Set<Integer> getValidArgsSizes() {
    return ImmutableSet.of(0);
  }


  @Override public boolean shouldCancelAutoescape() {
    return true;
  }


  @Override public SoyValue applyForJava(SoyValue value, List<SoyValue> args) {
    return (value instanceof StringData) ? value : StringData.forValue(value.coerceToString());
  }


  @Override public JsExpr applyForJsSrc(JsExpr value, List<JsExpr> args) {
    return value;
  }

}
