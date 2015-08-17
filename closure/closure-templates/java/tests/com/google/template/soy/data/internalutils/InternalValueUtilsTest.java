/*
 * Copyright 2015 Google Inc.
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

package com.google.template.soy.data.internalutils;

import com.google.common.collect.ImmutableMap;
import com.google.template.soy.base.SourceLocation;
import com.google.template.soy.data.restricted.BooleanData;
import com.google.template.soy.data.restricted.FloatData;
import com.google.template.soy.data.restricted.IntegerData;
import com.google.template.soy.data.restricted.NullData;
import com.google.template.soy.data.restricted.PrimitiveData;
import com.google.template.soy.data.restricted.StringData;
import com.google.template.soy.data.restricted.UndefinedData;
import com.google.template.soy.exprtree.BooleanNode;
import com.google.template.soy.exprtree.FloatNode;
import com.google.template.soy.exprtree.IntegerNode;
import com.google.template.soy.exprtree.NullNode;
import com.google.template.soy.exprtree.StringNode;

import junit.framework.TestCase;

import java.util.Iterator;
import java.util.Map;

/**
 * Unit tests for InternalValueUtils.
 *
 */
public class InternalValueUtilsTest extends TestCase {


  public void testConvertPrimitiveDataToExpr() {

    assertTrue(
        InternalValueUtils.convertPrimitiveDataToExpr(NullData.INSTANCE) instanceof NullNode);
    assertEquals(
        false,
        ((BooleanNode) InternalValueUtils.convertPrimitiveDataToExpr(BooleanData.FALSE))
            .getValue());
    assertEquals(
        26,
        ((IntegerNode) InternalValueUtils.convertPrimitiveDataToExpr(IntegerData.forValue(26)))
            .getValue());
    assertEquals(
        -3.14159,
        ((FloatNode) InternalValueUtils.convertPrimitiveDataToExpr(FloatData.forValue(-3.14159)))
            .getValue());
    assertEquals(
        "boo",
        ((StringNode) InternalValueUtils.convertPrimitiveDataToExpr(StringData.forValue("boo")))
            .getValue());

    try {
      InternalValueUtils.convertPrimitiveDataToExpr(UndefinedData.INSTANCE);
      fail();
    } catch (IllegalArgumentException iae) {
      // Test passes.
    }
  }


  public void testConvertPrimitiveExprToData() {
    assertTrue(InternalValueUtils.convertPrimitiveExprToData(
        new NullNode(SourceLocation.UNKNOWN)) instanceof NullData);
    assertEquals(
        true,
        InternalValueUtils.convertPrimitiveExprToData(
            new BooleanNode(true, SourceLocation.UNKNOWN)).booleanValue());
    assertEquals(
        -1,
        InternalValueUtils.convertPrimitiveExprToData(
            new IntegerNode(-1, SourceLocation.UNKNOWN)).integerValue());
    assertEquals(
        6.02e23,
        InternalValueUtils.convertPrimitiveExprToData(
            new FloatNode(6.02e23, SourceLocation.UNKNOWN)).floatValue());
    assertEquals(
        "foo",
        InternalValueUtils.convertPrimitiveExprToData(
            new StringNode("foo", SourceLocation.UNKNOWN)).stringValue());
  }


  public void testConvertCompileTimeGlobalsMap() {

    Map<String, Object> compileTimeGlobalsMap =
        ImmutableMap.<String, Object>of(
            "IS_SLEEPY", true, "sleepy.SHEEP", "Baa", "sleepy.NUM_SHEEP", 100,
            "NAME", "\u9EC4\u607A", "WHITESPACE", "\n\r\t");
    Map<String, PrimitiveData> actual =
        InternalValueUtils.convertCompileTimeGlobalsMap(compileTimeGlobalsMap);

    Map<String, PrimitiveData> expected =
        ImmutableMap.of(
            "IS_SLEEPY", BooleanData.TRUE,
            "sleepy.SHEEP", StringData.forValue("Baa"),
            "sleepy.NUM_SHEEP", IntegerData.forValue(100),
            "NAME", StringData.forValue("\u9EC4\u607A"),
            "WHITESPACE", StringData.forValue("\n\r\t"));

    // Note: Don't use Map.equals() because it doesn't require iteration order to be the same.
    assertEquals(expected.size(), actual.size());
    Iterator<Map.Entry<String, PrimitiveData>> expectedIter = expected.entrySet().iterator();
    Iterator<Map.Entry<String, PrimitiveData>> actualIter = actual.entrySet().iterator();
    for (int i = 0; i < expected.size(); i++) {
      assertEquals(expectedIter.next(), actualIter.next());
    }
  }

}
