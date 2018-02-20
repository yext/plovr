package org.plovr;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

public class JsxCompilerTest {

  @Test
  public void testSimpleCompilation() throws IOException {

  String compiledJsx = JsxCompiler.getInstance().compile(
      "const element = <h1 className=\"greeting\">Hello, world!</h1>;",
      "testcase.jsx");
  assertEquals(
      "const element = React.createElement(\n" +
      "  \"h1\",\n" +
      "  { className: \"greeting\" },\n" +
      "  \"Hello, world!\"\n" +
      ");",
      compiledJsx);
  }
}
