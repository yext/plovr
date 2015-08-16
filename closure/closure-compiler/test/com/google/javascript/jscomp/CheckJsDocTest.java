/*
 * Copyright 2015 The Closure Compiler Authors.
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

package com.google.javascript.jscomp;

import static com.google.javascript.jscomp.CheckJSDoc.ANNOTATION_DEPRECATED;
import static com.google.javascript.jscomp.CheckJSDoc.ARROW_FUNCTION_AS_CONSTRUCTOR;
import static com.google.javascript.jscomp.CheckJSDoc.DISALLOWED_MEMBER_JSDOC;
import static com.google.javascript.jscomp.CheckJSDoc.MISPLACED_ANNOTATION;
import static com.google.javascript.jscomp.CheckJSDoc.MISPLACED_MSG_ANNOTATION;

/**
 * Tests for {@link CheckJSDoc}.
 *
 * @author chadkillingsworth@gmail.com (Chad Killingsworth)
 */

public final class CheckJsDocTest extends Es6CompilerTestCase {

  @Override
  protected CompilerPass getProcessor(final Compiler compiler) {
    return new CheckJSDoc(compiler);
  }

  @Override
  protected CompilerOptions getOptions() {
    CompilerOptions options = super.getOptions();
    options.setWarningLevel(
        DiagnosticGroups.MISPLACED_TYPE_ANNOTATION, CheckLevel.WARNING);
    return options;
  }

  public void testInvalidClassJsdoc() {
    testSameEs6("class Foo { /** @param {number} x */ constructor(x) {}}");

    testWarningEs6(
        "class Foo { /** @constructor */ constructor() {}}",
        DISALLOWED_MEMBER_JSDOC);

    testWarningEs6(
        "class Foo { /** @interface */ constructor() {}}",
        DISALLOWED_MEMBER_JSDOC);

    testWarningEs6(
        "class Foo { /** @extends {Foo} */ constructor() {}}",
        DISALLOWED_MEMBER_JSDOC);

    testWarningEs6(
        "class Foo { /** @implements {Foo} */ constructor() {}}",
        DISALLOWED_MEMBER_JSDOC);
  }


  public void testExposeDeprecated() {
    testWarning("/** @expose */ var x = 0;", ANNOTATION_DEPRECATED);
  }

  public void testJSDocFunctionNodeAttachment() {
    testWarning("var a = /** @param {number} index */5;"
        + "/** @return boolean */function f(index){}", MISPLACED_ANNOTATION);
  }

  public void testJSDocDescAttachment() {
    testWarning(
        "function f() { return /** @type {string} */ (g(1 /** @desc x */)); };",
        MISPLACED_MSG_ANNOTATION);

    testWarning("/** @desc Foo. */ var bar = goog.getMsg('hello');",
        MISPLACED_MSG_ANNOTATION);
    testWarning("/** @desc Foo. */ x.y.z.bar = goog.getMsg('hello');",
        MISPLACED_MSG_ANNOTATION);
    testWarning("var msgs = {/** @desc x */ x: goog.getMsg('x')}",
        MISPLACED_MSG_ANNOTATION);
    testWarning("/** @desc Foo. */ bar = goog.getMsg('x');",
        MISPLACED_MSG_ANNOTATION);
  }

  public void testJSDocTypeAttachment() {
    testWarning(
        "function f() {  /** @type {string} */ if (true) return; };", MISPLACED_ANNOTATION);

    testWarning(
        "function f() {  /** @type {string} */  return; };", MISPLACED_ANNOTATION);
  }


  public void testMisplacedTypeAnnotation1() {
    // misuse with COMMA
    testWarning(
        "var o = {}; /** @type {string} */ o.prop1 = 1, o.prop2 = 2;", MISPLACED_ANNOTATION);
  }

  public void testMisplacedTypeAnnotation2() {
    // missing parentheses for the cast.
    testWarning(
        "var o = /** @type {string} */ getValue();",
        MISPLACED_ANNOTATION);
  }

  public void testMisplacedTypeAnnotation3() {
    // missing parentheses for the cast.
    testWarning(
        "var o = 1 + /** @type {string} */ value;",
        MISPLACED_ANNOTATION);
  }

  public void testMisplacedTypeAnnotation4() {
    // missing parentheses for the cast.
    testWarning(
        "var o = /** @type {!Array.<string>} */ ['hello', 'you'];",
        MISPLACED_ANNOTATION);
  }

  public void testMisplacedTypeAnnotation5() {
    // missing parentheses for the cast.
    testWarning(
        "var o = (/** @type {!Foo} */ {});",
        MISPLACED_ANNOTATION);
  }

  public void testMisplacedTypeAnnotation6() {
    testWarning(
        "var o = /** @type {function():string} */ function() {return 'str';}",
        MISPLACED_ANNOTATION);
  }

  public void testMisplacedTypeAnnotation7() {
    testWarning(
        "var x = /** @type {string} */ y;",
        MISPLACED_ANNOTATION);
  }

  public void testAllowedNocollapseAnnotation1() {
    testSame("var foo = {}; /** @nocollapse */ foo.bar = true;");
  }

  public void testAllowedNocollapseAnnotation2() {
    testSame(
        "/** @constructor */ function Foo() {};\n"
        + "var ns = {};\n"
        + "/** @nocollapse */ ns.bar = Foo.prototype.blah;");
  }

  public void testMisplacedNocollapseAnnotation1() {
    testWarning(
        "/** @constructor */ function foo() {};"
            + "/** @nocollapse */ foo.prototype.bar = function() {};",
        MISPLACED_ANNOTATION);
  }

  public void testNocollapseInExterns() {
    testSame("var foo = {}; /** @nocollapse */ foo.bar = true;",
        "foo.bar;", MISPLACED_ANNOTATION);
  }

  public void testArrowFuncAsConstructor() {
    testErrorEs6("/** @constructor */ var a = ()=>{}; var b = a();",
        ARROW_FUNCTION_AS_CONSTRUCTOR);
    testErrorEs6("var a = /** @constructor */ ()=>{}; var b = a();",
        ARROW_FUNCTION_AS_CONSTRUCTOR);
    testErrorEs6("/** @constructor */ let a = ()=>{}; var b = a();",
        ARROW_FUNCTION_AS_CONSTRUCTOR);
    testErrorEs6("/** @constructor */ const a = ()=>{}; var b = a();",
        ARROW_FUNCTION_AS_CONSTRUCTOR);
    testErrorEs6("var a; /** @constructor */ a = ()=>{}; var b = a();",
        ARROW_FUNCTION_AS_CONSTRUCTOR);
  }
}
