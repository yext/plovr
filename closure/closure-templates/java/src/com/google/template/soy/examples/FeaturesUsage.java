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

package com.google.template.soy.examples;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.io.Resources;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.template.soy.SoyFileSet;
import com.google.template.soy.SoyModule;
import com.google.template.soy.data.SoyListData;
import com.google.template.soy.data.SoyMapData;
import com.google.template.soy.examples.FeaturesSoyInfo.DemoAutoescapeTrueSoyTemplateInfo;
import com.google.template.soy.examples.FeaturesSoyInfo.DemoBidiSupportSoyTemplateInfo;
import com.google.template.soy.examples.FeaturesSoyInfo.DemoCallWithParamBlockSoyTemplateInfo;
import com.google.template.soy.examples.FeaturesSoyInfo.DemoCallWithParamSoyTemplateInfo;
import com.google.template.soy.examples.FeaturesSoyInfo.DemoCallWithoutParamSoyTemplateInfo;
import com.google.template.soy.examples.FeaturesSoyInfo.DemoDoubleBracesSoyTemplateInfo;
import com.google.template.soy.examples.FeaturesSoyInfo.DemoExpressionsSoyTemplateInfo;
import com.google.template.soy.examples.FeaturesSoyInfo.DemoForSoyTemplateInfo;
import com.google.template.soy.examples.FeaturesSoyInfo.DemoForeachSoyTemplateInfo;
import com.google.template.soy.examples.FeaturesSoyInfo.DemoMsgSoyTemplateInfo;
import com.google.template.soy.examples.FeaturesSoyInfo.DemoPrintSoyTemplateInfo;

import static com.google.template.soy.examples.FeaturesSoyInfo.DEMO_AUTOESCAPE_TRUE;
import static com.google.template.soy.examples.FeaturesSoyInfo.DEMO_BIDI_SUPPORT;
import static com.google.template.soy.examples.FeaturesSoyInfo.DEMO_CALL_WITHOUT_PARAM;
import static com.google.template.soy.examples.FeaturesSoyInfo.DEMO_CALL_WITH_PARAM;
import static com.google.template.soy.examples.FeaturesSoyInfo.DEMO_CALL_WITH_PARAM_BLOCK;
import static com.google.template.soy.examples.FeaturesSoyInfo.DEMO_COMMENTS;
import static com.google.template.soy.examples.FeaturesSoyInfo.DEMO_DOUBLE_BRACES;
import static com.google.template.soy.examples.FeaturesSoyInfo.DEMO_EXPRESSIONS;
import static com.google.template.soy.examples.FeaturesSoyInfo.DEMO_FOR;
import static com.google.template.soy.examples.FeaturesSoyInfo.DEMO_FOREACH;
import static com.google.template.soy.examples.FeaturesSoyInfo.DEMO_IF;
import static com.google.template.soy.examples.FeaturesSoyInfo.DEMO_LINE_JOINING;
import static com.google.template.soy.examples.FeaturesSoyInfo.DEMO_MSG;
import static com.google.template.soy.examples.FeaturesSoyInfo.DEMO_PRINT;
import static com.google.template.soy.examples.FeaturesSoyInfo.DEMO_RAW_TEXT_COMMANDS;
import static com.google.template.soy.examples.FeaturesSoyInfo.DEMO_SWITCH;
import com.google.template.soy.msgs.SoyMsgBundle;
import com.google.template.soy.msgs.SoyMsgBundleHandler;
import com.google.template.soy.tofu.SoyTofu;
import com.google.template.soy.xliffmsgplugin.XliffMsgPluginModule;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import java.io.IOException;
import java.net.URL;


/**
 * Usage of the features examples.
 *
 */
public class FeaturesUsage {

  private FeaturesUsage() {}


  /** The string to prepend to the usage message generated by Flags. */
  private static final String USAGE_PREFIX =
      "Usage:\n" +
      "java com.google.template.soy.examples.FeaturesUsage [-locale <locale>]\n";

  /** Prefix for XLIFF resource (ends with [locale].xlf). */
  private static final String XLIFF_RESOURCE_PREFIX = "examples_translated_";

  @Option(name = "-locale",
          usage = "The locale to render templates in. The corresponding XLIFF resource " +
                  XLIFF_RESOURCE_PREFIX + "<locale>.xlf must exist. If not provided, the" +
                  " messages from the Soy source will be used.")
  private String locale = "";


  /** Counter for the number of examples written so far. */
  private int numExamples = 0;


  /**
   * Prints the generated HTML to stdout.
   * @param args May contain flags.
   * @throws IOException If there is an error reading the input files.
   */
  public static void main(String[] args) throws IOException {
    (new FeaturesUsage()).execMain(args);
  }


  private void execMain(String[] args) throws IOException {

    CmdLineParser cmdLineParser = new CmdLineParser(this);
    cmdLineParser.setUsageWidth(100);
    try {
      cmdLineParser.parseArgument(args);

    } catch(CmdLineException cle) {
      System.err.println("\nError: " + cle.getMessage() + "\n\n");
      System.err.println(USAGE_PREFIX);
      cmdLineParser.printUsage(System.err);
      System.exit(1);
    }

    Injector injector = Guice.createInjector(new SoyModule(), new XliffMsgPluginModule());

    SoyFileSet.Builder sfsBuilder = injector.getInstance(SoyFileSet.Builder.class);
    SoyFileSet sfs = sfsBuilder
        .add(Resources.getResource("simple.soy"))
        .add(Resources.getResource("features.soy"))
        .setCompileTimeGlobals(Resources.getResource("FeaturesUsage_globals.txt"))
        .build();
    SoyTofu tofu = sfs.compileToTofu().forNamespace("soy.examples.features");

    SoyMsgBundle msgBundle;
    if (locale.length() > 0) {
      // Use translations from an XLIFF file.
      SoyMsgBundleHandler msgBundleHandler = injector.getInstance(SoyMsgBundleHandler.class);
      URL xliffResource = Resources.getResource(XLIFF_RESOURCE_PREFIX + locale + ".xlf");
      msgBundle = msgBundleHandler.createFromResource(xliffResource);
      if (msgBundle.getLocaleString() == null) {
        throw new IOException(
            "Error reading message resource \"" + XLIFF_RESOURCE_PREFIX + locale + ".xlf\".");
      }
    } else {
      // Use the messages from the Soy source files.
      msgBundle = null;
    }

    // Note: In the examples below, I sometimes use the version of render() that takes a SoyMapData
    // and sometimes use the version that takes a Map<String, ?>. They both work. The version that
    // takes a SoyMapData is more efficient if you need to reuse the same template data object for
    // multiple calls of render() (because the version that takes a Map<String, ?> internally
    // converts it to a new SoyMapData on every call).

    writeExampleHeader("demoComments");
    System.out.println(tofu.newRenderer(DEMO_COMMENTS).setMsgBundle(msgBundle).render());

    writeExampleHeader("demoLineJoining");
    System.out.println(tofu.newRenderer(DEMO_LINE_JOINING).setMsgBundle(msgBundle).render());

    writeExampleHeader("demoRawTextCommands");
    System.out.println(tofu.newRenderer(DEMO_RAW_TEXT_COMMANDS).setMsgBundle(msgBundle).render());

    writeExampleHeader("demoPrint");
    System.out.println(tofu.newRenderer(DEMO_PRINT)
        .setData(new SoyMapData(DemoPrintSoyTemplateInfo.BOO, "Boo!",
            DemoPrintSoyTemplateInfo.TWO, 2))
        .setMsgBundle(msgBundle)
        .render());

    writeExampleHeader("demoAutoescapeTrue");
    System.out.println(tofu.newRenderer(DEMO_AUTOESCAPE_TRUE)
        .setData(new SoyMapData(DemoAutoescapeTrueSoyTemplateInfo.ITALIC_HTML, "<i>italic</i>"))
        .setMsgBundle(msgBundle)
        .render());

    writeExampleHeader("demoMsg");
    System.out.println(tofu.newRenderer(DEMO_MSG)
        .setData(ImmutableMap.of(DemoMsgSoyTemplateInfo.NAME, "Ed",
                                 DemoMsgSoyTemplateInfo.LABS_URL, "http://labs.google.com"))
        .setMsgBundle(msgBundle)
        .render());

    writeExampleHeader("demoIf");
    System.out.println(tofu.newRenderer(DEMO_IF)
        .setData(new SoyMapData("pi", 3.14159)).setMsgBundle(msgBundle).render());
    System.out.println(tofu.newRenderer(DEMO_IF)
        .setData(new SoyMapData("pi", 2.71828)).setMsgBundle(msgBundle).render());
    System.out.println(tofu.newRenderer(DEMO_IF)
        .setData(new SoyMapData("pi", 1.61803)).setMsgBundle(msgBundle).render());

    writeExampleHeader("demoSwitch");
    System.out.println(tofu.newRenderer(DEMO_SWITCH)
        .setData(ImmutableMap.of("name", "Fay")).setMsgBundle(msgBundle).render());
    System.out.println(tofu.newRenderer(DEMO_SWITCH)
        .setData(ImmutableMap.of("name", "Go")).setMsgBundle(msgBundle).render());
    System.out.println(tofu.newRenderer(DEMO_SWITCH)
        .setData(ImmutableMap.of("name", "Hal")).setMsgBundle(msgBundle).render());
    System.out.println(tofu.newRenderer(DEMO_SWITCH)
        .setData(ImmutableMap.of("name", "Ivy")).setMsgBundle(msgBundle).render());

    writeExampleHeader("demoForeach");
    SoyListData persons = new SoyListData();
    persons.add(new SoyMapData("name", "Jen", "numWaffles", 1));
    persons.add(new SoyMapData("name", "Kai", "numWaffles", 3));
    persons.add(new SoyMapData("name", "Lex", "numWaffles", 1));
    persons.add(new SoyMapData("name", "Mel", "numWaffles", 2));
    System.out.println(tofu.newRenderer(DEMO_FOREACH)
        .setData(new SoyMapData(DemoForeachSoyTemplateInfo.PERSONS, persons))
        .setMsgBundle(msgBundle)
        .render());

    writeExampleHeader("demoFor");
    System.out.println(tofu.newRenderer(DEMO_FOR)
        .setData(new SoyMapData(DemoForSoyTemplateInfo.NUM_LINES, 3))
        .setMsgBundle(msgBundle)
        .render());

    writeExampleHeader("demoCallWithoutParam");
    System.out.println(tofu.newRenderer(DEMO_CALL_WITHOUT_PARAM)
        .setData(new SoyMapData(
                     DemoCallWithoutParamSoyTemplateInfo.NAME, "Neo",
                     DemoCallWithoutParamSoyTemplateInfo.TRIP_INFO,
                         new SoyMapData("name", "Neo", "destination", "The Matrix")))
        .setMsgBundle(msgBundle)
        .render());

    writeExampleHeader("demoCallWithParam");
    System.out.println(tofu.newRenderer(DEMO_CALL_WITH_PARAM)
        .setData(ImmutableMap.of(
                 DemoCallWithParamSoyTemplateInfo.NAME, "Oz",
                 DemoCallWithParamSoyTemplateInfo.COMPANION_NAME, "Pip",
                 DemoCallWithParamSoyTemplateInfo.DESTINATIONS,
                 ImmutableList.of("Gillikin Country", "Munchkin Country",
                                  "Quadling Country", "Winkie Country")))
        .setMsgBundle(msgBundle)
        .render());

    writeExampleHeader("demoCallWithParamBlock");
    System.out.println(tofu.newRenderer(DEMO_CALL_WITH_PARAM_BLOCK)
        .setData(new SoyMapData(DemoCallWithParamBlockSoyTemplateInfo.NAME, "Quo"))
        .setMsgBundle(msgBundle)
        .render());

    writeExampleHeader("demoExpressions");
    SoyListData students = new SoyListData();
    students.add(new SoyMapData("name", "Rob", "major", "Physics", "year", 1999));
    students.add(new SoyMapData("name", "Sha", "major", "Finance", "year", 1980));
    students.add(new SoyMapData("name", "Tim", "major", "Engineering", "year", 2005));
    students.add(new SoyMapData("name", "Uma", "major", "Biology", "year", 1972));
    System.out.println(tofu.newRenderer(DEMO_EXPRESSIONS)
        .setData(new SoyMapData(DemoExpressionsSoyTemplateInfo.STUDENTS, students,
                                DemoExpressionsSoyTemplateInfo.CURRENT_YEAR, 2008))
        .setMsgBundle(msgBundle)
        .render());

    writeExampleHeader("demoDoubleBraces");
    System.out.println(tofu.newRenderer(DEMO_DOUBLE_BRACES)
        .setData(ImmutableMap.of(DemoDoubleBracesSoyTemplateInfo.SET_NAME, "prime numbers",
                                 DemoDoubleBracesSoyTemplateInfo.SET_MEMBERS,
                                     ImmutableList.of(2, 3, 5, 7, 11, 13)))
         .setMsgBundle(msgBundle)
         .render());

    // The Hebrew in the following example comes out as question marks in the output because
    // System.out (and by default stdout generally) is set up to use a Latin encoding. To see
    // this really in action, run the Javascript example.
    writeExampleHeader("demoBidiSupport");
    System.out.println(tofu.newRenderer(DEMO_BIDI_SUPPORT)
        .setData(ImmutableMap.of(DemoBidiSupportSoyTemplateInfo.TITLE, "2008: A BiDi Odyssey",
                                 DemoBidiSupportSoyTemplateInfo.AUTHOR, "John Doe, Esq.",
                                 DemoBidiSupportSoyTemplateInfo.YEAR, "1973",
                                 DemoBidiSupportSoyTemplateInfo.KEYWORDS,
                                 ImmutableList.of(
                                     "Bi(Di)",
                                     "2008 (\u05E9\u05E0\u05D4)",
                                     "2008 (year)")))
        .setMsgBundle(msgBundle)
        .render());
  }


  /**
   * Private helper to write the header for each example.
   * @param exampleName The name of the example.
   */
  private void writeExampleHeader(String exampleName) {
    numExamples++;
    System.out.println(
        "--------------------------------------------------------------------------------");
    System.out.printf("[%d. %s]\n", numExamples, exampleName);
  }

}
