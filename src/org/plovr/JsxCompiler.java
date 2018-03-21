package org.plovr;

import javax.script.Bindings;
import java.io.IOException;

public class JsxCompiler
    extends AbstractJavaScriptBasedCompiler<IOException> {

  /**
   * @return the singleton instance of the {@link JsxCompiler}
   */
  public static JsxCompiler getInstance() {
    return JsxCompilerHolder.instance;
  }

  private JsxCompiler() {
    super("org/plovr/babel.js");
  }

  @Override
  protected IOException generateExceptionFromMessage(String message) {
    return new IOException(message);
  }

  @Override
  protected String insertScopeVariablesAndGenerateExecutableJavaScript(Bindings compileScope, String sourceCode, String sourceName) {
    compileScope.put("input", sourceCode);
    return "Babel.transform(input, {presets: ['react'], plugins: [['transform-react-jsx', {useBuiltIns: true}]]}).code";
  }

  private static class JsxCompilerHolder {
    private static JsxCompiler instance = new JsxCompiler();
  }
}
