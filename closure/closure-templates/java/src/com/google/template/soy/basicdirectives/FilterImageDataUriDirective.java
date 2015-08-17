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

package com.google.template.soy.basicdirectives;

import com.google.common.collect.ImmutableSet;
import com.google.template.soy.data.SoyValue;
import com.google.template.soy.jssrc.restricted.JsExpr;
import com.google.template.soy.jssrc.restricted.SoyJsSrcPrintDirective;
import com.google.template.soy.pysrc.restricted.PyExpr;
import com.google.template.soy.pysrc.restricted.SoyPySrcPrintDirective;
import com.google.template.soy.shared.restricted.Sanitizers;
import com.google.template.soy.shared.restricted.SoyJavaPrintDirective;
import com.google.template.soy.shared.restricted.SoyPurePrintDirective;

import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;


/**
 * Implements the |filterImageDataUri directive, which only accepts data URI's corresponding to
 * images.
 *
 * <p>
 * Note that this directive is not autoescape cancelling, and can thus be
 * used in strict templates.  The directive returns its result as an object
 * of type SanitizedContent of kind URI.
 */
@Singleton
@SoyPurePrintDirective
final class FilterImageDataUriDirective implements SoyJavaPrintDirective, SoyJsSrcPrintDirective,
    SoyPySrcPrintDirective{


  private static final Set<Integer> VALID_ARGS_SIZES = ImmutableSet.of(0);


  @Inject
  public FilterImageDataUriDirective() {}


  @Override public String getName() {
    return "|filterImageDataUri";
  }

  @Override public final Set<Integer> getValidArgsSizes() {
    return VALID_ARGS_SIZES;
  }

  @Override public boolean shouldCancelAutoescape() {
    return false;
  }

  @Override public SoyValue applyForJava(SoyValue value, List<SoyValue> args) {
    return Sanitizers.filterImageDataUri(value);
  }

  @Override public JsExpr applyForJsSrc(JsExpr value, List<JsExpr> args) {
    return new JsExpr("soy.$$filterImageDataUri(" + value.getText() + ")", Integer.MAX_VALUE);
  }

  @Override public PyExpr applyForPySrc(PyExpr value, List<PyExpr> args) {
    return new PyExpr("sanitize.filter_image_data_uri(" + value.getText() + ")", Integer.MAX_VALUE);
  }
}
