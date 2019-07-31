package org.plovr;

import java.io.File;

import org.plovr.io.Files;

public class JsxFile extends LocalFileJsInput {

  public JsxFile(String name, File source, File es6ImportRootDirectory) {
    super(name, source, es6ImportRootDirectory);
  }

  @Override
  public String generateCode() {
    try {
      File file = getSource();
      return replaceJsxImports(
          file.toPath(),
          JsxCompiler.getInstance().compile(Files.toString(file), getName()));
    } catch (Exception e) {
      throw new RuntimeException("Exception while compiling " + this.getSource().toURI(), e);
    }
  }
}
