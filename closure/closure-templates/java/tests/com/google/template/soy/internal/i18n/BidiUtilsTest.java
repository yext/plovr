/*
 * Copyright 2007 Google Inc.
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

package com.google.template.soy.internal.i18n;

import com.google.template.soy.data.Dir;

import com.ibm.icu.lang.UCharacter;
import junit.framework.TestCase;

/**
 * Test cases for BidiUtils
 */
public class BidiUtilsTest extends TestCase {
  private static final String LRE = "\u202A";
  private static final String RLE = "\u202B";
  private static final String PDF = "\u202C";
  private static final String LRO = "\u202D";
  private static final String RLO = "\u202E";
  private static final String BN = "\u200B";

  private static final String HE = "\u05D0";  // Hebrew character. RTL.
  private static final String HAN = "\u9910";  // Chinese character. LTR.
  private static final String FA = "\u06AF";  // Persian character. RTL.
  private static final String AR = "\u0627";  // Arabic character. RTL.

  private static final String TIMES = "\u00D7";  // Multiplication sign (bidi class ON).
  private static final String NBSP = "\u00A0";  // Non-breaking space (bidi class CS).
  private static final String MINUS = "\u2212";  // Mathematical minus (*not* a hyphen).
  private static final String HYPHEN = "\u2010";  // Hyphen (*not* a minus).
  private static final String ALT_PLUS = "\uFB29";  // "Alternative" Hebrew plus sign.

  private static final Dir LTR = Dir.LTR;
  private static final Dir RTL = Dir.RTL;
  private static final Dir NEUTRAL = Dir.NEUTRAL;

  public void testLanguageDir() {
    assertEquals(RTL, BidiUtils.languageDir("he"));
    assertEquals(RTL, BidiUtils.languageDir("iw"));
    assertEquals(RTL, BidiUtils.languageDir("ar"));
    assertEquals(RTL, BidiUtils.languageDir("fa"));
    assertEquals(RTL, BidiUtils.languageDir("FA"));
    assertEquals(RTL, BidiUtils.languageDir("ar-EG"));
    assertEquals(RTL, BidiUtils.languageDir("Ar-eg"));
    assertEquals(RTL, BidiUtils.languageDir("az-Arab"));
    assertEquals(RTL, BidiUtils.languageDir("az-Arab-IR"));
    assertEquals(RTL, BidiUtils.languageDir("az-ARAB-IR"));
    assertEquals(RTL, BidiUtils.languageDir("az_arab_IR"));
    assertEquals(LTR, BidiUtils.languageDir("es"));
    assertEquals(LTR, BidiUtils.languageDir("zh-CN"));
    assertEquals(LTR, BidiUtils.languageDir("fil"));
    assertEquals(LTR, BidiUtils.languageDir("az"));
    assertEquals(LTR, BidiUtils.languageDir("iw-Latn"));
    assertEquals(LTR, BidiUtils.languageDir("iw-LATN"));
    assertEquals(LTR, BidiUtils.languageDir("iw-latn"));
  }

  public void testDirectionalityEstimator_dirTypeOps() {
    BidiUtils.DirectionalityEstimator de = new BidiUtils.DirectionalityEstimator(
        "my my \uD835\uDFCE\uD840\uDC00!", false);
    assertEquals(UCharacter.DIRECTIONALITY_LEFT_TO_RIGHT, de.dirTypeForward());
    assertEquals(UCharacter.DIRECTIONALITY_LEFT_TO_RIGHT, de.dirTypeForward());
    assertEquals(UCharacter.DIRECTIONALITY_WHITESPACE, de.dirTypeForward());
    assertEquals(UCharacter.DIRECTIONALITY_LEFT_TO_RIGHT, de.dirTypeForward());
    assertEquals(UCharacter.DIRECTIONALITY_LEFT_TO_RIGHT, de.dirTypeForward());
    assertEquals(UCharacter.DIRECTIONALITY_WHITESPACE, de.dirTypeForward());
    assertEquals(UCharacter.DIRECTIONALITY_EUROPEAN_NUMBER, de.dirTypeForward());
    assertEquals(UCharacter.DIRECTIONALITY_LEFT_TO_RIGHT, de.dirTypeForward());
    assertEquals(UCharacter.DIRECTIONALITY_OTHER_NEUTRALS, de.dirTypeForward());
    try {
      de.dirTypeForward();  // Should throw.
      assertTrue(false);
    } catch (IndexOutOfBoundsException e) {
    }

    assertEquals(UCharacter.DIRECTIONALITY_OTHER_NEUTRALS, de.dirTypeBackward());
    assertEquals(UCharacter.DIRECTIONALITY_LEFT_TO_RIGHT, de.dirTypeBackward());
    assertEquals(UCharacter.DIRECTIONALITY_EUROPEAN_NUMBER, de.dirTypeBackward());
    assertEquals(UCharacter.DIRECTIONALITY_WHITESPACE, de.dirTypeBackward());
    assertEquals(UCharacter.DIRECTIONALITY_LEFT_TO_RIGHT, de.dirTypeBackward());
    assertEquals(UCharacter.DIRECTIONALITY_LEFT_TO_RIGHT, de.dirTypeBackward());
    assertEquals(UCharacter.DIRECTIONALITY_WHITESPACE, de.dirTypeBackward());
    assertEquals(UCharacter.DIRECTIONALITY_LEFT_TO_RIGHT, de.dirTypeBackward());
    assertEquals(UCharacter.DIRECTIONALITY_LEFT_TO_RIGHT, de.dirTypeBackward());
    try {
      de.dirTypeBackward();  // Should throw.
      assertTrue(false);
    } catch (IndexOutOfBoundsException e) {
    }
  }

  public void testDirectionalityEstimator_dirTypeOpsHtml() {
    BidiUtils.DirectionalityEstimator de = new BidiUtils.DirectionalityEstimator(
        "<span x='>" + HE + "'>my&ensp;\uD835\uDFCE!</span>;>", true);
    assertEquals(UCharacter.DIRECTIONALITY_BOUNDARY_NEUTRAL, de.dirTypeForward());
    assertEquals(UCharacter.DIRECTIONALITY_LEFT_TO_RIGHT, de.dirTypeForward());
    assertEquals(UCharacter.DIRECTIONALITY_LEFT_TO_RIGHT, de.dirTypeForward());
    assertEquals(UCharacter.DIRECTIONALITY_WHITESPACE, de.dirTypeForward());
    assertEquals(UCharacter.DIRECTIONALITY_EUROPEAN_NUMBER, de.dirTypeForward());
    assertEquals(UCharacter.DIRECTIONALITY_OTHER_NEUTRALS, de.dirTypeForward());
    assertEquals(UCharacter.DIRECTIONALITY_BOUNDARY_NEUTRAL, de.dirTypeForward());
    assertEquals(UCharacter.DIRECTIONALITY_OTHER_NEUTRALS, de.dirTypeForward());
    assertEquals(UCharacter.DIRECTIONALITY_OTHER_NEUTRALS, de.dirTypeForward());
    try {
      de.dirTypeForward();  // Should throw.
      assertTrue(false);
    } catch (IndexOutOfBoundsException e) {
    }

    assertEquals(UCharacter.DIRECTIONALITY_OTHER_NEUTRALS, de.dirTypeBackward());
    assertEquals(UCharacter.DIRECTIONALITY_OTHER_NEUTRALS, de.dirTypeBackward());
    assertEquals(UCharacter.DIRECTIONALITY_BOUNDARY_NEUTRAL, de.dirTypeBackward());
    assertEquals(UCharacter.DIRECTIONALITY_OTHER_NEUTRALS, de.dirTypeBackward());
    assertEquals(UCharacter.DIRECTIONALITY_EUROPEAN_NUMBER, de.dirTypeBackward());
    assertEquals(UCharacter.DIRECTIONALITY_WHITESPACE, de.dirTypeBackward());
    assertEquals(UCharacter.DIRECTIONALITY_LEFT_TO_RIGHT, de.dirTypeBackward());
    assertEquals(UCharacter.DIRECTIONALITY_LEFT_TO_RIGHT, de.dirTypeBackward());
    assertEquals(UCharacter.DIRECTIONALITY_BOUNDARY_NEUTRAL, de.dirTypeBackward());
    try {
      de.dirTypeBackward();  // Should throw.
      assertTrue(false);
    } catch (IndexOutOfBoundsException e) {
    }
  }

  public void testHasAnyLtr() {
    assertFalse(BidiUtils.hasAnyLtr(""));
    assertFalse(BidiUtils.hasAnyLtr("123\t... \n"));
    assertFalse(BidiUtils.hasAnyLtr(HE + HE + HE));
    assertTrue(BidiUtils.hasAnyLtr(HE + "z" + HE + HE));

    // LRE/RLE/LRO/RLO/PDF are ignored.
    assertFalse(BidiUtils.hasAnyLtr(LRE + PDF));
    assertFalse(BidiUtils.hasAnyLtr(LRE + HE + PDF));
    assertFalse(BidiUtils.hasAnyLtr(LRE + RLE + HE + PDF + PDF));
    assertFalse(BidiUtils.hasAnyLtr(LRO + PDF));
    assertFalse(BidiUtils.hasAnyLtr(LRO + HE + PDF));
    assertFalse(BidiUtils.hasAnyLtr(LRO + RLE + HE + PDF + PDF));
    assertTrue(BidiUtils.hasAnyLtr(RLE + "x" + PDF));
    assertTrue(BidiUtils.hasAnyLtr(RLE + LRE + "x" + PDF + PDF));
    assertTrue(BidiUtils.hasAnyLtr(RLO + "x" + PDF));
    assertTrue(BidiUtils.hasAnyLtr(RLO + LRE + "x" + PDF + PDF));

    assertTrue(BidiUtils.hasAnyLtr(RLE + HE + PDF + "x"));
    assertTrue(BidiUtils.hasAnyLtr(RLE + RLE + HE + PDF + PDF + "x"));
    assertTrue(BidiUtils.hasAnyLtr(RLO + HE + PDF + "x"));
    assertTrue(BidiUtils.hasAnyLtr(RLO + RLO + HE + PDF + PDF + "x"));

    assertTrue(BidiUtils.hasAnyLtr("<nasty title='a'>" + HE, false));
    assertFalse(BidiUtils.hasAnyLtr("<nasty title='a'>" + HE, true));
  }

  public void testHasAnyRtl() {
    assertFalse(BidiUtils.hasAnyRtl(""));
    assertFalse(BidiUtils.hasAnyRtl("123\t... \n"));
    assertFalse(BidiUtils.hasAnyRtl("abc"));
    assertTrue(BidiUtils.hasAnyRtl("ab" + HE + "c"));

    // LRE/RLE/LRO/RLO/PDF are ignored.
    assertFalse(BidiUtils.hasAnyRtl(RLE + PDF));
    assertFalse(BidiUtils.hasAnyRtl(RLE + "x" + PDF));
    assertFalse(BidiUtils.hasAnyRtl(RLE + LRE + "x" + PDF + PDF));
    assertFalse(BidiUtils.hasAnyRtl(RLO + PDF));
    assertFalse(BidiUtils.hasAnyRtl(RLO + "x" + PDF));
    assertFalse(BidiUtils.hasAnyRtl(RLO + LRE + "x" + PDF + PDF));
    assertTrue(BidiUtils.hasAnyRtl(LRE + HE + PDF));
    assertTrue(BidiUtils.hasAnyRtl(LRE + RLE + HE + PDF + PDF));
    assertTrue(BidiUtils.hasAnyRtl(LRO + HE + PDF));
    assertTrue(BidiUtils.hasAnyRtl(LRO + RLE + HE + PDF + PDF));

    assertTrue(BidiUtils.hasAnyRtl(LRE + "x" + PDF + HE));
    assertTrue(BidiUtils.hasAnyRtl(LRE + LRE + "x" + PDF + PDF + HE));
    assertTrue(BidiUtils.hasAnyRtl(LRO + "x" + PDF + HE));
    assertTrue(BidiUtils.hasAnyRtl(LRO + LRO + "x" + PDF + PDF + HE));

    assertTrue(BidiUtils.hasAnyRtl("<nasty title='" + HE + "'>a", false));
    assertFalse(BidiUtils.hasAnyRtl("<nasty title='" + HE + "'>a", true));
  }

  public void testGetUnicodeDir_NeutralText() {
    assertEquals(NEUTRAL, BidiUtils.getUnicodeDir(""));
    assertEquals(NEUTRAL, BidiUtils.getUnicodeDir("\t   \r\n"));
    assertEquals(NEUTRAL, BidiUtils.getUnicodeDir("123"));
    assertEquals(NEUTRAL, BidiUtils.getUnicodeDir(" 123-()"));
  }

  public void testGetUnicodeDir_LtrFirst() {
    assertEquals(LTR, BidiUtils.getUnicodeDir("\t   a"));
    assertEquals(LTR, BidiUtils.getUnicodeDir("\t   a " + HE));
  }

  public void testGetUnicodeDir_RtlFirst() {
    assertEquals(RTL, BidiUtils.getUnicodeDir("\t   " + HE));
    assertEquals(RTL, BidiUtils.getUnicodeDir("\t   " + HE + " a"));
  }

  public void testGetUnicodeDir_IgnoreEmbeddings() {
    assertEquals(LTR, BidiUtils.getUnicodeDir(RLE + PDF + "x"));
    assertEquals(RTL, BidiUtils.getUnicodeDir(LRE + HE + PDF + "x"));
    assertEquals(LTR, BidiUtils.getUnicodeDir(RLO + PDF + "x"));
    assertEquals(RTL, BidiUtils.getUnicodeDir(LRO + HE + PDF + "x"));

    assertEquals(RTL, BidiUtils.getUnicodeDir(LRE + PDF + HE));
    assertEquals(LTR, BidiUtils.getUnicodeDir(RLE + "x" + PDF + HE));
    assertEquals(RTL, BidiUtils.getUnicodeDir(LRO + PDF + HE));
    assertEquals(LTR, BidiUtils.getUnicodeDir(RLO + "x" + PDF + HE));
  }

  public void testGetUnicodeDirOfHtml_MarkupSkipped() {
    assertEquals(LTR, BidiUtils.getUnicodeDir("<a tag>" + HE));
    assertEquals(RTL, BidiUtils.getUnicodeDir("<a tag>" + HE, true));
    assertEquals(RTL, BidiUtils.getUnicodeDir("<a x=\"y>\" tag>" + HE, true));
    assertEquals(RTL, BidiUtils.getUnicodeDir("<a x=\"<y>\" tag>" + HE, true));
    assertEquals(RTL, BidiUtils.getUnicodeDir("<a x='<y>' tag>" + HE, true));
    assertEquals(LTR, BidiUtils.getUnicodeDir("<a x=\"<y>\" tag>a" + HE, true));
    assertEquals(RTL, BidiUtils.getUnicodeDir("<a x=\"<y>\" tag><b>" + HE, true));

    assertEquals(LTR, BidiUtils.getUnicodeDir("<notatag", true));
  }

  public void testGetUnicodeDirOfHtml_EntitySkipped() {
    assertEquals(NEUTRAL, BidiUtils.getUnicodeDir("&nbsp;", true));

    // TODO: Uncomment these lines and rename test to ...Parsed() when we start to map entities to
    // the characters for which they stand.
    // assertEquals(RTL, BidiUtils.getUnicodeDir("&nbsp;&rlm;", true));
    // assertEquals(LTR, BidiUtils.getUnicodeDir("&nbsp;a&rlm;", true));
    // assertEquals(LTR, BidiUtils.getUnicodeDir("&rlm;"));
    // assertEquals(RTL, BidiUtils.getUnicodeDir("&rlm;", true));
    // assertEquals(LTR, BidiUtils.getUnicodeDir("&nosuchentity;", true));
  }

  public void testGetEntryDir_NeutralText() {
    assertEquals(NEUTRAL, BidiUtils.getEntryDir(""));
    assertEquals(NEUTRAL, BidiUtils.getEntryDir("\t   \r\n"));
    assertEquals(NEUTRAL, BidiUtils.getEntryDir("123"));
    assertEquals(NEUTRAL, BidiUtils.getEntryDir(" 123-()"));
  }

  public void testGetEntryDir_LtrFirst() {
    assertEquals(LTR, BidiUtils.getEntryDir("a"));
    assertEquals(LTR, BidiUtils.getEntryDir("\t   a"));
    assertEquals(LTR, BidiUtils.getEntryDir("\t   abc"));
    assertEquals(LTR, BidiUtils.getEntryDir("\t   a " + HE));
    assertEquals(LTR, BidiUtils.getEntryDir("\t   a" + HE));
    assertEquals(LTR, BidiUtils.getEntryDir("\t   a " + HE + " " + HE + HE));
    assertEquals(LTR, BidiUtils.getEntryDir("http://www.google.com " + HE));
  }

  public void testGetEntryDir_RtlFirst() {
    assertEquals(RTL, BidiUtils.getEntryDir(HE));
    assertEquals(RTL, BidiUtils.getEntryDir("\t   " + HE));
    assertEquals(RTL, BidiUtils.getEntryDir("\t   " + HE + HE + HE));
    assertEquals(RTL, BidiUtils.getEntryDir("\t   " + HE + " a"));
    assertEquals(RTL, BidiUtils.getEntryDir("\t   " + HE + "a"));
    assertEquals(RTL, BidiUtils.getEntryDir("\t   " + HE + " a abc"));
  }

  public void testGetEntryDir_EmptyEmbeddingIgnored() {
    assertEquals(LTR, BidiUtils.getEntryDir(RLE + PDF + "a"));
    assertEquals(LTR, BidiUtils.getEntryDir(RLE + BN + PDF + "a"));
    assertEquals(LTR, BidiUtils.getEntryDir(RLO + PDF + "a"));
    assertEquals(LTR, BidiUtils.getEntryDir(RLO + BN + PDF + "a"));
    assertEquals(RTL, BidiUtils.getEntryDir(LRE + PDF + HE));
    assertEquals(RTL, BidiUtils.getEntryDir(LRE + BN + PDF + HE));
    assertEquals(RTL, BidiUtils.getEntryDir(LRO + PDF + HE));
    assertEquals(RTL, BidiUtils.getEntryDir(LRO + BN + PDF + HE));
  }

  public void testGetEntryDir_NonEmptyLtrEmbeddingFirst() {
    assertEquals(LTR, BidiUtils.getEntryDir(LRE + "." + PDF + HE));
    assertEquals(LTR, BidiUtils.getEntryDir(LRE + HE + PDF + HE));
    assertEquals(LTR, BidiUtils.getEntryDir(
        RLE + PDF + LRE + HE + PDF + HE));
    assertEquals(LTR, BidiUtils.getEntryDir(
        LRE + RLE + PDF + HE + PDF + HE));
    assertEquals(LTR, BidiUtils.getEntryDir(
        LRE + HE + RLE + HE + PDF + PDF + HE));
    assertEquals(LTR, BidiUtils.getEntryDir(
        RLE + LRE + HE + PDF + HE + PDF + HE));
    assertEquals(LTR, BidiUtils.getEntryDir(
        RLE + LRE + RLE + PDF + HE + PDF + HE + PDF + HE));
    assertEquals(LTR, BidiUtils.getEntryDir(
        RLE + LRE + RLE + BN + RLO + BN + PDF + BN + PDF + RLE + PDF + RLE + PDF + HE
        + PDF + HE + PDF + HE));
    assertEquals(LTR, BidiUtils.getEntryDir(LRO + "." + PDF + HE));
    assertEquals(LTR, BidiUtils.getEntryDir(LRO + HE + PDF + HE));
    assertEquals(LTR, BidiUtils.getEntryDir(
        RLE + PDF + LRO + HE + PDF + HE));
    assertEquals(LTR, BidiUtils.getEntryDir(
        LRO + RLE + PDF + HE + PDF + HE));
    assertEquals(LTR, BidiUtils.getEntryDir(
        LRO + HE + RLE + HE + PDF + PDF + HE));
    assertEquals(LTR, BidiUtils.getEntryDir(
        RLE + LRO + HE + PDF + HE + PDF + HE));
    assertEquals(LTR, BidiUtils.getEntryDir(
        RLE + LRO + RLE + PDF + HE + PDF + HE + PDF + HE));
    assertEquals(LTR, BidiUtils.getEntryDir(
        RLE + LRO + RLE + BN + RLO + BN + PDF + BN + PDF + RLE + PDF + RLE + PDF + HE
        + PDF + HE + PDF + HE));
  }

  public void testGetEntryDir_NonEmptyRtlEmbeddingFirst() {
    assertEquals(RTL, BidiUtils.getEntryDir(RLE + "." + PDF + "a"));
    assertEquals(RTL, BidiUtils.getEntryDir(RLE + "a" + PDF + "a"));
    assertEquals(RTL, BidiUtils.getEntryDir(LRE + PDF + RLE + "a" + PDF + "a"));
    assertEquals(RTL, BidiUtils.getEntryDir(RLE + LRE + PDF + "a" + PDF + "a"));
    assertEquals(RTL, BidiUtils.getEntryDir(RLE + "a" + LRE + "a" + PDF + PDF + "a"));
    assertEquals(RTL, BidiUtils.getEntryDir(LRE + RLE + "a" + PDF + "a" + PDF + "a"));
    assertEquals(RTL, BidiUtils.getEntryDir(
        LRE + RLE + LRE + PDF + "a" + PDF + "a" + PDF + "a"));
    assertEquals(RTL, BidiUtils.getEntryDir(
        LRE + RLE + LRE + BN + LRO + BN + PDF + BN + PDF + LRE + PDF + LRE + PDF + "a"
        + PDF + "a" + PDF + "a"));
    assertEquals(RTL, BidiUtils.getEntryDir(RLO + "." + PDF + "a"));
    assertEquals(RTL, BidiUtils.getEntryDir(RLO + "a" + PDF + "a"));
    assertEquals(RTL, BidiUtils.getEntryDir(LRE + PDF + RLO + "a" + PDF + "a"));
    assertEquals(RTL, BidiUtils.getEntryDir(RLO + LRE + PDF + "a" + PDF + "a"));
    assertEquals(RTL, BidiUtils.getEntryDir(RLO + "a" + LRE + "a" + PDF + PDF + "a"));
    assertEquals(RTL, BidiUtils.getEntryDir(LRE + RLO + "a" + PDF + "a" + PDF + "a"));
    assertEquals(RTL, BidiUtils.getEntryDir(
        LRE + RLO + LRE + PDF + "a" + PDF + "a" + PDF + "a"));
    assertEquals(RTL, BidiUtils.getEntryDir(
        LRE + RLO + LRE + BN + LRO + BN + PDF + BN + PDF + LRE + PDF + LRE + PDF + "a"
        + PDF + "a" + PDF + "a"));
  }

  public void testGetEntryDirOfHtml_MarkupSkipped() {
    assertEquals(LTR, BidiUtils.getEntryDir("<a tag>" + HE));
    assertEquals(RTL, BidiUtils.getEntryDir("<a tag>" + HE, true));
    assertEquals(RTL, BidiUtils.getEntryDir("<a x=y tag>" + HE, true));
    assertEquals(RTL, BidiUtils.getEntryDir("<a x=\"y>\" tag>" + HE, true));
    assertEquals(RTL, BidiUtils.getEntryDir("<a x=\"<y>\" tag>" + HE, true));
    assertEquals(RTL, BidiUtils.getEntryDir("<a x='<y>' tag>" + HE, true));
    assertEquals(LTR, BidiUtils.getEntryDir("<a x=\"<y>\" tag>a" + HE, true));
    assertEquals(RTL, BidiUtils.getEntryDir("<a x=\"<y>\" tag><b>" + HE, true));

    assertEquals(LTR, BidiUtils.getEntryDir("<notatag", true));
  }

  public void testGetEntryDirOfHtml_EntitySkipped() {
    assertEquals(NEUTRAL, BidiUtils.getEntryDir("&nbsp;", true));

    // TODO: Uncomment these lines and rename test to ...Parsed() when we start to map entities to
    // the characters for which they stand.
    // assertEquals(LTR, BidiUtils.getEntryDir("&rlm;"));
    // assertEquals(RTL, BidiUtils.getEntryDir("&rlm;", true));
    // assertEquals(RTL, BidiUtils.getEntryDir("!!!!&rlm;hello", true));
    // assertEquals(RTL, BidiUtils.getEntryDir("&nbsp;&rlm;", true));
    // assertEquals(LTR, BidiUtils.getEntryDir("&nbsp;a&rlm;", true));
    // assertEquals(LTR, BidiUtils.getEntryDir("&nosuchentity;", true));
  }

  public void testGetExitDir_NeutralText() {
    assertEquals(NEUTRAL, BidiUtils.getExitDir(""));
    assertEquals(NEUTRAL, BidiUtils.getExitDir("\t   \r\n"));
    assertEquals(NEUTRAL, BidiUtils.getExitDir("123"));
    assertEquals(NEUTRAL, BidiUtils.getExitDir(" 123-()"));
  }

  public void testGetExitDir_LtrLast() {
    assertEquals(LTR, BidiUtils.getExitDir("a"));
    assertEquals(LTR, BidiUtils.getExitDir("a   \t"));
    assertEquals(LTR, BidiUtils.getExitDir("abc   \t"));
    assertEquals(LTR, BidiUtils.getExitDir(HE + " a   \t"));
    assertEquals(LTR, BidiUtils.getExitDir(HE + "a   \t"));
    assertEquals(LTR, BidiUtils.getExitDir(HE + HE + " " + HE + " a   \t"));
    assertEquals(LTR, BidiUtils.getExitDir(HE + " http://www.google.com"));
  }

  public void testGetExitDir_RtlLast() {
    assertEquals(RTL, BidiUtils.getExitDir(HE));
    assertEquals(RTL, BidiUtils.getExitDir(HE + "   \t"));
    assertEquals(RTL, BidiUtils.getExitDir(HE + HE + HE + "   \t"));
    assertEquals(RTL, BidiUtils.getExitDir("a " + HE + "   \t"));
    assertEquals(RTL, BidiUtils.getExitDir("a" + HE + "   \t"));
    assertEquals(RTL, BidiUtils.getExitDir("abc a " + HE + "   \t"));
  }

  public void testGetExitDir_EmptyEmbeddingIgnored() {
    assertEquals(LTR, BidiUtils.getExitDir("a" + RLE + PDF));
    assertEquals(LTR, BidiUtils.getExitDir("a" + RLE + BN + PDF));
    assertEquals(LTR, BidiUtils.getExitDir("a" + RLO + PDF));
    assertEquals(LTR, BidiUtils.getExitDir("a" + RLO + BN + PDF));
    assertEquals(RTL, BidiUtils.getExitDir(HE + LRE + PDF));
    assertEquals(RTL, BidiUtils.getExitDir(HE + LRE + BN + PDF));
    assertEquals(RTL, BidiUtils.getExitDir(HE + LRO + PDF));
    assertEquals(RTL, BidiUtils.getExitDir(HE + LRO + BN + PDF));
  }

  public void testGetExitDir_NonEmptyLtrEmbeddingLast() {
    assertEquals(LTR, BidiUtils.getExitDir(HE + LRE + "." + PDF));
    assertEquals(LTR, BidiUtils.getExitDir(HE + LRE + HE + PDF));
    assertEquals(LTR, BidiUtils.getExitDir(
        HE + LRE + HE + PDF + RLE + PDF));
    assertEquals(LTR, BidiUtils.getExitDir(
        HE + LRE + HE + RLE + PDF + PDF));
    assertEquals(LTR, BidiUtils.getExitDir(
        HE + LRE + RLE + HE + PDF + HE + PDF));
    assertEquals(LTR, BidiUtils.getExitDir(
        HE + RLE + HE + LRE + HE + PDF + PDF));
    assertEquals(LTR, BidiUtils.getExitDir(
        HE + RLE + HE + LRE + HE + RLE + PDF + PDF + PDF));
    assertEquals(LTR, BidiUtils.getExitDir(HE + LRO + "." + PDF));
    assertEquals(LTR, BidiUtils.getExitDir(HE + LRO + HE + PDF));
    assertEquals(LTR, BidiUtils.getExitDir(
        HE + LRO + HE + PDF + RLE + PDF));
    assertEquals(LTR, BidiUtils.getExitDir(
        HE + LRO + HE + RLE + PDF + PDF));
    assertEquals(LTR, BidiUtils.getExitDir(
        HE + LRO + RLE + HE + PDF + HE + PDF));
    assertEquals(LTR, BidiUtils.getExitDir(
        HE + RLE + HE + LRO + HE + PDF + PDF));
    assertEquals(LTR, BidiUtils.getExitDir(
        HE + RLE + HE + LRO + HE + RLE + PDF + PDF + PDF));
  }

  public void testGetExitDir_NonEmptyRtlEmbeddingLast() {
    assertEquals(RTL, BidiUtils.getExitDir("a" + RLE + "." + PDF));
    assertEquals(RTL, BidiUtils.getExitDir("a" + RLE + "a" + PDF));
    assertEquals(RTL, BidiUtils.getExitDir("a" + RLE + "a" + PDF + LRE + PDF));
    assertEquals(RTL, BidiUtils.getExitDir("a" + RLE + "a" + LRE + PDF + PDF));
    assertEquals(RTL, BidiUtils.getExitDir("a" + RLE + LRE + "a" + PDF + "a" + PDF));
    assertEquals(RTL, BidiUtils.getExitDir("a" + LRE + "a" + RLE + "a" + PDF + PDF));
    assertEquals(RTL, BidiUtils.getExitDir(
        "a" + LRE + "a" + RLE + "a" + LRE + PDF + PDF + PDF));
    assertEquals(RTL, BidiUtils.getExitDir("a" + RLO + "." + PDF));
    assertEquals(RTL, BidiUtils.getExitDir("a" + RLO + "a" + PDF));
    assertEquals(RTL, BidiUtils.getExitDir("a" + RLO + "a" + PDF + LRE + PDF));
    assertEquals(RTL, BidiUtils.getExitDir("a" + RLO + "a" + LRE + PDF + PDF));
    assertEquals(RTL, BidiUtils.getExitDir("a" + RLO + LRE + "a" + PDF + "a" + PDF));
    assertEquals(RTL, BidiUtils.getExitDir("a" + LRE + "a" + RLO + "a" + PDF + PDF));
    assertEquals(RTL, BidiUtils.getExitDir(
        "a" + LRE + "a" + RLO + "a" + LRE + PDF + PDF + PDF));
  }

  public void testGetExitDirOfHtml_MarkupSkipped() {
    assertEquals(LTR, BidiUtils.getExitDir(HE + "<a tag>"));
    assertEquals(RTL, BidiUtils.getExitDir(HE + "<a tag>", true));
    assertEquals(RTL, BidiUtils.getExitDir(HE + "<a x=y tag>", true));
    assertEquals(RTL, BidiUtils.getExitDir(HE + "<a x=\"y>\" tag>", true));
    assertEquals(RTL, BidiUtils.getExitDir(HE + "<a x=\"<y>\" tag>", true));
    assertEquals(RTL, BidiUtils.getExitDir(HE + "<a x='<y>' tag>", true));
    assertEquals(LTR, BidiUtils.getExitDir(HE + "a<a x=\"<y>\" tag>", true));
    assertEquals(RTL, BidiUtils.getExitDir(HE + "<a x=\"<y>\" tag><b>", true));

    assertEquals(LTR, BidiUtils.getExitDir("<notatag", true));
  }

  public void testGetExitDirOfHtml_EntitySkipped() {
    assertEquals(NEUTRAL, BidiUtils.getExitDir("&nbsp;", true));

    // TODO: Uncomment these lines and rename test to ...Parsed() when we start to map entities to
    // the characters for which they stand.
    // assertEquals(LTR, BidiUtils.getExitDir("&rlm;"));
    // assertEquals(RTL, BidiUtils.getExitDir("&rlm;", true));
    // assertEquals(RTL, BidiUtils.getExitDir("hello&rlm;!!!!", true));
    // assertEquals(RTL, BidiUtils.getExitDir("&rlm;&nbsp;", true));
    // assertEquals(LTR, BidiUtils.getExitDir("&rlm;a&nbsp;", true));
    // assertEquals(LTR, BidiUtils.getExitDir("&nosuchentity;", true));
  }

  private Dir estimateTextDir(String s) {
    return BidiUtils.estimateDirection(s, false);
  }

  private Dir estimateHtmlDir(String s) {
    return BidiUtils.estimateDirection(s, true);
  }

  // The following test is basically superseded by the more detailed and thorough tests below, but
  // more tests are always better.
  public void testEstimateDirection_Old() {
    assertEquals(NEUTRAL, estimateTextDir(""));
    assertEquals(NEUTRAL, estimateTextDir(" "));
    assertEquals(NEUTRAL, estimateTextDir("! (...)"));
    assertEquals(LTR, estimateTextDir("Pure Ascii content"));
    assertEquals(LTR, estimateTextDir("-17.0%"));
    assertEquals(LTR, estimateTextDir("http://foo/bar/"));
    assertEquals(LTR, estimateTextDir(
        "http://foo/bar/?s=" + HE + HE + HE + HE + HE + HE + HE + HE + HE + HE + HE
        + HE + HE + HE + HE + HE + HE + HE + HE + HE + HE + HE + HE + HE + HE));
    assertEquals(LTR, estimateTextDir(LRO + HE + HE + PDF));
    assertEquals(RTL, estimateTextDir(HE));
    assertEquals(RTL, estimateTextDir(
        "9 " + HE + " -> 17.5, 23, 45, 19"));
    // We want to consider URLs "weakly LTR" like numbers, so they do not affect the estimation
    // if there are any strong directional words around. This should work regardless of the number
    // of spaces preceding the URL (which is a concern in the implementation.)
    assertEquals(RTL, estimateTextDir(
        "http://foo/bar/ " + HE + " http://foo2/bar/  http://foo/bar3/   http://foo4/bar/"));
    assertEquals(RTL, estimateTextDir(RLO + "foo" + PDF));
    assertEquals(RTL, estimateTextDir(
        "\u05d0\u05d9\u05df \u05de\u05de\u05e9 "
        + "\u05de\u05d4 \u05dc\u05e8\u05d0\u05d5\u05ea: "
        + "\u05dc\u05d0 \u05e6\u05d9\u05dc\u05de\u05ea\u05d9 "
        + "\u05d4\u05e8\u05d1\u05d4 \u05d5\u05d2\u05dd \u05d0"
        + "\u05dd \u05d4\u05d9\u05d9\u05ea\u05d9 \u05de\u05e6\u05dc"
        + "\u05dd, \u05d4\u05d9\u05d4 \u05e9\u05dd"));
    assertEquals(RTL, estimateTextDir(
        "\u05db\u05d0\u05df - http://geek.co.il/gallery/v/2007-06"
        + " - \u05d0\u05d9\u05df \u05de\u05de\u05e9 \u05de\u05d4 "
        + "\u05dc\u05e8\u05d0\u05d5\u05ea: \u05dc\u05d0 \u05e6"
        + "\u05d9\u05dc\u05de\u05ea\u05d9 \u05d4\u05e8\u05d1\u05d4 "
        + "\u05d5\u05d2\u05dd \u05d0\u05dd \u05d4\u05d9\u05d9\u05ea"
        + "\u05d9 \u05de\u05e6\u05dc\u05dd, \u05d4\u05d9\u05d4 "
        + "\u05e9\u05dd \u05d1\u05e2\u05d9\u05e7\u05e8 \u05d4\u05e8"
        + "\u05d1\u05d4 \u05d0\u05e0\u05e9\u05d9\u05dd. \u05de"
        + "\u05d4 \u05e9\u05db\u05df - \u05d0\u05e4\u05e9\u05e8 "
        + "\u05dc\u05e0\u05e6\u05dc \u05d0\u05ea \u05d4\u05d4 "
        + "\u05d3\u05d6\u05de\u05e0\u05d5\u05ea \u05dc\u05d4\u05e1"
        + "\u05ea\u05db\u05dc \u05e2\u05dc \u05db\u05de\u05d4 "
        + "\u05ea\u05de\u05d5\u05e0\u05d5\u05ea \u05de\u05e9\u05e2"
        + "\u05e9\u05e2\u05d5\u05ea \u05d9\u05e9\u05e0\u05d5\u05ea "
        + "\u05d9\u05d5\u05ea\u05e8 \u05e9\u05d9\u05e9 \u05dc"
        + "\u05d9 \u05d1\u05d0\u05ea\u05e8"));
    assertEquals(RTL, estimateTextDir(
        "CAPTCHA \u05de\u05e9\u05d5\u05db\u05dc\u05dc \u05de\u05d3\u05d9?"));
    assertEquals(LTR, estimateTextDir(
        "CAPTCHA blah \u05de\u05d3\u05d9?"));
    assertEquals(RTL, estimateTextDir(
        HE + HE + " " + LRO + HE + HE + PDF));
    assertEquals(LTR, estimateTextDir(
        HE + HE + " " + LRO + HE + HE + PDF + " " + LRO + HE + HE + PDF));
    assertEquals(RTL, estimateTextDir(
        "hello " + RLO + "foo" + PDF + " " + RLO + "bar" + PDF));
    assertEquals(RTL, estimateTextDir(
        "Yes Prime Minister \u05e2\u05d3\u05db\u05d5\u05df. "
        + "\u05e9\u05d0\u05dc\u05d5 \u05d0\u05d5\u05ea\u05d9 "
        + "\u05de\u05d4 \u05d0\u05e0\u05d9 \u05e8\u05d5\u05e6"
        + "\u05d4 \u05de\u05ea\u05e0\u05d4 \u05dc\u05d7\u05d2"));
    assertEquals(RTL, estimateTextDir(
        "17.4.02 \u05e9\u05e2\u05d4:13-20 .15-00 .\u05dc\u05d0 "
        + "\u05d4\u05d9\u05d9\u05ea\u05d9 \u05db\u05d0\u05df."));
    assertEquals(RTL, estimateTextDir(
        "5710 5720 5730. \u05d4\u05d3\u05dc\u05ea. "
        + "\u05d4\u05e0\u05e9\u05d9\u05e7\u05d4"));
    assertEquals(RTL, estimateTextDir(
        "\u05d4\u05d3\u05dc\u05ea http://www.google.com "
        + "http://www.gmail.com"));
    assertEquals(LTR, estimateTextDir(
        "\u05d4\u05d3\u05dc\u05ea <some quite nasty html mark up>"));
    assertEquals(RTL, estimateHtmlDir(
        "\u05d4\u05d3\u05dc\u05ea <some quite nasty html mark up>"));
    assertEquals(LTR, estimateTextDir(
        "\u05d4\u05d3\u05dc\u05ea &amp; &lt; &gt;"));
    assertEquals(RTL, estimateHtmlDir(
        "\u05d4\u05d3\u05dc\u05ea &amp; &lt; &gt;"));
  }

  public void testEstimateDirection_English() {
    String s =  // 21 ltr, 0 rtl, 0 numeric
       "Once upon a midnight dreary,\n"
       + "While I nodded, weak and weary,\n"
       + "Over many a quaint and curious volume of forgotten lore...";
    assertEquals(LTR, estimateTextDir(s));
    assertEquals(LTR, estimateHtmlDir(s));
  }

  public void testEstimateDirection_Hebrew() {
    // s has 0 ltr, 3 rtl, 0 numeric
    String s = "\u05D0\u05D1\u05D2\u05D3 \u05D4\u05D5\u05D6 ... \u05D7\u05D8";
    assertEquals(RTL, estimateTextDir(s));
    assertEquals(RTL, estimateHtmlDir(s));
  }

  public void testEstimateDirection_Arabic() {
    // s has 0 ltr, 3 rtl, 0 numeric
    String s = "\u0627\u0628\u0629\u062A\u062B \u062C\u062D ... \u062E\u062F!";
    assertEquals(RTL, estimateTextDir(s));
    assertEquals(RTL, estimateHtmlDir(s));
  }

  public void testEstimateDirection_Chinese() {
    // s has 1 ltr, 0 rtl, 0 numeric
    String s = "\u9910!";
    assertEquals(LTR, estimateTextDir(s));
    assertEquals(LTR, estimateHtmlDir(s));
  }

  public void testEstimateDirection_Neutral() {
    String s = "   ..././.\\*(())!#%% \r\n\t ];'\\;'@#$";
    assertEquals(NEUTRAL, estimateTextDir(s));
    assertEquals(NEUTRAL, estimateHtmlDir(s));
  }

  public void testEstimateDirection_EmptyString() {
    assertEquals(NEUTRAL, estimateTextDir(""));
    assertEquals(NEUTRAL, estimateHtmlDir(""));
  }

  public void testEstimateDirection_Mixed() {
    assertEquals(RTL, estimateTextDir("supercalifragilisticexpialidocious " + HE));
    assertEquals(RTL, estimateHtmlDir("supercalifragilisticexpialidocious " + HE));
    assertEquals(LTR, estimateTextDir("a supercalifragilisticexpialidocious " + HE));
    assertEquals(LTR, estimateHtmlDir("a supercalifragilisticexpialidocious " + HE));

    assertEquals(RTL, estimateTextDir("a/b/c.d.e.f@g-h " + HE));
    assertEquals(RTL, estimateHtmlDir("a/b/c.d.e.f@g-h " + HE));
    assertEquals(RTL, estimateTextDir("a/b/c.d.e.f@g-h " + HE));
    assertEquals(RTL, estimateHtmlDir("a/b/c.d.e.f@g-h " + HE));

    assertEquals(RTL, estimateTextDir("HTML \u0648Google"));
    assertEquals(RTL, estimateHtmlDir("HTML \u0648Google"));
    assertEquals(RTL, estimateTextDir("HTML \u05D5-Google"));
    assertEquals(RTL, estimateHtmlDir("HTML \u05D5-Google"));

    assertEquals(LTR, estimateTextDir("we love " + HE + " +10!"));
    assertEquals(LTR, estimateHtmlDir("we love " + HE + " +10!"));
    assertEquals(LTR, estimateTextDir("we love " + HE + " so"));
    assertEquals(LTR, estimateHtmlDir("we love " + HE + " so"));
    assertEquals(RTL, estimateTextDir(HE + " cool 1!"));
    assertEquals(RTL, estimateHtmlDir(HE + " cool 1!"));
    assertEquals(RTL, estimateTextDir(HE + " cool " + HE));
    assertEquals(RTL, estimateHtmlDir(HE + " cool " + HE));
  }

  public void testEstimateDirection_MixedWithHan() {
    assertEquals(RTL, estimateTextDir(HAN + ' ' + HE));
    assertEquals(RTL, estimateHtmlDir(HAN + ' ' + HE));
  }

  public void testEstimateDirection_PersianNumeric() {
    assertEquals(NEUTRAL, estimateTextDir("\u06F1"));
    assertEquals(NEUTRAL, estimateHtmlDir("\u06F1"));
    assertEquals(NEUTRAL, estimateTextDir("\u06F1\u06F2\u06F3"));
    assertEquals(NEUTRAL, estimateHtmlDir("\u06F1\u06F2\u06F3"));
    assertEquals(NEUTRAL, estimateTextDir("\u06F1\u06F2\u06F3,\u06F4\u06F5\u06F6.\u06F7\u06F8"));
    assertEquals(NEUTRAL, estimateHtmlDir("\u06F1\u06F2\u06F3,\u06F4\u06F5\u06F6.\u06F7\u06F8"));
    assertEquals(NEUTRAL, estimateTextDir("\u06F1:\u06F2"));
    assertEquals(NEUTRAL, estimateHtmlDir("\u06F1:\u06F2"));
    assertEquals(NEUTRAL, estimateTextDir("\u06F1" + NBSP + "\u06F2"));
    assertEquals(NEUTRAL, estimateHtmlDir("\u06F1" + NBSP + "\u06F2"));
    assertEquals(NEUTRAL, estimateTextDir("\u06F1-"));
    assertEquals(NEUTRAL, estimateHtmlDir("\u06F1-"));
    assertEquals(NEUTRAL, estimateTextDir("+ \u06F1"));
    assertEquals(NEUTRAL, estimateHtmlDir("+ \u06F1"));
    assertEquals(NEUTRAL, estimateTextDir("- \u06F1"));
    assertEquals(NEUTRAL, estimateHtmlDir("- \u06F1"));
    assertEquals(NEUTRAL, estimateTextDir("-/\u06F1"));
    assertEquals(NEUTRAL, estimateHtmlDir("-/\u06F1"));
    assertEquals(NEUTRAL, estimateTextDir("-*\u06F1"));
    assertEquals(NEUTRAL, estimateHtmlDir("-*\u06F1"));

    assertEquals(LTR, estimateTextDir("-\u06F1"));
    assertEquals(LTR, estimateHtmlDir("-\u06F1"));
    assertEquals(LTR, estimateTextDir("+\u06F1"));
    assertEquals(LTR, estimateHtmlDir("+\u06F1"));
    assertEquals(LTR, estimateTextDir(MINUS + "\u06F1"));
    assertEquals(LTR, estimateHtmlDir(MINUS + "\u06F1"));
    assertEquals(LTR, estimateTextDir("(-\u06F1)"));
    assertEquals(LTR, estimateHtmlDir("(-\u06F1)"));
    assertEquals(LTR, estimateTextDir("* '+\u06F1\u06F2.\u06F3\u06F4'?"));
    assertEquals(LTR, estimateHtmlDir("* '+\u06F1\u06F2.\u06F3\u06F4'?"));
    assertEquals(LTR, estimateTextDir("--\u06F1"));
    assertEquals(LTR, estimateHtmlDir("--\u06F1"));
    assertEquals(LTR, estimateTextDir("\u06F1\u00D7\u06F1\u06F0^-\u06F6"));
    assertEquals(LTR, estimateHtmlDir("\u06F1\u00D7\u06F1\u06F0^-\u06F6"));

    assertEquals(LTR, estimateTextDir(
        "+\u06F1 \u06F9 \u06F2\u06F3\u06F4-\u06F5\u06F6\u06F7\u06F8"));
    assertEquals(LTR, estimateHtmlDir(
        "+\u06F1 \u06F9 \u06F2\u06F3\u06F4-\u06F5\u06F6\u06F7\u06F8"));
    assertEquals(LTR, estimateTextDir("+\u06F1\u06F9\u06F2\u06F3\u06F4\u06F5\u06F6\u06F7\u06F8"));
    assertEquals(LTR, estimateHtmlDir("+\u06F1\u06F9\u06F2\u06F3\u06F4\u06F5\u06F6\u06F7\u06F8"));

    assertEquals(LTR, estimateTextDir("\u06F0\u06F1 \u06F2\u06F3\u06F4-\u06F5\u06F6\u06F7\u06F8"));
    assertEquals(LTR, estimateHtmlDir("\u06F0\u06F1 \u06F2\u06F3\u06F4-\u06F5\u06F6\u06F7\u06F8"));
    assertEquals(LTR, estimateTextDir("\u06F2\u06F3\u06F4-\u06F5\u06F6\u06F7\u06F8"));
    assertEquals(LTR, estimateHtmlDir("\u06F2\u06F3\u06F4-\u06F5\u06F6\u06F7\u06F8"));
    assertEquals(LTR, estimateTextDir("\u06F2\u06F3\u06F4" + HYPHEN + "\u06F5\u06F6\u06F7\u06F8"));
    assertEquals(LTR, estimateHtmlDir("\u06F2\u06F3\u06F4" + HYPHEN + "\u06F5\u06F6\u06F7\u06F8"));
    assertEquals(LTR, estimateTextDir("\u06F2\u06F3/\u06F4\u06F5"));
    assertEquals(LTR, estimateHtmlDir("\u06F2\u06F3/\u06F4\u06F5"));
    assertEquals(LTR, estimateTextDir("\u06F1 \u06F2 \u06F3 \u06F4 \u06F5 \u06F6"));
    assertEquals(LTR, estimateHtmlDir("\u06F1 \u06F2 \u06F3 \u06F4 \u06F5 \u06F6"));
    assertEquals(LTR, estimateTextDir("\u06F1" + TIMES + "\u06F2"));
    assertEquals(LTR, estimateHtmlDir("\u06F1" + TIMES + "\u06F2"));
    assertEquals(LTR, estimateTextDir("\u06F1 " + TIMES + " \u06F2"));
    assertEquals(LTR, estimateHtmlDir("\u06F1 " + TIMES + " \u06F2"));
    assertEquals(LTR, estimateTextDir("\u06F1" + NBSP + TIMES + NBSP + "\u06F2"));
    assertEquals(LTR, estimateHtmlDir("\u06F1" + NBSP + TIMES + NBSP + "\u06F2"));

    assertEquals(RTL, estimateTextDir("\u06F1 \u06F2 \u06F3 \u06F4 \u06F5 \u06F6 " + FA));
    assertEquals(RTL, estimateHtmlDir("\u06F1 \u06F2 \u06F3 \u06F4 \u06F5 \u06F6 " + FA));

    assertEquals(LTR, estimateTextDir("\u06F1 \u06F2 \u06F3 \u06F4 \u06F5 \u06F6"));
    assertEquals(LTR, estimateHtmlDir("\u06F1 \u06F2 \u06F3 \u06F4 \u06F5 \u06F6"));
  }

  public void testEstimateDirection_ArabicNumeric() {
    assertEquals(NEUTRAL, estimateTextDir("\u0661"));
    assertEquals(NEUTRAL, estimateHtmlDir("\u0661"));
    assertEquals(NEUTRAL, estimateTextDir("\u0661\u0662\u0663"));
    assertEquals(NEUTRAL, estimateHtmlDir("\u0661\u0662\u0663"));
    assertEquals(NEUTRAL, estimateTextDir(
        "\u0661\u0662\u0663,\u0664\u0665\u0666.\u0667\u0668"));
    assertEquals(NEUTRAL, estimateHtmlDir(
        "\u0661\u0662\u0663,\u0664\u0665\u0666.\u0667\u0668"));
    assertEquals(NEUTRAL, estimateTextDir("\u0661:\u0662"));
    assertEquals(NEUTRAL, estimateHtmlDir("\u0661:\u0662"));
    assertEquals(NEUTRAL, estimateTextDir("\u0661" + NBSP + "\u0662"));
    assertEquals(NEUTRAL, estimateHtmlDir("\u0661" + NBSP + "\u0662"));
    assertEquals(NEUTRAL, estimateTextDir("\u0661-"));
    assertEquals(NEUTRAL, estimateHtmlDir("\u0661-"));
    assertEquals(NEUTRAL, estimateTextDir("+ \u0661"));
    assertEquals(NEUTRAL, estimateHtmlDir("+ \u0661"));
    assertEquals(NEUTRAL, estimateTextDir("- \u0661"));
    assertEquals(NEUTRAL, estimateHtmlDir("- \u0661"));
    assertEquals(NEUTRAL, estimateTextDir("-/\u0661"));
    assertEquals(NEUTRAL, estimateHtmlDir("-/\u0661"));
    assertEquals(NEUTRAL, estimateTextDir("-*\u0661"));
    assertEquals(NEUTRAL, estimateHtmlDir("-*\u0661"));

    assertEquals(RTL, estimateTextDir("-\u0661"));
    assertEquals(RTL, estimateHtmlDir("-\u0661"));
    assertEquals(RTL, estimateTextDir(MINUS + "\u0661"));
    assertEquals(RTL, estimateHtmlDir(MINUS + "\u0661"));
    assertEquals(RTL, estimateTextDir("(-\u0661)"));
    assertEquals(RTL, estimateHtmlDir("(-\u0661)"));
    assertEquals(RTL, estimateTextDir("--\u0661"));
    assertEquals(RTL, estimateHtmlDir("--\u0661"));
    assertEquals(RTL, estimateTextDir("\u0661\u0627\u0633-\u0666"));
    assertEquals(RTL, estimateHtmlDir("\u0661\u0627\u0633-\u0666"));

    assertEquals(LTR, estimateTextDir(
        "+\u0661" + NBSP + "\u0669" + NBSP + "\u0662\u0663\u0664" +
        NBSP + "\u0665\u0666\u0667\u0668"));
    assertEquals(LTR, estimateHtmlDir(
        "+\u0661" + NBSP + "\u0669" + NBSP + "\u0662\u0663\u0664" +
        NBSP + "\u0665\u0666\u0667\u0668"));
    assertEquals(LTR, estimateTextDir("+\u0661\u0669\u0662\u0663\u0664\u0665\u0666\u0667\u0668"));
    assertEquals(LTR, estimateHtmlDir("+\u0661\u0669\u0662\u0663\u0664\u0665\u0666\u0667\u0668"));
    assertEquals(LTR, estimateTextDir(
        "+\u0661 \u0669 \u0662\u0663\u0664-\u0665\u0666\u0667\u0668"));
    assertEquals(LTR, estimateHtmlDir(
        "+\u0661 \u0669 \u0662\u0663\u0664-\u0665\u0666\u0667\u0668"));
    assertEquals(LTR, estimateTextDir("+\u0661"));
    assertEquals(LTR, estimateHtmlDir("+\u0661"));
    assertEquals(LTR, estimateTextDir("* '+\u0661\u0662.\u0663\u0664'?"));
    assertEquals(LTR, estimateHtmlDir("* '+\u0661\u0662.\u0663\u0664'?"));

    assertEquals(NEUTRAL, estimateTextDir(
        "\u0660\u0661.\u0662\u0663\u0664.\u0665\u0666\u0667\u0668"));
    assertEquals(NEUTRAL, estimateHtmlDir(
        "\u0660\u0661.\u0662\u0663\u0664.\u0665\u0666\u0667\u0668"));
    assertEquals(NEUTRAL, estimateTextDir(
        "\u0660\u0661 \u0662\u0663\u0664-\u0665\u0666\u0667\u0668"));
    assertEquals(NEUTRAL, estimateHtmlDir(
        "\u0660\u0661 \u0662\u0663\u0664-\u0665\u0666\u0667\u0668"));
    assertEquals(NEUTRAL, estimateTextDir("\u0662\u0663\u0664-\u0665\u0666\u0667\u0668"));
    assertEquals(NEUTRAL, estimateHtmlDir("\u0662\u0663\u0664-\u0665\u0666\u0667\u0668"));
    assertEquals(NEUTRAL, estimateTextDir(
        "\u0662\u0663\u0664" + HYPHEN + "\u0665\u0666\u0667\u0668"));
    assertEquals(NEUTRAL, estimateHtmlDir(
        "\u0662\u0663\u0664" + HYPHEN + "\u0665\u0666\u0667\u0668"));
    assertEquals(NEUTRAL, estimateTextDir("\u0662\u0663/\u0664\u0665"));
    assertEquals(NEUTRAL, estimateHtmlDir("\u0662\u0663/\u0664\u0665"));
    assertEquals(NEUTRAL, estimateTextDir("\u0661 \u0662 \u0663 \u0664 \u0665 \u0666"));
    assertEquals(NEUTRAL, estimateHtmlDir("\u0661 \u0662 \u0663 \u0664 \u0665 \u0666"));
    assertEquals(NEUTRAL, estimateTextDir("\u0661" + TIMES + "\u0662"));
    assertEquals(NEUTRAL, estimateHtmlDir("\u0661" + TIMES + "\u0662"));
    assertEquals(NEUTRAL, estimateTextDir("\u0661 " + TIMES + " \u0662"));
    assertEquals(NEUTRAL, estimateHtmlDir("\u0661 " + TIMES + " \u0662"));
    assertEquals(NEUTRAL, estimateTextDir("\u0661" + NBSP + TIMES + NBSP + "\u0662"));
    assertEquals(NEUTRAL, estimateHtmlDir("\u0661" + NBSP + TIMES + NBSP + "\u0662"));

    assertEquals(RTL, estimateTextDir("\u0661 \u0662 \u0663 \u0664 \u0665 \u0666 " + AR));
    assertEquals(RTL, estimateHtmlDir("\u0661 \u0662 \u0663 \u0664 \u0665 \u0666 " + AR));

    assertEquals(NEUTRAL, estimateTextDir("\u0661 \u0662 \u0663 \u0664 \u0665 \u0666"));
    assertEquals(NEUTRAL, estimateHtmlDir("\u0661 \u0662 \u0663 \u0664 \u0665 \u0666"));
  }

  public void testEstimateDirection_AsciiNumeric() {
    assertEquals(NEUTRAL, estimateTextDir("1"));
    assertEquals(NEUTRAL, estimateHtmlDir("1"));
    assertEquals(NEUTRAL, estimateTextDir("123"));
    assertEquals(NEUTRAL, estimateHtmlDir("123"));
    assertEquals(NEUTRAL, estimateTextDir("123,456.78"));
    assertEquals(NEUTRAL, estimateHtmlDir("123,456.78"));
    assertEquals(NEUTRAL, estimateTextDir("1:2"));
    assertEquals(NEUTRAL, estimateHtmlDir("1:2"));
    assertEquals(NEUTRAL, estimateTextDir("1" + NBSP + "2"));
    assertEquals(NEUTRAL, estimateHtmlDir("1" + NBSP + "2"));
    assertEquals(NEUTRAL, estimateTextDir("1-"));
    assertEquals(NEUTRAL, estimateHtmlDir("1-"));
    assertEquals(NEUTRAL, estimateTextDir("+ 1"));
    assertEquals(NEUTRAL, estimateHtmlDir("+ 1"));
    assertEquals(NEUTRAL, estimateTextDir("- 1"));
    assertEquals(NEUTRAL, estimateHtmlDir("- 1"));
    assertEquals(NEUTRAL, estimateTextDir("-/1"));
    assertEquals(NEUTRAL, estimateHtmlDir("-/1"));
    assertEquals(NEUTRAL, estimateTextDir("-*1"));
    assertEquals(NEUTRAL, estimateHtmlDir("-*1"));

    assertEquals(LTR, estimateTextDir("-1"));
    assertEquals(LTR, estimateHtmlDir("-1"));
    assertEquals(LTR, estimateTextDir("+1"));
    assertEquals(LTR, estimateHtmlDir("+1"));
    assertEquals(LTR, estimateTextDir(ALT_PLUS + "1"));
    assertEquals(LTR, estimateHtmlDir(ALT_PLUS + "1"));
    assertEquals(LTR, estimateTextDir(MINUS + "1"));
    assertEquals(LTR, estimateHtmlDir(MINUS + "1"));
    assertEquals(LTR, estimateTextDir("(-1)"));
    assertEquals(LTR, estimateHtmlDir("(-1)"));
    assertEquals(LTR, estimateTextDir("* '+12.34'?"));
    assertEquals(LTR, estimateHtmlDir("* '+12.34'?"));
    assertEquals(LTR, estimateTextDir("--1"));
    assertEquals(LTR, estimateHtmlDir("--1"));
    assertEquals(LTR, estimateTextDir("1e-6"));
    assertEquals(LTR, estimateHtmlDir("1e-6"));

    assertEquals(LTR, estimateTextDir("+1 9 234-5678"));
    assertEquals(LTR, estimateHtmlDir("+1 9 234-5678"));
    assertEquals(LTR, estimateTextDir("+192345678"));
    assertEquals(LTR, estimateHtmlDir("+192345678"));

    assertEquals(LTR, estimateTextDir("01 234-5678"));
    assertEquals(LTR, estimateHtmlDir("01 234-5678"));
    assertEquals(LTR, estimateTextDir("234-5678"));
    assertEquals(LTR, estimateHtmlDir("234-5678"));
    assertEquals(LTR, estimateTextDir("234" + HYPHEN + "5678"));
    assertEquals(LTR, estimateHtmlDir("234" + HYPHEN + "5678"));
    assertEquals(LTR, estimateTextDir("23/45"));
    assertEquals(LTR, estimateHtmlDir("23/45"));
    assertEquals(LTR, estimateTextDir("1 2 3 4 5 6"));
    assertEquals(LTR, estimateHtmlDir("1 2 3 4 5 6"));
    assertEquals(LTR, estimateTextDir("1" + TIMES + "2"));
    assertEquals(LTR, estimateHtmlDir("1" + TIMES + "2"));
    assertEquals(LTR, estimateTextDir("1 " + TIMES + " 2"));
    assertEquals(LTR, estimateHtmlDir("1 " + TIMES + " 2"));
    assertEquals(LTR, estimateTextDir("1" + NBSP + TIMES + NBSP + "2"));
    assertEquals(LTR, estimateHtmlDir("1" + NBSP + TIMES + NBSP + "2"));

    assertEquals(RTL, estimateTextDir("1 2 3 4 5 6 " + HE));
    assertEquals(RTL, estimateHtmlDir("1 2 3 4 5 6 " + HE));

    assertEquals(LTR, estimateTextDir("1 2 3 4 5 6"));
    assertEquals(LTR, estimateHtmlDir("1 2 3 4 5 6"));
  }

  public void testEstimateDirection_HttpUrl() {
    assertEquals(LTR, estimateTextDir("http://"));
    assertEquals(LTR, estimateHtmlDir("http://"));
    assertEquals(LTR, estimateTextDir("http://x"));
    assertEquals(LTR, estimateHtmlDir("http://x"));
    assertEquals(LTR, estimateTextDir("http://x/" + HE));
    assertEquals(LTR, estimateHtmlDir("http://x/" + HE));

    // At least for now, even all-RTL URLs are considered LTR.
    assertEquals(LTR, estimateTextDir(
        "http://\u0645\u0648\u0642\u0639.\u0648\u0632\u0627\u0631\u0629-"
        + "\u0627\u0644\u0627\u062A\u0635\u0627\u0644\u0627\u062A.\u0645\u0635\u0631"));
    assertEquals(LTR, estimateHtmlDir(
        "http://\u0645\u0648\u0642\u0639.\u0648\u0632\u0627\u0631\u0629-"
        + "\u0627\u0644\u0627\u062A\u0635\u0627\u0644\u0627\u062A.\u0645\u0635\u0631"));
    assertEquals(LTR, estimateTextDir(
        "http://\u0627\u0633\u062A\u0641\u062A\u0627\u0621.\u0645\u0635\u0631"));
    assertEquals(LTR, estimateHtmlDir(
        "http://\u0627\u0633\u062A\u0641\u062A\u0627\u0621.\u0645\u0635\u0631"));

    assertEquals(LTR, estimateTextDir("x " + HE + " y"));
    assertEquals(LTR, estimateHtmlDir("x " + HE + " y"));
    assertEquals(RTL, estimateTextDir("http:// " + HE + " http://"));
    assertEquals(RTL, estimateHtmlDir("http:// " + HE + " http://"));
    assertEquals(RTL, estimateTextDir("http://x " + HE + " http://y"));
    assertEquals(RTL, estimateHtmlDir("http://x " + HE + " http://y"));
    assertEquals(LTR, estimateTextDir("http:/ " + HE + " http:/"));
    assertEquals(LTR, estimateHtmlDir("http:/ " + HE + " http:/"));

    assertEquals(LTR, estimateTextDir("http://"));
    assertEquals(LTR, estimateHtmlDir("http://"));
    assertEquals(LTR, estimateTextDir("http://"));
    assertEquals(LTR, estimateHtmlDir("http://" + HE));
    assertEquals(RTL, estimateTextDir(HE + " http://"));
    assertEquals(RTL, estimateHtmlDir(HE + " http://"));
  }

  public void testEstimateDirection_HttpsUrl() {
    assertEquals(LTR, estimateTextDir("https://"));
    assertEquals(LTR, estimateHtmlDir("https://"));
    assertEquals(LTR, estimateTextDir("https://x"));
    assertEquals(LTR, estimateHtmlDir("https://x"));
    assertEquals(LTR, estimateTextDir("x " + HE + " y"));
    assertEquals(LTR, estimateHtmlDir("x " + HE + " y"));
    assertEquals(RTL, estimateTextDir("https:// " + HE + " https://"));
    assertEquals(RTL, estimateHtmlDir("https:// " + HE + " https://"));
    assertEquals(RTL, estimateTextDir("https://x " + HE + " https://y"));
    assertEquals(RTL, estimateHtmlDir("https://x " + HE + " https://y"));
    assertEquals(LTR, estimateTextDir("https:/ " + HE + " https:/"));
    assertEquals(LTR, estimateHtmlDir("https:/ " + HE + " https:/"));
    assertEquals(LTR, estimateTextDir("https://"));
    assertEquals(LTR, estimateHtmlDir("https://"));
    assertEquals(LTR, estimateTextDir("https://" + HE));
    assertEquals(LTR, estimateHtmlDir("https://" + HE));
    assertEquals(RTL, estimateTextDir(HE + " https://"));
    assertEquals(RTL, estimateHtmlDir(HE + " https://"));
  }

  public void testEstimateDirection_Embed() {
    assertEquals(NEUTRAL, estimateTextDir(RLE + HE + PDF));
    assertEquals(NEUTRAL, estimateHtmlDir(RLE + HE + PDF));
    assertEquals(NEUTRAL, estimateTextDir(RLE + HE + PDF));
    assertEquals(NEUTRAL, estimateHtmlDir(RLE + HE + PDF));

    assertEquals(NEUTRAL, estimateTextDir(RLE + "L T R" + PDF));
    assertEquals(NEUTRAL, estimateHtmlDir(RLE + "L T R" + PDF));
    assertEquals(NEUTRAL, estimateTextDir(RLE + "L T R" + PDF));
    assertEquals(NEUTRAL, estimateHtmlDir(RLE + "L T R" + PDF));

    assertEquals(NEUTRAL, estimateTextDir(LRE + HE + PDF));
    assertEquals(NEUTRAL, estimateHtmlDir(LRE + HE + PDF));
    assertEquals(NEUTRAL, estimateTextDir(LRE + HE + PDF));
    assertEquals(NEUTRAL, estimateHtmlDir(LRE + HE + PDF));

    assertEquals(NEUTRAL, estimateTextDir(LRE + "." + PDF));
    assertEquals(NEUTRAL, estimateHtmlDir(LRE + "." + PDF));
    assertEquals(NEUTRAL, estimateTextDir(LRE + "." + PDF));
    assertEquals(NEUTRAL, estimateHtmlDir(LRE + "." + PDF));

    assertEquals(NEUTRAL, estimateTextDir(LRE + RLE + HE + PDF + "." + PDF));
    assertEquals(NEUTRAL, estimateHtmlDir(LRE + RLE + HE + PDF + "." + PDF));

    assertEquals(LTR, estimateTextDir("A" + RLE + HE + PDF));
    assertEquals(LTR, estimateHtmlDir("A" + RLE + HE + PDF));

    assertEquals(LTR, estimateTextDir("A" + RLE + " " + HE + PDF));
    assertEquals(LTR, estimateHtmlDir("A" + RLE + " " + HE + PDF));

    assertEquals(LTR, estimateTextDir("A " + RLE + HE + PDF));
    assertEquals(LTR, estimateHtmlDir("A " + RLE + HE + PDF));

    assertEquals(LTR, estimateTextDir(RLE + HE + PDF + "A"));
    assertEquals(LTR, estimateHtmlDir(RLE + HE + PDF + "A"));

    assertEquals(RTL, estimateTextDir(HE + PDF + " A"));
    assertEquals(RTL, estimateHtmlDir(HE + PDF + " A"));

    assertEquals(LTR, estimateTextDir(HE + PDF + " A A A A A A A A"));
    assertEquals(LTR, estimateHtmlDir(HE + PDF + " A A A A A A A A"));

    assertEquals(LTR, estimateTextDir("A" + RLE + HE));
    assertEquals(LTR, estimateHtmlDir("A" + RLE + HE));

    assertEquals(LTR, estimateTextDir("A" + RLE + " " + HE));
    assertEquals(LTR, estimateHtmlDir("A" + RLE + " " + HE));

    assertEquals(LTR, estimateTextDir("A " + RLE + HE));
    assertEquals(LTR, estimateHtmlDir("A " + RLE + HE));
  }

  public void testEstimateDirection_Override() {
    assertEquals(RTL, estimateTextDir(RLO + HE + PDF));
    assertEquals(RTL, estimateHtmlDir(RLO + HE + PDF));
    assertEquals(RTL, estimateTextDir(RLO + HE + PDF));
    assertEquals(RTL, estimateHtmlDir(RLO + HE + PDF));

    assertEquals(RTL, estimateTextDir(RLO + "L T R" + PDF));
    assertEquals(RTL, estimateHtmlDir(RLO + "L T R" + PDF));
    assertEquals(RTL, estimateTextDir(RLO + "L T R" + PDF));
    assertEquals(RTL, estimateHtmlDir(RLO + "L T R" + PDF));

    assertEquals(LTR, estimateTextDir(LRO + HE + PDF));
    assertEquals(LTR, estimateHtmlDir(LRO + HE + PDF));
    assertEquals(LTR, estimateTextDir(LRO + HE + PDF));
    assertEquals(LTR, estimateHtmlDir(LRO + HE + PDF));

    assertEquals(RTL, estimateTextDir("A " + RLO + "L T R" + PDF));
    assertEquals(RTL, estimateHtmlDir("A " + RLO + "L T R" + PDF));
  }

  public void testEstimateDirection_MarkUp() {
    assertEquals(LTR, estimateTextDir("<t a=1>"));
    assertEquals(NEUTRAL, estimateHtmlDir("<t a=1>"));
    assertEquals(LTR, estimateTextDir("<t a=1>"));
    assertEquals(NEUTRAL, estimateHtmlDir("<t a=1>"));

    assertEquals(LTR, estimateTextDir(HE + " <t a=1>"));
    assertEquals(RTL, estimateHtmlDir(HE + "<t a=1 b=2>"));
    assertEquals(RTL, estimateHtmlDir(HE + "<t a=1 b=2>"));
    assertEquals(LTR, estimateHtmlDir("I am " + HE + "<t a=1 b=2>"));
    assertEquals(LTR, estimateHtmlDir("foo/<b>" + HE + "</b>"));
  }

  public void testEstimateDirection_Entity() {
    assertEquals(LTR, estimateTextDir("...&amp;"));
    assertEquals(NEUTRAL, estimateHtmlDir("...&amp;"));
    assertEquals(NEUTRAL, estimateHtmlDir("<span>"));
    assertEquals(LTR, estimateHtmlDir("&lt;span&gt;"));
    assertEquals(LTR, estimateHtmlDir("...&amp;a"));
    assertEquals(LTR, estimateTextDir("a&#32;&#x05D0;"));  // 32 is space
    // TODO: Uncomment these lines when we start to map entities to the characters for which they
    // stand.
    // assertEquals(RTL, estimateHtmlDir("a&#32;&#x05D0;"));
    // assertEquals(LTR, estimateHtmlDir("a&#32;a&#32;&#x05D0;"));
    // assertEquals(RTL, estimateHtmlDir("...&nosuchescape &#x05D0;"));
  }
}
