/*
 * Copyright 2011 Google Inc.
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

package com.google.template.soy.parsepasses;

import com.google.template.soy.error.ErrorReporter;
import com.google.template.soy.soytree.AbstractSoyNodeVisitor;
import com.google.template.soy.soytree.CallNode;
import com.google.template.soy.soytree.MsgPlaceholderNode;
import com.google.template.soy.soytree.PrintNode;
import com.google.template.soy.soytree.SoyNode;
import com.google.template.soy.soytree.SoyNode.MsgPlaceholderInitialNode;
import com.google.template.soy.soytree.SoyNode.ParentSoyNode;
import com.google.template.soy.soytree.SoySyntaxExceptionUtils;

/**
 * Visitor to verify that all occurrences of the 'phname' attribute are on message placeholders.
 *
 * <p> Note: Doesn't check HTML tags since we don't parse HTML tags outside of messages anyway. Only
 * checks PrintNode and CallNode.
 *
 */
public final class VerifyPhnameAttrOnlyOnPlaceholdersVisitor extends AbstractSoyNodeVisitor<Void> {

  public VerifyPhnameAttrOnlyOnPlaceholdersVisitor(ErrorReporter errorReporter) {
    super(errorReporter);
  }

  @Override protected void visitPrintNode(PrintNode node) {
    visitMsgPlaceholderInitialContentNodeHelper(node);
  }


  @Override protected void visitCallNode(CallNode node) {
    visitMsgPlaceholderInitialContentNodeHelper(node);
    visitChildren(node);
  }


  private void visitMsgPlaceholderInitialContentNodeHelper(MsgPlaceholderInitialNode node) {
    if (node.getUserSuppliedPhName() != null &&
        ! (node.getParent() instanceof MsgPlaceholderNode)) {
      throw SoySyntaxExceptionUtils.createWithNode(
          "Found 'phname' attribute not on a msg placeholder (tag " + node.toSourceString() + ").",
          node);
    }
  }


  @Override protected void visitSoyNode(SoyNode node) {
    if (node instanceof ParentSoyNode<?>) {
      visitChildren((ParentSoyNode<?>) node);
    }
  }

}
