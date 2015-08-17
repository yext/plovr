/*
 * Copyright 2008 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.template.soy.soytree;

import static com.google.template.soy.soytree.AutoescapeMode.parseAutoEscapeMode;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.template.soy.base.SourceLocation;
import com.google.template.soy.base.internal.BaseUtils;
import com.google.template.soy.base.internal.SoyFileKind;
import com.google.template.soy.basetree.CopyState;
import com.google.template.soy.basetree.SyntaxVersion;
import com.google.template.soy.basetree.SyntaxVersionBound;
import com.google.template.soy.error.ErrorReporter;
import com.google.template.soy.error.SoyError;
import com.google.template.soy.soytree.CommandTextAttributesParser.Attribute;
import com.google.template.soy.soytree.SoyNode.SplitLevelTopNode;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nullable;

/**
 * Node representing a Soy file.
 *
 * <p> Important: Do not use outside of Soy code (treat as superpackage-private).
 *
 */
public final class SoyFileNode extends AbstractParentSoyNode<TemplateNode>
    implements SplitLevelTopNode<TemplateNode> {

  private static final SoyError INVALID_DELPACKAGE_NAME =
      SoyError.of("Invalid delegate package name ''{0}''.");

  private static final SoyError INVALID_NAMESPACE_COMMAND_TEXT =
      SoyError.of("Invalid namespace command text ''{0}''.");

  private static final SoyError INVALID_NAMESPACE_NAME =
      SoyError.of("Invalid namespace name ''{0}''.");

  private static final SoyError NAMESPACE_ALIAS_LITERALLY_NAMED_AS =
      SoyError.of("Invalid namespace alias ''as'' for namespace ''{0}''.");

  private static final SoyError INVALID_ALIAS_FOR_LAST_PART_OF_NAMESPACE =
      SoyError.of("Not allowed to alias the last part of the file''s namespace ({0}) "
          + "to another namespace ({1}).");

  private static final SoyError DIFFERENT_NAMESPACES_WITH_SAME_ALIAS =
      SoyError.of("Found two namespaces with the same alias (''{0}'' and ''{1}'').");

  /** Pattern for pre-trimmed 'namespace' cmd text. Group 1 = namespace, group 2 = attributes. */
  private static final Pattern NAMESPACE_CMD_TEXT_PATTERN = Pattern.compile(
      "(" + BaseUtils.DOTTED_IDENT_RE + ") (\\s .*)?", Pattern.COMMENTS | Pattern.DOTALL);

  /** Pattern for pre-trimmed 'alias' cmd text. Group 1 = namespace, group 2 = alias (or null). */
  private static final Pattern ALIAS_CMD_TEXT_PATTERN = Pattern.compile(
      "(" + BaseUtils.DOTTED_IDENT_RE + ") (?: \\s+ as \\s+ (" + BaseUtils.IDENT_RE + ") )?",
      Pattern.COMMENTS);

  /** The default autoescape mode if none is specified in the command text. */
  private static final AutoescapeMode DEFAULT_FILE_WIDE_DEFAULT_AUTOESCAPE_MODE =
      AutoescapeMode.STRICT;

  /** Parser for the command text besides the namespace. */
  private static final CommandTextAttributesParser ATTRIBUTES_PARSER =
      new CommandTextAttributesParser("namespace",
          new Attribute("autoescape", AutoescapeMode.getAttributeValues(),
              DEFAULT_FILE_WIDE_DEFAULT_AUTOESCAPE_MODE.getAttributeValue()),
          new Attribute("requirecss", Attribute.ALLOW_ALL_VALUES, null),
          new Attribute("cssbase", Attribute.ALLOW_ALL_VALUES, null));

  public static final Predicate<SoyFileNode> MATCH_SRC_FILENODE = new Predicate<SoyFileNode>() {
    @Override
    public boolean apply(@Nullable SoyFileNode input) {
      return input != null && input.getSoyFileKind() == SoyFileKind.SRC;
    }
  };

  /** The kind of this Soy file */
  private final SoyFileKind soyFileKind;

  /** The name of the containing delegate package, or null if none. */
  @Nullable private final String delPackageName;

  /** This Soy file's namespace, or null if syntax version V1. */
  @Nullable private final String namespace;

  /** The default autoescape mode declared by this file's namespace, if any. */
  private final Optional<AutoescapeMode> namespaceAutoescapeMode;

  /** CSS namespaces required by this file (usable in any template in this file). */
  private final ImmutableList<String> requiredCssNamespaces;

  /** CSS base package for package-relative selectors. */
  private final String cssBaseNamespace;

  /** Map from aliases to namespaces for this file. */
  private final ImmutableMap<String, String> aliasToNamespaceMap;

  /** This Soy file's name (null if not supplied). */
  @Nullable private String fileName;


  /**
   * @param id The id for this node.
   * @param filePath The path to the Soy source file.
   * @param soyFileKind The kind of this Soy file.
   * @param errorReporter For reporting syntax errors.
   * @param delpackageCmdText This Soy file's delegate package, or null if none.
   * @param namespaceCmdText This Soy file's namespace and attributes. Nullable for backwards
   *     compatibility only.
   * @param aliasCmdTexts The command texts of the 'alias' declarations. Allowed to be null.
   */
  public SoyFileNode(
      int id,
      String filePath,
      SoyFileKind soyFileKind,
      ErrorReporter errorReporter,
      @Nullable String delpackageCmdText,
      @Nullable String namespaceCmdText,
      @Nullable List<String> aliasCmdTexts) {
    super(id, new SourceLocation(filePath));

    this.soyFileKind = soyFileKind;

    if (delpackageCmdText != null) {
      this.delPackageName = delpackageCmdText;
      if (!BaseUtils.isDottedIdentifier(delPackageName)) {
        errorReporter.report(getSourceLocation(), INVALID_DELPACKAGE_NAME, delPackageName);
      }
    } else {
      this.delPackageName = null;
    }

    // TODO(user): aliases, delpackages, and namespaces should be represented directly
    // in the AST, instead of being a part of SoyFileNode.
    String namespace = null;
    AutoescapeMode defaultAutoescapeMode = null;
    ImmutableList<String> requiredCssNamespaces = ImmutableList.of();
    String cssBaseNamespace = null;

    if (namespaceCmdText != null) {
      Matcher nctMatcher = NAMESPACE_CMD_TEXT_PATTERN.matcher(namespaceCmdText);
      if (nctMatcher.matches()) {
        namespace = nctMatcher.group(1);
        String attributeText = nctMatcher.group(2);
        if (attributeText != null) {
          attributeText = attributeText.trim();
          Map<String, String> attributes = ATTRIBUTES_PARSER.parse(
              attributeText, errorReporter, getSourceLocation());
          if (attributes.containsKey("autoescape")) {
            defaultAutoescapeMode = parseAutoEscapeMode(
                attributes.get("autoescape"), getSourceLocation(), errorReporter);
          }
          if (attributes.containsKey("requirecss")) {
            requiredCssNamespaces =
                RequirecssUtils.parseRequirecssAttr(attributes.get("requirecss"));
          }
          if (attributes.containsKey("cssbase")) {
            cssBaseNamespace = attributes.get("cssbase");
          }
        }
      } else {
        errorReporter.report(
            getSourceLocation(), INVALID_NAMESPACE_COMMAND_TEXT, namespaceCmdText);
      }
    }

    this.namespace = namespace;
    this.namespaceAutoescapeMode = Optional.fromNullable(defaultAutoescapeMode);
    this.requiredCssNamespaces = requiredCssNamespaces;
    this.cssBaseNamespace = cssBaseNamespace;
    if (namespace == null) {
      maybeSetSyntaxVersionBound(new SyntaxVersionBound(
          SyntaxVersion.V2_0, "Soy V2 files must have a namespace declaration."));
    } else if (!BaseUtils.isDottedIdentifier(namespace)) {
      errorReporter.report(getSourceLocation(), INVALID_NAMESPACE_NAME, namespace);
    }

    if (aliasCmdTexts != null) {
      Preconditions.checkNotNull(this.namespace);
      String aliasForFileNamespace = BaseUtils.extractPartAfterLastDot(this.namespace);
      Map<String, String> tempAliasToNamespaceMap = Maps.newLinkedHashMap();
      for (String aliasCmdText : aliasCmdTexts) {
        Matcher actMatcher = ALIAS_CMD_TEXT_PATTERN.matcher(aliasCmdText);
        Preconditions.checkArgument(actMatcher.matches());
        String aliasNamespace = actMatcher.group(1);
        Preconditions.checkArgument(BaseUtils.isDottedIdentifier(aliasNamespace));
        String alias = actMatcher.group(2) != null ?
            actMatcher.group(2) : BaseUtils.extractPartAfterLastDot(aliasNamespace);
        if (alias.equals("as")) {
          errorReporter.report(
              getSourceLocation(),
              NAMESPACE_ALIAS_LITERALLY_NAMED_AS,
              aliasNamespace);
        }
        if (alias.equals(aliasForFileNamespace) && !aliasNamespace.equals(this.namespace)) {
          errorReporter.report(
              getSourceLocation(),
              INVALID_ALIAS_FOR_LAST_PART_OF_NAMESPACE,
              this.namespace,
              aliasNamespace);
        }
        if (tempAliasToNamespaceMap.containsKey(alias)) {
          errorReporter.report(
              getSourceLocation(),
              DIFFERENT_NAMESPACES_WITH_SAME_ALIAS,
              tempAliasToNamespaceMap.get(alias), aliasNamespace);
        }
        tempAliasToNamespaceMap.put(alias, aliasNamespace);
      }
      aliasToNamespaceMap = ImmutableMap.copyOf(tempAliasToNamespaceMap);
    } else {
      aliasToNamespaceMap = ImmutableMap.of();
    }
  }


  /**
   * Copy constructor.
   * @param orig The node to copy.
   */
  private SoyFileNode(SoyFileNode orig, CopyState copyState) {
    super(orig, copyState);
    this.soyFileKind = orig.soyFileKind;
    this.delPackageName = orig.delPackageName;
    this.namespace = orig.namespace;
    this.namespaceAutoescapeMode = orig.namespaceAutoescapeMode;
    this.requiredCssNamespaces = orig.requiredCssNamespaces;  // immutable
    this.cssBaseNamespace = orig.cssBaseNamespace;
    this.aliasToNamespaceMap = orig.aliasToNamespaceMap;  // immutable
    this.fileName = orig.fileName;
  }


  @Override public Kind getKind() {
    return Kind.SOY_FILE_NODE;
  }


  /** Returns the kind of this Soy file. */
  public SoyFileKind getSoyFileKind() {
    return soyFileKind;
  }


  /** Returns the name of the containing delegate package, or null if none. */
  @Nullable public String getDelPackageName() {
    return delPackageName;
  }


  /** Returns this Soy file's namespace, or null if syntax version V1. */
  @Nullable public String getNamespace() {
    return namespace;
  }


  /** Returns the default autoescaping mode for contained templates. */
  public AutoescapeMode getDefaultAutoescapeMode() {
    return namespaceAutoescapeMode.or(DEFAULT_FILE_WIDE_DEFAULT_AUTOESCAPE_MODE);
  }


  /** Returns the CSS namespaces required by this file (usable in any template in this file). */
  public ImmutableList<String> getRequiredCssNamespaces() {
    return requiredCssNamespaces;
  }


  /** Returns the CSS base namespace for this file (usable in any template in this file). */
  public String getCssBaseNamespace() {
    return cssBaseNamespace;
  }


  /** Returns the map from aliases to namespaces for this file. */
  public ImmutableMap<String, String> getAliasToNamespaceMap() {
    return aliasToNamespaceMap;
  }


  /** Returns the path to the source Soy file ("unknown" if not supplied). */
  public String getFilePath() {
    return getSourceLocation().getFilePath();
  }


  /** Returns this Soy file's name (null if not supplied). */
  @Nullable public String getFileName() {
    return getSourceLocation().getFileName();
  }


  @Override public String toSourceString() {

    StringBuilder sb = new StringBuilder();

    if (delPackageName != null) {
      sb.append("{delpackage ").append(delPackageName).append("}\n");
    }
    if (namespace != null) {
      sb.append("{namespace ").append(namespace);
      if (this.namespaceAutoescapeMode.isPresent()) {
        sb.append(" autoescape=\"")
            .append(this.namespaceAutoescapeMode.get().getAttributeValue())
            .append("\"");
      }
      sb.append("}\n");
    }

    if (!aliasToNamespaceMap.isEmpty()) {
      sb.append("\n");
      for (Map.Entry<String, String> entry : aliasToNamespaceMap.entrySet()) {
        String alias = entry.getKey();
        String aliasNamespace = entry.getValue();
        if (aliasNamespace.equals(alias) || aliasNamespace.endsWith("." + alias)) {
          sb.append("{alias ").append(aliasNamespace).append("}\n");
        } else {
          sb.append("{alias ").append(aliasNamespace).append(" as ").append(alias).append("}\n");
        }
      }
    }

    for (SoyNode child : getChildren()) {
      sb.append("\n");
      sb.append(child.toSourceString());
    }

    return sb.toString();
  }


  @Override public SoyFileSetNode getParent() {
    return (SoyFileSetNode) super.getParent();
  }


  @Override public SoyFileNode copy(CopyState copyState) {
    return new SoyFileNode(this, copyState);
  }

}
