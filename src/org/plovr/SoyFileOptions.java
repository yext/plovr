package org.plovr;

import java.util.List;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

/**
 * {@link SoyFileOptions} specifies the options to use when translating a Soy
 * file to JavaScript.
 *
 * @author bolinfest@gmail.com (Michael Bolin)
 */
final class SoyFileOptions {

  final List<String> pluginModuleNames;
  final boolean useClosureLibrary;
  final boolean isUsingInjectedData;
  final boolean soyGenerateGoogMsgs;

  public SoyFileOptions() {
    this(ImmutableList.<String>of(), /* pluginModuleNames */
        true, /* useClosureLibrary */
        false, /* isUsingInjectedData */
        false); /* soyGenerateGoogMsgs */
  }

  public SoyFileOptions(List<String> pluginModuleNames,
      boolean useClosureLibrary,
      boolean isUsingInjectedData,
      boolean soyGenerateGoogMsgs) {
    Preconditions.checkNotNull(pluginModuleNames);
    this.pluginModuleNames = ImmutableList.copyOf(pluginModuleNames);
    this.useClosureLibrary = useClosureLibrary;
    this.isUsingInjectedData = isUsingInjectedData;
    this.soyGenerateGoogMsgs = soyGenerateGoogMsgs;
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(
        pluginModuleNames, useClosureLibrary, isUsingInjectedData, soyGenerateGoogMsgs);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (!(obj instanceof SoyFileOptions)) {
      return false;
    }
    SoyFileOptions that = (SoyFileOptions)obj;
    return Objects.equal(this.pluginModuleNames, that.pluginModuleNames) &&
        Objects.equal(this.useClosureLibrary, that.useClosureLibrary) &&
        Objects.equal(this.isUsingInjectedData, that.isUsingInjectedData) &&
        Objects.equal(this.soyGenerateGoogMsgs, that.soyGenerateGoogMsgs);
  }
}
