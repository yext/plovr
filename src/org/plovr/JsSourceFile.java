package org.plovr;

import java.io.File;
import java.io.IOException;

import org.plovr.io.Files;

/**
 * {@link JsSourceFile} represents a JavaScript file.
 *
 * @author bolinfest@gmail.com (Michael Bolin)
 */
public class JsSourceFile extends LocalFileJsInput {

  JsSourceFile(String name, File source, File es6ImportRootDirectory) {
    super(name, source, es6ImportRootDirectory);
  }

  @Override
  public String generateCode() {
    try {
      File file = getSource();
      if (es6ImportRootDirectory == null) {
          return Files.toString(file);
      }
      return replaceJsxImports(file.toPath(), Files.toString(file));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public boolean supportsEtags() {
    return true;
  }
}
