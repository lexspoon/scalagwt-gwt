/*
 * Copyright 2009 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.google.gwt.uibinder.elementparsers;

import com.google.gwt.core.ext.UnableToCompleteException;

import junit.framework.TestCase;

import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.Iterator;

/**
 * A unit test. Guess what of.
 */
public class LayoutPanelParserTest extends TestCase {

  private static final String PARSED_TYPE = "com.google.gwt.user.client.ui.LayoutPanel";

  private ElementParserTester tester;

  @Override
  public void setUp() throws Exception {
    super.setUp();
    tester = new ElementParserTester(PARSED_TYPE, new LayoutPanelParser());
  }

  public void testBadChild() throws SAXException, IOException {
    StringBuffer b = new StringBuffer();
    b.append("<g:LayoutPanel>");
    b.append("  <g:blah/>");
    b.append("</g:LayoutPanel>");

    try {
      tester.parse(b.toString());
      fail();
    } catch (UnableToCompleteException e) {
      assertTrue("expect \"only g:layer\" error",
          tester.logger.died.contains("only <g:layer> children"));
    }
  }

  public void testBadValue() throws SAXException, IOException {
    StringBuffer b = new StringBuffer();
    b.append("<g:LayoutPanel>");
    b.append("  <g:layer left='goosnarg'><g:HTML/></g:layer>");
    b.append("</g:LayoutPanel>");

    try {
      tester.parse(b.toString());
      fail();
    } catch (UnableToCompleteException e) {
      assertTrue("expect \"Unable to parse\" error",
          tester.logger.died.contains("Unable to parse"));
    }
  }

  public void testHappy() throws UnableToCompleteException, SAXException,
      IOException {
    StringBuffer b = new StringBuffer();
    b.append("<g:LayoutPanel>");
    b.append("  <g:layer>");
    b.append("    <g:Label id='foo0'>nada</g:Label>");
    b.append("  </g:layer>");

    b.append("  <g:layer left='1em' width='1px'>");
    b.append("    <g:Label id='foo1'>left-width</g:Label>");
    b.append("  </g:layer>");
    b.append("  <g:layer right='1em' width='1px'>");
    b.append("    <g:Label id='foo2'>right-width</g:Label>");
    b.append("  </g:layer>");
    b.append("  <g:layer left='1em' right='1px'>");
    b.append("    <g:Label id='foo3'>left-right</g:Label>");
    b.append("  </g:layer>");

    b.append("  <g:layer top='1em' height='50%'>");
    b.append("    <g:Label id='foo4'>top-height</g:Label>");
    b.append("  </g:layer>");
    b.append("  <g:layer bottom='1em' height='50%'>");
    b.append("    <g:Label id='foo5'>bottom-height</g:Label>");
    b.append("  </g:layer>");
    b.append("  <g:layer top='1em' bottom='50%'>");
    b.append("    <g:Label id='foo6'>top-bottom</g:Label>");
    b.append("  </g:layer>");

    b.append("  <g:layer top='{foo.value}em' height='50{foo.unit}'>");
    b.append("    <g:Label id='foo7'>top-height</g:Label>");
    b.append("  </g:layer>");
    b.append("</g:LayoutPanel>");

    String[] expected = {
        "fieldName.add(<g:Label id='foo0'>);",

        "fieldName.add(<g:Label id='foo1'>);",
        "fieldName.setWidgetLeftWidth(<g:Label id='foo1'>, 1, "
            + "com.google.gwt.dom.client.Style.Unit.EM, 1, com.google.gwt.dom.client.Style.Unit.PX);",
        "fieldName.add(<g:Label id='foo2'>);",
        "fieldName.setWidgetRightWidth(<g:Label id='foo2'>, 1, "
            + "com.google.gwt.dom.client.Style.Unit.EM, 1, com.google.gwt.dom.client.Style.Unit.PX);",
        "fieldName.add(<g:Label id='foo3'>);",
        "fieldName.setWidgetLeftRight(<g:Label id='foo3'>, 1, "
            + "com.google.gwt.dom.client.Style.Unit.EM, 1, com.google.gwt.dom.client.Style.Unit.PX);",

        "fieldName.add(<g:Label id='foo4'>);",
        "fieldName.setWidgetTopHeight(<g:Label id='foo4'>, 1, "
            + "com.google.gwt.dom.client.Style.Unit.EM, 50, com.google.gwt.dom.client.Style.Unit.PCT);",
        "fieldName.add(<g:Label id='foo5'>);",
        "fieldName.setWidgetBottomHeight(<g:Label id='foo5'>, 1, "
            + "com.google.gwt.dom.client.Style.Unit.EM, 50, com.google.gwt.dom.client.Style.Unit.PCT);",
        "fieldName.add(<g:Label id='foo6'>);",
        "fieldName.setWidgetTopBottom(<g:Label id='foo6'>, 1, "
            + "com.google.gwt.dom.client.Style.Unit.EM, 50, com.google.gwt.dom.client.Style.Unit.PCT);",

        "fieldName.add(<g:Label id='foo7'>);",
        "fieldName.setWidgetTopHeight(<g:Label id='foo7'>, (double)foo.value(), "
            + "com.google.gwt.dom.client.Style.Unit.EM, 50, foo.unit());" };

    tester.parse(b.toString());

    Iterator<String> i = tester.writer.statements.iterator();
    for (String e : expected) {
      assertEquals(e, i.next());
    }
    assertFalse(i.hasNext());
    assertNull(tester.logger.died);
  }

  public void testLonelyBottom() throws SAXException, IOException {
    StringBuffer b = new StringBuffer();
    b.append("<g:LayoutPanel>");
    b.append("  <g:layer bottom='0'><g:HTML/></g:layer>");
    b.append("</g:LayoutPanel>");

    try {
      tester.parse(b.toString());
      fail();
    } catch (UnableToCompleteException e) {
      assertTrue("expect \"must be paired\" error",
          tester.logger.died.contains("must be paired"));
    }
  }

  public void testLonelyLeft() throws SAXException, IOException {
    StringBuffer b = new StringBuffer();
    b.append("<g:LayoutPanel>");
    b.append("  <g:layer left='0'><g:HTML/></g:layer>");
    b.append("</g:LayoutPanel>");

    try {
      tester.parse(b.toString());
      fail();
    } catch (UnableToCompleteException e) {
      assertTrue("expect \"must be paired\" error",
          tester.logger.died.contains("must be paired"));
    }
  }

  public void testLonelyRight() throws SAXException, IOException {
    StringBuffer b = new StringBuffer();
    b.append("<g:LayoutPanel>");
    b.append("  <g:layer right='0'><g:HTML/></g:layer>");
    b.append("</g:LayoutPanel>");

    try {
      tester.parse(b.toString());
      fail();
    } catch (UnableToCompleteException e) {
      assertTrue("expect \"must be paired\" error",
          tester.logger.died.contains("must be paired"));
    }
  }

  public void testLonelyTop() throws SAXException, IOException {
    StringBuffer b = new StringBuffer();
    b.append("<g:LayoutPanel>");
    b.append("  <g:layer top='0'><g:HTML/></g:layer>");
    b.append("</g:LayoutPanel>");

    try {
      tester.parse(b.toString());
      fail();
    } catch (UnableToCompleteException e) {
      assertTrue("expect \"must be paired\" error",
          tester.logger.died.contains("must be paired"));
    }
  }

  public void testOverConstrained() throws SAXException, IOException {
    StringBuffer b = new StringBuffer();
    b.append("<g:LayoutPanel>");
    b.append("  <g:layer left='0' width='0' right='0'><g:HTML/></g:layer>");
    b.append("</g:LayoutPanel>");

    try {
      tester.parse(b.toString());
      fail();
    } catch (UnableToCompleteException e) {
      assertTrue("expect \"too many\" error",
          tester.logger.died.contains("too many"));
    }
  }
}
