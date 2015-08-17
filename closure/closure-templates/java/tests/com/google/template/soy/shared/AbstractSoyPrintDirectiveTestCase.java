/*
 * Copyright 2010 Google Inc.
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

package com.google.template.soy.shared;

import static com.google.common.truth.Truth.assertThat;
import static java.nio.charset.StandardCharsets.UTF_8;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.template.soy.data.SoyValue;
import com.google.template.soy.data.SoyValueHelper;
import com.google.template.soy.data.restricted.StringData;
import com.google.template.soy.jssrc.restricted.JsExpr;
import com.google.template.soy.jssrc.restricted.SoyJsSrcPrintDirective;
import com.google.template.soy.shared.restricted.SoyJavaPrintDirective;

import junit.framework.TestCase;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.NativeArray;
import org.mozilla.javascript.NativeObject;
import org.mozilla.javascript.ScriptableObject;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Arrays;
import java.util.List;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;


/**
 * Utilities for testing Soy print directives.
 *
 */
@ParametersAreNonnullByDefault
public abstract class AbstractSoyPrintDirectiveTestCase extends TestCase {


  /**
   * @param expectedOutput The expected result of applying directive to value with args.
   * @param value The test input.
   * @param directive The directive whose {@link SoyJavaPrintDirective#applyForJava} is under test.
   * @param args Arguments to the Soy directive.
   */
  protected void assertTofuOutput(
      String expectedOutput, @Nullable Object value, SoyJavaPrintDirective directive,
      Object... args) {
    assertTofuOutput(StringData.forValue(expectedOutput), value, directive, args);
  }


  /**
   * @param expectedOutput The expected result of applying directive to value with args.
   * @param value The test input.
   * @param directive The directive whose {@link SoyJavaPrintDirective#applyForJava} is under test.
   * @param args Arguments to the Soy directive.
   */
  protected void assertTofuOutput(
      SoyValue expectedOutput, Object value, SoyJavaPrintDirective directive, Object... args) {
    ImmutableList.Builder<SoyValue> argsData = ImmutableList.builder();
    for (Object arg : args) {
      argsData.add(SoyValueHelper.UNCUSTOMIZED_INSTANCE.convert(arg).resolve());
    }
    assertThat(
        directive.applyForJava(
            SoyValueHelper.UNCUSTOMIZED_INSTANCE.convert(value).resolve(),
            argsData.build()).toString())
        .isEqualTo(expectedOutput.toString());
  }


  /**
   * Aggregates multiple JS tests so that they can be run in a single JS interpreter.
   */
  public final class JsSrcPrintDirectiveTestBuilder {
    private final ImmutableList.Builder<TestData> testDataListBuilder = ImmutableList.builder();

    public JsSrcPrintDirectiveTestBuilder() {
      // Nothing to do.
    }

    /**
     * @param expectedOutput The string value that should be produced by directive given the
     *     accompanying value and arguments.
     * @param valueJs Source code for a JavaScript expression that produces a value for the
     *     directive.
     * @param directive The directive under test.
     * @param directiveArgsJs Source code for JavaScript expressions that produce the directive's
     *     arguments.
     */
    public JsSrcPrintDirectiveTestBuilder addTest(
        String expectedOutput, String valueJs, SoyJsSrcPrintDirective directive,
        String... directiveArgsJs) {
      testDataListBuilder.add(
          new TestData(expectedOutput, valueJs, directive, Arrays.asList(directiveArgsJs)));
      return this;
    }

    public void runTests() {
      StringBuilder generatedJsExprsAsJsArray = new StringBuilder();
      ImmutableList.Builder<String> expectedOutputsListBuilder = ImmutableList.builder();

      generatedJsExprsAsJsArray.append("[");
      boolean generatedJsExprsAsJsArrayHasElements = false;
      for (TestData testData : testDataListBuilder.build()) {
        expectedOutputsListBuilder.add(testData.expectedOutput);

        ImmutableList.Builder<JsExpr> args = ImmutableList.builder();
        for (String argJs : testData.directiveArgsJs) {
          args.add(new JsExpr("(" + argJs + ")", Integer.MAX_VALUE));
        }
        if (generatedJsExprsAsJsArrayHasElements) {
          generatedJsExprsAsJsArray.append(",\n");
        }
        generatedJsExprsAsJsArray
            .append("String(")
            .append(
                testData.directive.applyForJsSrc(
                    new JsExpr("(" + testData.valueJs + ")", Integer.MAX_VALUE), args.build())
                .getText())
            .append(")");
        generatedJsExprsAsJsArrayHasElements = true;
      }
      generatedJsExprsAsJsArray.append("]");

      Context context = new ContextFactory().enterContext();
      context.setOptimizationLevel(-1);  // Only running once.
      ScriptableObject globalScope = context.initStandardObjects();

      // Define a fake navigator object for Closure's userAgent library.
      NativeObject navigator = new NativeObject();
      ScriptableObject.putConstProperty(navigator, "userAgent", "testZilla");
      globalScope.defineProperty("navigator", navigator, ScriptableObject.DONTENUM);

      // Disable asserts, so that zSoyz will be returned instead of throwing an exception.
      NativeObject defines = new NativeObject();
      ScriptableObject.putConstProperty(defines, "goog.asserts.ENABLE_ASSERTS", false);
      globalScope.defineProperty("CLOSURE_UNCOMPILED_DEFINES", defines, ScriptableObject.CONST);

      try {
        String soyutilsPath = getSoyUtilsPath();
        Reader soyutils = new InputStreamReader(new FileInputStream(soyutilsPath), UTF_8);
        try {
          String basename = soyutilsPath.substring(soyutilsPath.lastIndexOf('/') + 1);
          context.evaluateReader(globalScope, soyutils, basename, 1, null);
        } finally {
          soyutils.close();
        }
      } catch (IOException ex) {
        throw new AssertionError(ex);
      }

      NativeArray outputAsJsList = (NativeArray) context.evaluateString(
          globalScope,
          generatedJsExprsAsJsArray.toString(),
          getClass() + ":" + getName(),  // File name for JS traces.
          1,
          null);
      long n = outputAsJsList.getLength();
      ImmutableList.Builder<String> actualOutputListBuilder = ImmutableList.builder();
      for (int i = 0; i < n; ++i) {
        actualOutputListBuilder.add((String) outputAsJsList.get(i, globalScope));
      }

      assertThat(Joiner.on('\n').join(actualOutputListBuilder.build()))
          .isEqualTo(Joiner.on('\n').join(expectedOutputsListBuilder.build()));
    }
  }


  private static String getSoyUtilsPath() {
    return "testdata/javascript/soy_usegoog_lib.js";
  }


  /**
   * Data for a single print directive test to run in a JS interpreter.
   */
  static final class TestData {

    /** The expected output string. */
    final String expectedOutput;

    /** The print directive value as a JavaScript expression. */
    final String valueJs;

    /** The directive under test. */
    final SoyJsSrcPrintDirective directive;

    /** The print directive arguments as a javaScript expression. */
    final ImmutableList<String> directiveArgsJs;

    TestData(
        String expectedOutput, String valueJs, SoyJsSrcPrintDirective directive,
        List<String> directiveArgsJs) {
      this.expectedOutput = expectedOutput;
      this.valueJs = valueJs;
      this.directive = directive;
      this.directiveArgsJs = ImmutableList.copyOf(directiveArgsJs);
    }
  }

}
