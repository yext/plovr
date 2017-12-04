package org.plovr;

import java.util.List;
import java.util.logging.Logger;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.javascript.jscomp.LoggerErrorManager;
import com.google.javascript.jscomp.deps.DependencyInfo;
import com.google.javascript.jscomp.deps.JsFileParser;

/**
 * {@link AbstractJsInput} provides the default logic for extracting
 * {@code goog.provide()} and {@code goog.require()} information.
 *
 * @author bolinfest@gmail.com (Michael Bolin)
 */
public abstract class AbstractJsInput implements JsInput {

  private static final JsFileParser FILE_PARSER =
      new JsFileParser(new LoggerErrorManager(Logger.getAnonymousLogger()));

  private final String name;

  private String code;
  private List<String> provides;
  private List<String> requires;

  AbstractJsInput(String name) {
    this.name = name;
  }

  /**
   * If the underlying file changes, then remove all cached information.
   */
  synchronized void markDirty() {
    provides = null;
    requires = null;
    code = null;
  }

  @Override
  public final String getCode() {
    cacheExpensiveValuesIfNecessary();
    return Preconditions.checkNotNull(code, getName());
  }

  protected abstract String generateCode();

  @Override
  public boolean supportsEtags() {
    return false;
  }

  @Override
  public CodeWithEtag getCodeWithEtag() {
    if (!supportsEtags()) {
      throw new UnsupportedOperationException(
          "This input does not know how to calculate its own ETags.");
    }

    String code = getCode();
    String eTag = calculateEtagFor(code);
    return new CodeWithEtag(code, eTag);
  }

  /**
   * Must return a stable ETag for the supplied code. Note that for some
   * JsInputs, such as Soy and CoffeeScript files, the ETag should be a
   * function of the source code (the .soy or .coffee rather than the JS) as
   * well as the options used to translate the source code to JS. For example,
   * if a user loads a Soy file and an ETag is returned based on the content of
   * the .soy, then the user modifies the Soy options in the plovr config and
   * reloads, then the generated JS as well as the ETag must be different to
   * reflect that change.
   */
  final protected String calculateEtagFor(String code) {
    // Consider creating a stronger eTag.
    // Note that an ETag must be quoted.
    return "\"" + Integer.toHexString(code.hashCode()) + "\"";
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public List<String> getProvides() {
    cacheExpensiveValuesIfNecessary();
    return Preconditions.checkNotNull(provides, getName());
  }

  @Override
  public List<String> getRequires() {
    cacheExpensiveValuesIfNecessary();
    return Preconditions.checkNotNull(requires, getName());
  }

  protected boolean hasInputChanged() {
    return false;
  }

  @Override
  public boolean isSoyFile() {
    return false;
  }

  @Override
  public String getTemplateCode() {
    throw new UnsupportedOperationException("This does not represent a Soy file");
  }

  private synchronized void cacheExpensiveValuesIfNecessary() {
    if (code != null && !hasInputChanged()) {
      return;
    }

    code = generateCode();

    DependencyInfo dependencyInfo = FILE_PARSER.parseFile(name, name, code);

    this.provides = ImmutableList.copyOf(dependencyInfo.getProvides());
    this.requires = ImmutableList.copyOf(dependencyInfo.getRequires());
  }

  @Override
  public String toString() {
    return name;
  }

}
