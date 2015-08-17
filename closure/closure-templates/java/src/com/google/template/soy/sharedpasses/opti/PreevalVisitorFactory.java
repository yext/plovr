/*
 * Copyright 2011 Google Inc.
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

package com.google.template.soy.sharedpasses.opti;

import com.google.common.base.Preconditions;
import com.google.template.soy.data.SoyRecord;
import com.google.template.soy.data.SoyValueHelper;
import com.google.template.soy.error.ErrorReporter;
import com.google.template.soy.shared.internal.SharedModule.Shared;
import com.google.template.soy.shared.restricted.SoyJavaFunction;
import com.google.template.soy.sharedpasses.render.Environment;
import com.google.template.soy.sharedpasses.render.EvalVisitor.EvalVisitorFactory;

import java.util.Map;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * A factory for creating PreevalVisitor objects.
 *
 * <p> Important: Do not use outside of Soy code (treat as superpackage-private).
 *
 */
@Singleton
public final class PreevalVisitorFactory implements EvalVisitorFactory {

  /** Instance of SoyValueHelper to use. */
  private final SoyValueHelper valueHelper;

  /** Map of all SoyJavaFunctions (name to function). */
  private final Map<String, SoyJavaFunction> soyJavaFunctionsMap;

  /** For reporting errors. */
  private final ErrorReporter errorReporter;

  /**
   * @param soyJavaFunctionsMap Map of all SoyJavaFunctions (name to function).
   */
  @Inject
  public PreevalVisitorFactory(
      SoyValueHelper valueHelper,
      @Shared Map<String, SoyJavaFunction> soyJavaFunctionsMap,
      ErrorReporter errorReporter) {
    this.valueHelper = valueHelper;
    this.soyJavaFunctionsMap = soyJavaFunctionsMap;
    this.errorReporter = errorReporter;
  }


  public PreevalVisitor create(Environment env) {
    return new PreevalVisitor(valueHelper, soyJavaFunctionsMap,  env, errorReporter);
  }


  @Override public PreevalVisitor create(
      @Nullable SoyRecord ijData, Environment env) {

    // PreevalVisitor cannot handle ijData references.
    Preconditions.checkArgument(ijData == null);

    return new PreevalVisitor(valueHelper, soyJavaFunctionsMap, env, errorReporter);
  }
}
