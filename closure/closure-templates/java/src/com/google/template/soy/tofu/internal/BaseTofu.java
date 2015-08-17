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

package com.google.template.soy.tofu.internal;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Maps;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import com.google.template.soy.data.SanitizedContent;
import com.google.template.soy.data.SoyRecord;
import com.google.template.soy.data.SoyValueHelper;
import com.google.template.soy.data.UnsafeSanitizedContentOrdainer;
import com.google.template.soy.data.internalutils.NodeContentKinds;
import com.google.template.soy.error.ErrorReporter;
import com.google.template.soy.internal.base.Pair;
import com.google.template.soy.msgs.SoyMsgBundle;
import com.google.template.soy.msgs.internal.InsertMsgsVisitor;
import com.google.template.soy.parseinfo.SoyTemplateInfo;
import com.google.template.soy.shared.SoyCssRenamingMap;
import com.google.template.soy.shared.SoyIdRenamingMap;
import com.google.template.soy.shared.internal.ApiCallScopeUtils;
import com.google.template.soy.shared.internal.GuiceSimpleScope;
import com.google.template.soy.shared.internal.GuiceSimpleScope.WithScope;
import com.google.template.soy.shared.restricted.ApiCallScopeBindingAnnotations.ApiCall;
import com.google.template.soy.sharedpasses.FindIjParamsVisitor;
import com.google.template.soy.sharedpasses.FindIjParamsVisitor.IjParamsInfo;
import com.google.template.soy.sharedpasses.RenameCssVisitor;
import com.google.template.soy.sharedpasses.opti.SimplifyVisitor;
import com.google.template.soy.sharedpasses.render.RenderException;
import com.google.template.soy.sharedpasses.render.RenderVisitor;
import com.google.template.soy.soytree.SoyFileSetNode;
import com.google.template.soy.soytree.SoytreeUtils;
import com.google.template.soy.soytree.TemplateNode;
import com.google.template.soy.soytree.TemplateRegistry;
import com.google.template.soy.soytree.Visibility;
import com.google.template.soy.tofu.SoyTofu;
import com.google.template.soy.tofu.SoyTofuException;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

/**
 * Represents a compiled Soy file set. This is the result of compiling Soy to a Java object.
 *
 * <p> Important: Do not use outside of Soy code (treat as superpackage-private).
 *
 */
public class BaseTofu implements SoyTofu {


  /**
   * Injectable factory for creating an instance of this class.
   *
   * <p> Important: Do not use outside of Soy code (treat as superpackage-private).
   */
  public static interface BaseTofuFactory {

    /**
     * @param soyTree The Soy parse tree containing all the files in the Soy file set.
     * @param isCaching Whether this instance caches intermediate Soy trees after substitutions from
     *     the msgBundle and the cssRenamingMap.
     * @param errorReporter For reporting errors.
     */
    public BaseTofu create(SoyFileSetNode soyTree, boolean isCaching, ErrorReporter errorReporter);
  }


  /** Instance of SoyValueHelper to use. */
  private final SoyValueHelper valueHelper;

  /** The scope object that manages the API call scope. */
  private final GuiceSimpleScope apiCallScope;

  /** Factory for creating an instance of TofuRenderVisitor. */
  private final TofuRenderVisitorFactory tofuRenderVisitorFactory;

  /** The instanceof of SimplifyVisitor to use. */
  private final SimplifyVisitor simplifyVisitor;

  /** The Soy parse tree containing all the files in the Soy file set. */
  private final SoyFileSetNode soyTree;

  /** Whether this instance caches intermediate Soy trees after substitutions from the msgBundle
   *  and the cssRenamingMap. */
  private final boolean isCaching;

  /** Map of cached template registries. Only applicable when isCaching is true. */
  private final
  Map<Pair<SoyMsgBundle, SoyCssRenamingMap>, TemplateRegistry> cachedTemplateRegistries;

  /** The template registry used for no-caching mode of rendering. Applicable when isCaching is
   *  false or when isCaching is true but doAddToCache is false. */
  private final TemplateRegistry templateRegistryForNoCaching;

  /** Map from template node to injected params info for all templates. */
  private final ImmutableMap<TemplateNode, IjParamsInfo> templateToIjParamsInfoMap;

  /** For reporting errors. */
  private final ErrorReporter errorReporter;

  /**
   * @param valueHelper Instance of SoyValueHelper to use.
   * @param apiCallScope The scope object that manages the API call scope.
   * @param tofuRenderVisitorFactory Factory for creating an instance of TofuRenderVisitor.
   * @param simplifyVisitor The instance of SimplifyVisitor to use.
   * @param soyTree The Soy parse tree containing all the files in the Soy file set.
   * @param isCaching Whether this instance caches intermediate Soy trees after substitutions from
   *     the msgBundle and the cssRenamingMap.
   */
  @AssistedInject
  public BaseTofu(
      SoyValueHelper valueHelper,
      @ApiCall GuiceSimpleScope apiCallScope,
      TofuRenderVisitorFactory tofuRenderVisitorFactory,
      SimplifyVisitor simplifyVisitor,
      @Assisted SoyFileSetNode soyTree,
      @Assisted boolean isCaching,
      @Assisted ErrorReporter errorReporter) {

    this.valueHelper = valueHelper;
    this.apiCallScope = apiCallScope;
    this.tofuRenderVisitorFactory = tofuRenderVisitorFactory;
    this.simplifyVisitor = simplifyVisitor;
    this.soyTree = soyTree;
    this.isCaching = isCaching;
    this.errorReporter = errorReporter;

    if (isCaching) {
      cachedTemplateRegistries = Maps.newHashMap();
      addToCache(null, null);
    } else {
      cachedTemplateRegistries = null;
    }
    SoyFileSetNode soyTreeForNoCaching = SoytreeUtils.cloneNode(soyTree);
    templateRegistryForNoCaching = buildTemplateRegistry(soyTreeForNoCaching);
    templateToIjParamsInfoMap =
        new FindIjParamsVisitor(templateRegistryForNoCaching, errorReporter)
            .execOnAllTemplates(soyTreeForNoCaching);
  }


  /**
   * {@inheritDoc}
   *
   * <p> For objects of this class, the namespace is always null.
   */
  @Override public String getNamespace() {
    return null;
  }


  @Override public SoyTofu forNamespace(@Nullable String namespace) {
    return (namespace == null) ? this : new NamespacedTofu(this, namespace);
  }


  @Override public boolean isCaching() {
    return isCaching;
  }


  @Override public void addToCache(
      @Nullable SoyMsgBundle msgBundle, @Nullable SoyCssRenamingMap cssRenamingMap) {
    if (!isCaching) {
      throw new SoyTofuException("Cannot addToCache() when isCaching is false.");
    }

    try (WithScope withScope = apiCallScope.enter()) {
      ApiCallScopeUtils.seedSharedParams(apiCallScope, msgBundle);
      getCachedTemplateRegistry(Pair.of(msgBundle, cssRenamingMap), true);
    }
  }


  @Override public Renderer newRenderer(SoyTemplateInfo templateInfo) {
    return new RendererImpl(this, templateInfo.getName());
  }


  @Override public Renderer newRenderer(String templateName) {
    return new RendererImpl(this, templateName);
  }


  @Override public ImmutableSortedSet<String> getUsedIjParamsForTemplate(
      SoyTemplateInfo templateInfo) {
    return getUsedIjParamsForTemplate(templateInfo.getName());
  }


  @Override public ImmutableSortedSet<String> getUsedIjParamsForTemplate(String templateName) {
    TemplateNode template = templateRegistryForNoCaching.getBasicTemplate(templateName);
    if (template == null) {
      throw new SoyTofuException("Template '" + templateName + "' not found.");
    }
    IjParamsInfo ijParamsInfo = templateToIjParamsInfoMap.get(template);
    // TODO: Ideally we'd check that there are no external calls, but we find that in practice many
    // users have written templates that conditionally call to undefined templates. Instead,
    // we'll return a best effor set of what we have here, and over time, we'll encourage users to
    // enforce the "assertNoExternalCalls" flag.
    return ijParamsInfo.ijParamSet;
  }


  // -----------------------------------------------------------------------------------------------
  // Private methods.


  /**
   * Builds a template registry for the given Soy tree.
   * @param soyTree The Soy tree to build a template registry for.
   * @return The newly built template registry.
   */
  private TemplateRegistry buildTemplateRegistry(SoyFileSetNode soyTree) {
    return new TemplateRegistry(soyTree, errorReporter);
  }


  /**
   * Gets the template registry associated with the given key (a key is a pair of SoyMsgBundle and
   * SoyCssRenamingMap), optionally adding the mapping to the cache if it's not already there.
   *
   * <p> Specifically, if doAddToCache is true, then the mapping will be added to the cache if it's
   * not already there. Thus, after calling this method with doAddToCache set to true, the given key
   * is guaranteed to be found in the cache. On the other hand, if doAddToCache is false and the key
   * is not already in the cache, then this method simply returns null without modifying the cache.
   *
   * @param key The pair of SoyMsgBundle and SoyCssRenamingMap for which to retrieve the
   *     corresponding template registry.
   * @param doAddToCache Whether to add this combination to the cache in the case that it's not
   *     found in the cache.
   * @return The corresponding template registry, or null if not found in cache and doAddToCache is
   *     false.
   */
  private TemplateRegistry getCachedTemplateRegistry(
      Pair<SoyMsgBundle, SoyCssRenamingMap> key, boolean doAddToCache) {

    // This precondition check is for SimplifyVisitor, which we use below after making substitutions
    // from the SoyMsgBundle and SoyCssRenamingMap. While SimplifyVisitor will work correctly
    // outside of an active apiCallScope, always running it within the apiCallScope allows it to
    // potentially do more, such as apply bidi functions/directives that require bidiGlobalDir to be
    // in scope.
    Preconditions.checkState(apiCallScope.isActive());

    TemplateRegistry templateRegistry = cachedTemplateRegistries.get(key);
    if (templateRegistry == null) {
      if (!doAddToCache) {
        return null;
      }
      SoyFileSetNode soyTreeClone = SoytreeUtils.cloneNode(soyTree);
      new InsertMsgsVisitor(key.first, true /* dontErrorOnPlrselMsgs */ , errorReporter)
          .exec(soyTreeClone);
      new RenameCssVisitor(key.second, errorReporter)
          .exec(soyTreeClone);
      simplifyVisitor.exec(soyTreeClone);
      templateRegistry = buildTemplateRegistry(soyTreeClone);
      cachedTemplateRegistries.put(key, templateRegistry);
    }
    return templateRegistry;
  }


  /**
   * @param outputBuf The Appendable to write the output to.
   * @param templateName The full name of the template to render.
   * @param data The data to call the template with. Can be null if the template has no parameters.
   * @param ijData The injected data to call the template with. Can be null if not used.
   * @param activeDelPackageNames The set of active delegate package names, or null if none.
   * @param msgBundle The bundle of translated messages, or null to use the messages from the Soy
   *     source.
   * @param cssRenamingMap Map for renaming selectors in 'css' tags, or null if not used.
   * @param doAddToCache Whether to add the current combination of msgBundle and cssRenamingMap to
   *     the cache if it's not already there. If set to false, then falls back to the no-caching
   *     mode of rendering when not found in cache. Only applicable if isCaching is true for this
   *     BaseTofu instance.
   * @return The template that was rendered.
   */
  private TemplateNode renderMain(
      Appendable outputBuf, String templateName, @Nullable SoyRecord data,
      @Nullable SoyRecord ijData, @Nullable Set<String> activeDelPackageNames,
      @Nullable SoyMsgBundle msgBundle, @Nullable SoyIdRenamingMap idRenamingMap,
      @Nullable SoyCssRenamingMap cssRenamingMap,
      boolean doAddToCache) {

    if (activeDelPackageNames == null) {
      activeDelPackageNames = Collections.emptySet();
    }

    try (WithScope withScope = apiCallScope.enter()) {
      // Seed the scoped parameters.
      ApiCallScopeUtils.seedSharedParams(apiCallScope, msgBundle);

      // Do the rendering.
      TemplateRegistry cachedTemplateRegistry = isCaching ?
          getCachedTemplateRegistry(Pair.of(msgBundle, cssRenamingMap), doAddToCache) : null;
      // Note: cachedTemplateRegistry may be null even when isCaching is true (specifically, if
      // doAddToCache is false).
      if (cachedTemplateRegistry != null) {
        // Note: Still need to pass msgBundle because we currently don't cache plural/select msgs.
        return renderMainHelper(
            cachedTemplateRegistry, outputBuf, templateName, data, ijData, activeDelPackageNames,
            msgBundle, null, null);
      } else {
        return renderMainHelper(
            templateRegistryForNoCaching, outputBuf, templateName, data, ijData,
            activeDelPackageNames, msgBundle, idRenamingMap, cssRenamingMap);
      }
    }
  }


  /**
   * Renders a template and appends the result to a StringBuilder.
   *
   * @param templateRegistry A registry of all templates.
   * @param outputBuf The Appendable to append the rendered text to.
   * @param templateName The full name of the template to render.
   * @param data The data to call the template with. Can be null if the template has no parameters.
   * @param ijData The injected data to call the template with. Can be null if not used.
   * @param activeDelPackageNames The set of active delegate package names.
   * @param msgBundle The bundle of translated messages, or null to use the messages from the Soy
   *     source.
   * @param cssRenamingMap Map for renaming selectors in 'css' tags, or null if not used.
   * @return The template that was rendered.
   */
  private TemplateNode renderMainHelper(
      TemplateRegistry templateRegistry, Appendable outputBuf, String templateName,
      @Nullable SoyRecord data, @Nullable SoyRecord ijData, Set<String> activeDelPackageNames,
      @Nullable SoyMsgBundle msgBundle, @Nullable SoyIdRenamingMap idRenamingMap,
      @Nullable SoyCssRenamingMap cssRenamingMap) {

    TemplateNode template = templateRegistry.getBasicTemplate(templateName);
    if (template == null) {
      throw new SoyTofuException("Attempting to render undefined template '" + templateName + "'.");
    } else if (template.getVisibility() == Visibility.PRIVATE) {
      throw new SoyTofuException("Attempting to render private template '" + templateName + "'.");
    }

    if (data == null) {
      data = SoyValueHelper.EMPTY_DICT;
    }

    try {
      RenderVisitor rv = tofuRenderVisitorFactory.create(
          outputBuf,
          templateRegistry,
          data,
          errorReporter,
          ijData,
          activeDelPackageNames,
          msgBundle,
          idRenamingMap,
          cssRenamingMap);
      rv.exec(template);

    } catch (RenderException re) {
      throw new SoyTofuException(re);
    }

    return template;
  }


  // -----------------------------------------------------------------------------------------------
  // Renderer implementation.


  /**
   * Simple implementation of the Renderer interface.
   */
  private static class RendererImpl implements Renderer {

    private final BaseTofu baseTofu;
    private final String templateName;
    private SoyRecord data;
    private SoyRecord ijData;
    private SoyMsgBundle msgBundle;
    private SoyIdRenamingMap idRenamingMap;
    private SoyCssRenamingMap cssRenamingMap;
    private Set<String> activeDelPackageNames;
    private boolean doAddToCache;
    private SanitizedContent.ContentKind expectedContentKind;
    private boolean contentKindExplicitlySet;

    /**
     * @param baseTofu The underlying BaseTofu object used to perform the rendering.
     * @param templateName The full template name (including namespace).
     */
    public RendererImpl(BaseTofu baseTofu, String templateName) {
      this.baseTofu = baseTofu;
      this.templateName = templateName;
      this.data = null;
      this.ijData = null;
      this.activeDelPackageNames = null;
      this.msgBundle = null;
      this.cssRenamingMap = null;
      this.idRenamingMap = null;
      this.doAddToCache = true;
      this.expectedContentKind = SanitizedContent.ContentKind.HTML;
      this.contentKindExplicitlySet = false;
    }

    @Override public Renderer setData(Map<String, ?> data) {
      this.data =
          (data == null) ? null : baseTofu.valueHelper.newEasyDictFromJavaStringMap(data);
      return this;
    }

    @Override public Renderer setData(SoyRecord data) {
      this.data = data;
      return this;
    }

    @Override public Renderer setIjData(Map<String, ?> ijData) {
      this.ijData =
          (ijData == null) ? null : baseTofu.valueHelper.newEasyDictFromJavaStringMap(ijData);
      return this;
    }

    @Override public Renderer setIjData(SoyRecord ijData) {
      this.ijData = ijData;
      return this;
    }

    @Override public Renderer setActiveDelegatePackageNames(
        Set<String> activeDelegatePackageNames) {
      this.activeDelPackageNames = activeDelegatePackageNames;
      return this;
    }

    @Override public Renderer setMsgBundle(SoyMsgBundle msgBundle) {
      this.msgBundle = msgBundle;
      return this;
    }

    @Override public Renderer setIdRenamingMap(SoyIdRenamingMap idRenamingMap) {
      this.idRenamingMap = idRenamingMap;
      return this;
    }

    @Override public Renderer setCssRenamingMap(SoyCssRenamingMap cssRenamingMap) {
      this.cssRenamingMap = cssRenamingMap;
      return this;
    }

    @Override public Renderer setDontAddToCache(boolean dontAddToCache) {
      this.doAddToCache = !dontAddToCache;
      return this;
    }

    @Override public Renderer setContentKind(SanitizedContent.ContentKind contentKind) {
      this.expectedContentKind = Preconditions.checkNotNull(contentKind);
      this.contentKindExplicitlySet = true;
      return this;
    }

    @Override public String render() {
      StringBuilder sb = new StringBuilder();
      render(sb);
      return sb.toString();
    }

    @Override public SanitizedContent.ContentKind render(Appendable out) {
      TemplateNode template = baseTofu.renderMain(
          out, templateName, data, ijData, activeDelPackageNames, msgBundle, idRenamingMap,
          cssRenamingMap, doAddToCache);
      if (contentKindExplicitlySet || template.getContentKind() != null) {
        // Enforce the content kind if:
        // - The caller explicitly set a content kind to validate.
        // - The template is strict. This avoids accidentally using a text strict template in a
        // place where HTML was implicitly expected.
        enforceContentKind(template);
      }
      return template.getContentKind();
    }

    @Override public SanitizedContent renderStrict() {
      StringBuilder sb = new StringBuilder();
      TemplateNode template = baseTofu.renderMain(
          sb, templateName, data, ijData, activeDelPackageNames, msgBundle, idRenamingMap,
          cssRenamingMap, doAddToCache);
      enforceContentKind(template);
      // Use the expected instead of actual content kind; that way, if an HTML template is rendered
      // as TEXT, we will return TEXT.
      return UnsafeSanitizedContentOrdainer.ordainAsSafe(sb.toString(), expectedContentKind);
    }

    private void enforceContentKind(TemplateNode template) {
      if (expectedContentKind == SanitizedContent.ContentKind.TEXT) {
        // Allow any template to be called as text. This is consistent with the fact that
        // kind="text" templates can call any other template.
        return;
      }
      if (template.getContentKind() == null) {
        throw new SoyTofuException("Expected template to be autoescape=\"strict\" " +
            "but was autoescape=\"" + template.getAutoescapeMode().getAttributeValue() + "\": " +
            template.getTemplateName());
      }
      if (expectedContentKind != template.getContentKind()) {
        throw new SoyTofuException("Expected template to be kind=\"" +
            NodeContentKinds.toAttributeValue(expectedContentKind) +
            "\" but was kind=\"" + NodeContentKinds.toAttributeValue(template.getContentKind()) +
            "\": " + template.getTemplateName());
      }
    }
  }


  // -----------------------------------------------------------------------------------------------
  // Old render methods.

  @Deprecated
  @Override public String render(
      SoyTemplateInfo templateInfo, @Nullable SoyRecord data, @Nullable SoyMsgBundle msgBundle) {
    return (new RendererImpl(this, templateInfo.getName())).setData(data).setMsgBundle(msgBundle)
        .render();
  }


  @Deprecated
  @Override public String render(
      String templateName, @Nullable Map<String, ?> data, @Nullable SoyMsgBundle msgBundle) {
    return (new RendererImpl(this, templateName)).setData(data).setMsgBundle(msgBundle).render();
  }


  @Deprecated
  @Override public String render(
      String templateName, @Nullable SoyRecord data, @Nullable SoyMsgBundle msgBundle) {
    return (new RendererImpl(this, templateName)).setData(data).setMsgBundle(msgBundle).render();
  }

}
