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

package com.google.template.soy.soytree;

import static com.google.template.soy.soytree.TemplateSubject.assertThatTemplateContent;

import junit.framework.TestCase;

/**
 * Tests for {@link LetNode}.
 *
 * @author brndn@google.com (Brendan Linn)
 */
public final class LetNodeTest extends TestCase {

  public void testInvalidCommandText() {
    assertThatTemplateContent("{let foo /}\n")
        .causesError(LetNode.INVALID_COMMAND_TEXT)
        .at(1, 1);
    // Let nodes don't have accurate source location information for their command texts yet.
    // TODO(user): fix.
    assertThatTemplateContent("{let\n\n\nfoo /}\n")
        .causesError(LetNode.INVALID_COMMAND_TEXT)
        .at(1, 1);
  }
}
