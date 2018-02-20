package org.plovr;

import java.io.File;

import org.plovr.io.Files;

public class JsxFile extends LocalFileJsInput {

  public JsxFile(String name, File source) {
    super(name, source);
  }

  @Override
  public String generateCode() {
    try {
      return JsxCompiler.getInstance().compile(
          Files.toString(getSource()), getName());
    } catch (Exception e) {
      throw new RuntimeException("Exception while compiling " + this.getSource().toURI(), e);
    }
  }
}
