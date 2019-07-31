package org.plovr;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.FileSystems;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.collect.ImmutableList;

import com.google.common.base.Objects;
import com.google.common.base.Throwables;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

/**
 * {@link LocalFileJsInput} represents a JavaScript input to the Closure
 * Compiler that can be read from a local file.
 *
 * @author bolinfest@gmail.com (Michael Bolin)
 */
public abstract class LocalFileJsInput extends AbstractJsInput {

  private static final Logger logger = Logger.getLogger(
      LocalFileJsInput.class.getName());

  private final File source;

  private long lastModified;

  protected File es6ImportRootDirectory;

  private static final CacheLoader<Key, List<JsInput>> fileLoader =
      new CacheLoader<Key, List<JsInput>>() {
    public List<JsInput> load(Key key) {
      File file = key.file;
      String name = key.name;
      String fileName = file.getName();
      File es6ImportRootDirectory = key.es6ImportRootDirectory;
      if (fileName.endsWith(".soy")) {
        if (key.soyFileOptions.useIncrementalDom) {
          if (key.soyFileOptions.disableNonIncrementalDom) {
            return ImmutableList.of(
                    new SoyFile(name, file, key.soyFileOptions, true)
            );
          } else {
            return ImmutableList.of(
                    new SoyFile(name, file, key.soyFileOptions, false),
                    new SoyFile(name, file, key.soyFileOptions, true)
            );
          }
        } else {
          return ImmutableList.of(
              new SoyFile(name, file, key.soyFileOptions, false)
          );
        }
      } else if (fileName.endsWith(".coffee")) {
        return ImmutableList.of(new CoffeeFile(name, file));
      } else if (fileName.endsWith(".ts")) {
        return ImmutableList.of(new TypeScriptFile(name, file));
      } else if (fileName.endsWith(".jsx")) {
        return ImmutableList.of(new JsxFile(name, file, es6ImportRootDirectory));
      } else {
        return ImmutableList.of(new JsSourceFile(name, file, es6ImportRootDirectory));
      }
    }
  };

  private static final LoadingCache<Key, List<JsInput>> jsInputCache =
      CacheBuilder.newBuilder().build(fileLoader);

  LocalFileJsInput(String name, File source) {
    this(name, source, null);
  }

  LocalFileJsInput(String name, File source, File es6ImportRootDirectory) {
    super(name);

    // TODO(bolinfest): Use java.nio to listen for updates to the underlying
    // file and invoke markDirty() if it changes. Upon doing so, remove the
    // hasInputChanged() method from the superclass.
    this.source = source;

    this.lastModified = source.lastModified();

    this.es6ImportRootDirectory = es6ImportRootDirectory;
  }

  private static final class Key {
    private final File file;
    private final String name;
    private final SoyFileOptions soyFileOptions;
    private final File es6ImportRootDirectory;

    Key(File file, String name, SoyFileOptions soyFileOptions, File es6ImportRootDirectory) {
      this.file = file;
      this.name = name;
      this.soyFileOptions = soyFileOptions;
      this.es6ImportRootDirectory = es6ImportRootDirectory;
    }

    @Override
    public int hashCode() {
      return Objects.hashCode(file, name, soyFileOptions, es6ImportRootDirectory);
    }

    @Override
    public boolean equals(Object obj) {
      if (!(obj instanceof Key)) {
        return false;
      }
      Key that = (Key) obj;
      return Objects.equal(this.file, that.file) &&
          Objects.equal(this.name, that.name) &&
          Objects.equal(this.soyFileOptions, that.soyFileOptions) &&
          Objects.equal(this.es6ImportRootDirectory, that.es6ImportRootDirectory);
    }
  }

  static List<JsInput> createForFileWithName(File file, String name,
      SoyFileOptions soyFileOptions, File es6ImportRootDirectory) {
    try {
      return jsInputCache.get(new Key(file, name, soyFileOptions, es6ImportRootDirectory));
    } catch (ExecutionException e) {
      throw Throwables.propagate(e);
    }
  }

  final protected File getSource() {
    return source;
  }

  @Override
  protected boolean hasInputChanged() {
    long currentlastModified = source.lastModified();
    if (currentlastModified != lastModified) {
      this.lastModified = currentlastModified;
      return true;
    }
    return false;
  }

  /**
   * Gets a normalized path name for the source. This is important because the
   * same file may be both an "input" and a "path" for a config, but it may
   * be referenced via different File names because of how relative paths
   * are resolved.
   * @return
   */
  protected String getCanonicalPath() {
    try {
      return source.getCanonicalPath();
    } catch (IOException e) {
      logger.log(Level.SEVERE, "Cannot get the canonical path for <" +
          source.getAbsolutePath() + "> what kind of file is this? ", e);
      return source.getAbsolutePath();
    }
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (!(obj instanceof LocalFileJsInput)) {
      return false;
    }
    LocalFileJsInput otherInput = (LocalFileJsInput)obj;
    return getCanonicalPath().equals(otherInput.getCanonicalPath());
  }

  @Override
  public int hashCode() {
    return getCanonicalPath().hashCode();
  }

  @Override
  public long getLastModified() {
    return source.lastModified();
  }

  private static final Pattern ES6_IMPORT =
    Pattern.compile("(?m)^import (?:[^;]*? from )['\"](/[^'\"]+)['\"];");

  protected String replaceJsxImports(Path sourcePath, String source) {
    if (es6ImportRootDirectory == null) {
      return source;
    }

    Matcher m = ES6_IMPORT.matcher(source);
    while (m.find()) {
      String importFilename = m.group(1);  // e.g. /js-spruce/yext/entitiesstorm/utils.js
      Path importPath = new File(es6ImportRootDirectory, importFilename.substring(1)).toPath();
      Path relImportPath = sourcePath.getParent().relativize(importPath);
      String relImportPathStr = relImportPath.toString();
      if (!relImportPathStr.startsWith(".")) {
        relImportPathStr = "./" + relImportPathStr;
      }
      source = source.replace(importFilename, relImportPathStr);
    }
    return source;
  }
}
