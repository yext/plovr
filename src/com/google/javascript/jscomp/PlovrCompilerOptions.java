package com.google.javascript.jscomp;

import org.plovr.ConfigTest;

/**
 * {@link PlovrCompilerOptions} is a subclass of {@link CompilerOptions} with
 * customizations for plovr. The need for the subclass is twofold:
 * <ol>
 *   <li>It contains compiler options that are specific to plovr, such as
 *       {@code globalScopeName} (though admittedly, {@code globalScopeName}
 *       will go away soon in favor of the Compiler's built-in
 *       {@link RescopeGlobalSymbols} pass).
 *   <li>It provides public accessors to properties that are ordinarily
 *       package-private, which is helpful during unit-testing. (See
 *       {@link ConfigTest}).
 * </ol>
 * Originally, these modifications were contained in plovr's local fork of
 * {@link CompilerOptions}, but this often created merge conflicts when syncing
 * plovr's Closure Compiler fork.
 *
 * @author bolinfest@gmail.com (Michael Bolin)
 */
@SuppressWarnings("serial")
public class PlovrCompilerOptions extends CompilerOptions {

  public boolean getProcessObjectPropertyString() {
    return processObjectPropertyString;
  }

  public CheckLevel getReportUnknownTypes() {
    return reportUnknownTypes;
  }

  public boolean getAcceptConstKeyword() {
    return acceptConstKeyword;
  }

  public String getOutputCharset() {
    return outputCharset;
  }
}
