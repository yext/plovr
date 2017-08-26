package org.plovr;

import java.util.List;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.template.soy.msgs.SoyMsgBundle;

/**
 * {@link SoyFileOptions} specifies the options to use when translating a Soy
 * file to JavaScript.
 *
 * @author bolinfest@gmail.com (Michael Bolin)
 */
final class SoyFileOptions {

  final List<String> pluginModuleNames;
  final boolean useClosureLibrary;
  final String protoFileDescriptors;
  final SoyMsgBundle msgBundle;
  final boolean soyGenerateGoogMsgs;

  public SoyFileOptions() {
    this(ImmutableList.<String>of(), /* pluginModuleNames */
        true, /* useClosureLibrary */
         "", /* protoFileDescriptors */
        null, /* msgBundle */
        false); /* soyGenerateGoogMsgs */
  }

  public SoyFileOptions(List<String> pluginModuleNames,
      boolean useClosureLibrary,
      String protoFileDescriptors) {
    this(pluginModuleNames, useClosureLibrary, protoFileDescriptors, null, false);
  }

  private SoyFileOptions(List<String> pluginModuleNames,
      boolean useClosureLibrary,
      String protoFileDescriptors,
      SoyMsgBundle msgBundle,
      boolean soyGenerateGoogMsgs) {
    Preconditions.checkNotNull(pluginModuleNames);
    this.pluginModuleNames = ImmutableList.copyOf(pluginModuleNames);
    this.useClosureLibrary = useClosureLibrary;
    this.protoFileDescriptors = protoFileDescriptors;;
    this.msgBundle = msgBundle;
    this.soyGenerateGoogMsgs = soyGenerateGoogMsgs;
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(
        pluginModuleNames, useClosureLibrary, protoFileDescriptors, msgBundle, soyGenerateGoogMsgs);
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
        Objects.equal(this.protoFileDescriptors, that.protoFileDescriptors) &&
        Objects.equal(this.msgBundle, that.msgBundle) &&
        Objects.equal(this.soyGenerateGoogMsgs, that.soyGenerateGoogMsgs);
  }

  public static class Builder {
    List<String> pluginModuleNames = ImmutableList.<String>of();
    boolean useClosureLibrary = false;
    String protoFileDescriptors = "";
    SoyMsgBundle msgBundle = null;
    boolean generateGoogMsgs = false;

    public Builder setPluginModuleNames(List<String> values) {
      pluginModuleNames = values;
      return this;
    }

    public Builder setUseClosureLibrary(boolean value) {
      useClosureLibrary = value;
      return this;
    }

    public Builder setProtoFileDescriptors(String value) {
      protoFileDescriptors = value;
      return this;
    }

    public Builder setMsgBundle(SoyMsgBundle value) {
      msgBundle = value;
      return this;
    }

    public Builder setGenerateGoogMsgs(boolean value) {
      generateGoogMsgs = value;
      return this;
    }

    public SoyFileOptions build() {
      return new SoyFileOptions(
          pluginModuleNames,
          useClosureLibrary,
          protoFileDescriptors,
          msgBundle,
          generateGoogMsgs);
    }
  }
}
