/*
 * Copyright 2012 The Closure Compiler Authors.
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

/**
 * Tests {@link AngularPass}.
 */
public final class AngularPassTest extends Es6CompilerTestCase {

  public AngularPassTest() {
    super();
  }

  @Override
  protected CompilerPass getProcessor(Compiler compiler) {
    return new AngularPass(compiler);
  }

  @Override
  protected int getNumRepetitions() {
    // This pass only runs once.
    return 1;
  }

  @Override
  protected CompilerOptions getOptions() {
    CompilerOptions options = super.getOptions();
    // enables angularPass.
    options.angularPass = true;
    compareJsDoc = false;
    return options;
  }

  public void testNgInjectAddsInjectToFunctions() throws Exception {
    test("/** @ngInject */ function fn(a, b) {}",
        "function fn(a, b) {} fn['$inject']=['a', 'b']");

    testSame("function fn(a, b) {}");
  }

  public void testNgInjectAddsInjectAfterGoogInherits() throws Exception {
    test(
        LINE_JOINER.join(
            "/** @ngInject @constructor */ ",
            "function fn(a, b) {}",
            "goog.inherits(fn, parent);"),
        LINE_JOINER.join(
            "function fn(a, b) {}",
            "goog.inherits(fn, parent);",
            "fn['$inject']=['a', 'b']"));

    test(
        LINE_JOINER.join(
            "/** @ngInject @constructor */",
            "function fn(a, b) {}",
            "goog.inherits(fn, parent);",
            "var foo = 42;"),
        LINE_JOINER.join(
            "function fn(a, b) {}",
            "goog.inherits(fn, parent);",
            "fn['$inject']=['a', 'b'];",
            "var foo = 42;"));
  }

  public void testNgInjectAddsInjectToProps() throws Exception {
    test("var ns = {}; /** @ngInject */ ns.fn = function (a, b) {}",
         "var ns = {}; ns.fn = function (a, b) {}; ns.fn['$inject']=['a', 'b']");

    testSame("var ns = {}; ns.fn = function (a, b) {}");
  }

  public void testNgInjectAddsInjectToNestedProps() throws Exception {
    test(
        LINE_JOINER.join(
            "var ns = {}; ns.subns = {};",
            "/** @ngInject */ ns.subns.fn = function (a, b) {}"),
        LINE_JOINER.join(
            "var ns = {}; ns.subns = {};",
            "ns.subns.fn = function (a, b) {};",
            "ns.subns.fn['$inject']=['a', 'b']"));

    testSame("var ns = {}; ns.fn = function (a, b) {}");
  }

  public void testNgInjectAddsInjectToVars() throws Exception {
    test("/** @ngInject */ var fn = function (a, b) {}",
         "var fn = function (a, b) {}; fn['$inject']=['a', 'b']");

    testSame("var fn = function (a, b) {}");
  }

  public void testNgInjectAddsInjectToLet() throws Exception {
    testEs6("/** @ngInject */ let fn = function (a, b) {}",
         "let fn = function (a, b) {}; fn['$inject']=['a', 'b']");

    testSameEs6("let fn = function (a, b) {}");
  }

  public void testNgInjectAddsInjectToConst() throws Exception {
    testEs6("/** @ngInject */ const fn = function (a, b) {}",
         "const fn = function (a, b) {}; fn['$inject']=['a', 'b']");

    testSameEs6("const fn = function (a, b) {}");
  }


  public void testNgInjectAddsInjectToVarsWithChainedAssignment()
      throws Exception {
    test("var ns = {}; /** @ngInject */ var fn = ns.func = function (a, b) {}",
        LINE_JOINER.join(
            "var ns = {}; var fn = ns.func = function (a, b) {};",
            "fn['$inject']=['a', 'b']"));

    testSame("var ns = {}; var fn = ns.func = function (a, b) {}");
  }

  public void testNgInjectInBlock() throws Exception {
    test(
        LINE_JOINER.join(
            "(function() {",
            "  var ns = {};",
            "  /** @ngInject */ var fn = ns.func = function (a, b) {}",
            "})()"),
        LINE_JOINER.join(
            "(function() {",
            "  var ns = {}; var fn = ns.func = function (a, b) {};",
            "  fn['$inject']=['a', 'b']",
            "})()"));

    testSame(LINE_JOINER.join(
        "(function() {",
        "  var ns = {}; var fn = ns.func = function (a, b) {}",
        "})()"));
  }

  public void testNgInjectAddsToTheRightBlock() throws Exception {
    test(
        LINE_JOINER.join(
            "var fn = 10;",
            "(function() {",
            "  var ns = {};",
            "  /** @ngInject */ var fn = ns.func = function (a, b) {};",
            "})()"),
        LINE_JOINER.join(
            "var fn = 10;",
            "(function() {",
            "  var ns = {}; var fn = ns.func = function (a, b) {};",
            "  fn['$inject']=['a', 'b'];",
            "})()"));
  }

  public void testNgInjectInNonBlock() throws Exception {
    testError("function fake(){};" +
              "var ns = {};" +
              "fake( /** @ngInject */ ns.func = function (a, b) {} )",
              AngularPass.INJECT_IN_NON_GLOBAL_OR_BLOCK_ERROR);

    testError("/** @ngInject */( function (a, b) {} )",
              AngularPass.INJECT_IN_NON_GLOBAL_OR_BLOCK_ERROR);
  }

  public void testNgInjectNonFunction() throws Exception {
    testError("var ns = {}; ns.subns = {};" +
              "ns.subns.fake = function(x, y){};" +
              "/** @ngInject */ ns.subns.fake(1);",
              AngularPass.INJECT_NON_FUNCTION_ERROR);

    testError("/** @ngInject */ var a = 10",
              AngularPass.INJECT_NON_FUNCTION_ERROR);

    testError("/** @ngInject */ var x",
              AngularPass.INJECT_NON_FUNCTION_ERROR);

    testErrorEs6("class FnClass {constructor(a, b) {/** @ngInject */ this.x = 42}}",
        AngularPass.INJECT_NON_FUNCTION_ERROR);

    testErrorEs6("class FnClass {constructor(a, b) {/** @ngInject */ this.x}}",
        AngularPass.INJECT_NON_FUNCTION_ERROR);
  }

  public void testNgInjectAddsInjectToClass() throws Exception {
    testErrorEs6("/** @ngInject */ class FnClass {constructor(a, b) {}}",
        AngularPass.INJECT_NON_FUNCTION_ERROR);
  }

  public void testNgInjectAddsInjectToClassConstructor() throws Exception {
    testEs6("class FnClass {/** @ngInject */ constructor(a, b) {}}",
        "class FnClass{constructor(a, b){}}"
        + "FnClass['$inject'] = ['a', 'b'];");
  }

  public void testNgInjectAddsInjectToClassMethod1() throws Exception {
    testEs6(
        LINE_JOINER.join(
            "class FnClass {",
            "  constructor(a, b) {}",
            "  /** @ngInject */ ",
            "  methodA(c, d){}",
            "}"),
        LINE_JOINER.join(
            "class FnClass {",
            "  constructor(a, b){}",
            "  methodA(c, d){}",
            "}",
            "FnClass.prototype.methodA['$inject'] = ['c','d']"));
  }

  public void testNgInjectAddsInjectToClassMethod2() throws Exception {
    testEs6(
        LINE_JOINER.join(
            "FnClass.foo = class {",
            "  /** @ngInject */",
            "  constructor(a, b) {}",
            "};"),
        LINE_JOINER.join(
            "FnClass.foo = class {",
            "  constructor(a, b){}",
            "};",
            "FnClass.foo['$inject'] = ['a','b'];"));
  }

  public void testNgInjectAddsInjectToStaticMethod() throws Exception {
    testEs6(
        LINE_JOINER.join(
            "class FnClass {",
            "  constructor(a, b) {}",
            "  /** @ngInject */ ",
            "  static methodA(c, d) {}",
            "}"),
        LINE_JOINER.join(
            "class FnClass {",
            "  constructor(a, b) {}",
            "  static methodA(c, d) {}",
            "}",
            "FnClass.methodA['$inject'] = ['c','d']"));
  }

  public void testNgInjectAddsInjectToClassGenerator() throws Exception {
    testEs6(
        LINE_JOINER.join(
            "class FnClass {",
            "  constructor(a, b) {}",
            "  /** @ngInject */ ",
            "  * methodA(c, d){}",
            "}"),
        LINE_JOINER.join(
            "class FnClass {",
            "  constructor(a, b){}",
            "  *methodA(c, d){}",
            "}",
            "FnClass.prototype.methodA['$inject'] = ['c','d']"));
  }

  public void testNgInjectAddsInjectToClassMixOldStyle() throws Exception {
    testEs6(
        LINE_JOINER.join(
            "class FnClass {",
            "  constructor() {",
            "    /** @ngInject */ ",
            "    this.someMethod = function(a, b){}",
            "  }",
            "}"),
        LINE_JOINER.join(
            "class FnClass {",
            "  constructor() {",
            "    this.someMethod = function(a, b){}",
            "    this.someMethod['$inject'] = ['a','b']",
            "  }",
            "}"));
  }

  public void testNgInjectAddsInjectToClassArrowFunc() throws Exception {
    testEs6(
        LINE_JOINER.join(
            "class FnClass {",
            "  constructor() {",
            "    /** @ngInject */ ",
            "    this.someMethod = (a, b) => 42",
            "  }",
            "}"),
        LINE_JOINER.join(
            "class FnClass {",
            "  constructor() {",
            "    this.someMethod = (a, b) => 42",
            "    this.someMethod['$inject'] = ['a','b']",
            "  }",
            "}"));
  }

  public void testNgInjectAddsInjectToClassCompMethodName() throws Exception {
    testErrorEs6(
        LINE_JOINER.join(
            "class FnClass {",
            "  constructor() {}",
            "    /** @ngInject */ ",
            "  ['comp' + 'MethodName'](a, b){}",
            "}"),
        AngularPass.INJECT_NON_FUNCTION_ERROR);
  }

  public void testNgInjectToArrowFunctions() {
    testEs6("/** @ngInject */ var fn = (a, b, c)=>{};",
        "var fn = (a, b, c)=>{}; fn['$inject']=['a', 'b', 'c'];");
    testEs6("/** @ngInject */ var fn = ()=>{}",
            "var fn = ()=>{}");
  }

  public void testNgInjectToFunctionsWithDestructuredParam() {
    testErrorEs6("/** @ngInject */ function fn(a, {b, c}){}",
        AngularPass.INJECTED_FUNCTION_HAS_DESTRUCTURED_PARAM);
    testErrorEs6("/** @ngInject */ function fn(a, [b, c]){}",
        AngularPass.INJECTED_FUNCTION_HAS_DESTRUCTURED_PARAM);
    testErrorEs6("/** @ngInject */ function fn(a, {b, c}, d){}",
        AngularPass.INJECTED_FUNCTION_HAS_DESTRUCTURED_PARAM);
  }

  public void testNgInjectToFunctionsWithDefaultValue() {
    testErrorEs6("/** @ngInject */ function fn(a, b = 1){}",
        AngularPass.INJECTED_FUNCTION_HAS_DEFAULT_VALUE);
    testErrorEs6("/** @ngInject */ function fn(a, {b, c} = {b: 1, c: 2}){}",
        AngularPass.INJECTED_FUNCTION_HAS_DEFAULT_VALUE);
    testErrorEs6("/** @ngInject */ function fn(a, [b, c] = [1, 2]){}",
        AngularPass.INJECTED_FUNCTION_HAS_DEFAULT_VALUE);
  }
}
