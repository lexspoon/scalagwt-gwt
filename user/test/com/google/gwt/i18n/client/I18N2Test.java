/*
 * Copyright 2008 Google Inc.
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
package com.google.gwt.i18n.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.TestAnnotatedMessages.Nested;
import com.google.gwt.i18n.client.gen.Colors;
import com.google.gwt.i18n.client.gen.TestBadKeys;
import com.google.gwt.junit.client.GWTTestCase;

import java.util.Date;

/**
 * Test the same things as I18NTest but with a different module which
 * uses different locales.
 */
public class I18N2Test extends GWTTestCase {

  @Override
  public String getModuleName() {
    return "com.google.gwt.i18n.I18N2Test";
  }

  @SuppressWarnings("deprecation")
  public void testAnnotatedMessages() {
    TestAnnotatedMessages m = GWT.create(TestAnnotatedMessages.class);
    assertEquals("Test me", m.basicText());
    assertEquals("Once more, with meaning", m.withMeaning());
    assertEquals("One argument: one", m.oneArgument("one"));
    assertEquals("One argument, which is optional",
        m.optionalArgument("where am I?"));
    assertEquals("Two arguments, second and first, inverted",
        m.invertedArguments("first", "second"));
    assertEquals("Don't tell me I can't {quote things in braces}", m.quotedText());
    assertEquals("This {0} would be an argument if not quoted", m.quotedArg());
    assertEquals("Total is US$11,305.01", m.currencyFormat(11305.01));
    assertEquals("Default number format is 1,017.1", m.defaultNumberFormat(1017.1));
    assertEquals("It is 12:01 PM on Saturday, December 1, 2007",
        m.getTimeDate(new Date(107, 11, 1, 12, 1, 2)));
    assertEquals("13 widgets", m.pluralWidgetsOther(13));
//    assertEquals("A widget", m.pluralWidgetsOther(1));
  }
  
  public void testBadKeys() {
    TestBadKeys test = GWT.create(TestBadKeys.class);
    assertEquals("zh_spacer", test.zh_spacer());
    assertEquals("zh_spacer", test.getString("zh_spacer"));
    assertEquals("logger_org_hibernate_jdbc", test.logger_org_hibernate_jdbc());
    assertEquals("logger_org_hibernate_jdbc",
        test.getString("logger_org_hibernate_jdbc"));
    assertEquals("cell_2_5", test.cell_2_5());
    assertEquals("cell_2_5", test.getString("cell_2_5"));
    assertEquals("_level", test._level());
    assertEquals("_level", test.getString("_level"));
    assertEquals("__s", test.__s());
    assertEquals("__s", test.getString("__s"));
    assertEquals(
        "________________________________________________________________",
        test.________________________________________________________________());
    assertEquals(
        "________________________________________________________________",
        test.getString("________________________________________________________________"));
    assertEquals("_", test._());
    assertEquals("_", test.getString("_"));
    assertEquals("maven_jdiff_old_tag", test.maven_jdiff_old_tag());
    assertEquals("maven_jdiff_old_tag", test.getString("maven_jdiff_old_tag"));
    assertEquals("maven_checkstyle_properties",
        test.maven_checkstyle_properties());
    assertEquals("maven_checkstyle_properties",
        test.getString("maven_checkstyle_properties"));
    assertEquals("_1_2_3_4", test._1_2_3_4());
    assertEquals("_1_2_3_4", test.getString("_1_2_3_4"));
    assertEquals("entity_160", test.entity_160());
    assertEquals("entity_160", test.getString("entity_160"));
    assertEquals("a__b", test.a__b());
    assertEquals("a__b", test.getString("a__b"));
    assertEquals("AWT_f5", test.AWT_f5());
    assertEquals("AWT_f5", test.getString("AWT_f5"));
    assertEquals("Cursor_MoveDrop_32x32_File",
        test.Cursor_MoveDrop_32x32_File());
    assertEquals("Cursor_MoveDrop_32x32_File",
        test.getString("Cursor_MoveDrop_32x32_File"));
    assertEquals("_c_____", test._c_____());
    assertEquals("_c_____", test.getString("_c_____"));
    assertEquals("__s_dup", test.__s_dup());
    assertEquals("__s_dup", test.getString("__s_dup"));
    assertEquals("__dup", test.__dup());
    assertEquals("__dup", test.getString("__dup"));
    assertEquals("AWT_end", test.AWT_end());
    assertEquals("AWT_end", test.getString("AWT_end"));
    assertEquals("permissions_755", test.permissions_755());
    assertEquals("permissions_755", test.getString("permissions_755"));
    assertEquals("a_b_c", test.a_b_c());
    assertEquals("a_b_c", test.getString("a_b_c"));
    assertEquals("__s_dup_dup", test.__s_dup_dup());
    assertEquals("e in b_C_d", test.getString("__dup_dup"));
    assertEquals("e in b_C_d", test.__dup_dup());
    assertEquals("andStar", test.getString("__"));
    assertEquals("andStar", test.__());
  }

  public void testBinding() {
    TestBinding t = GWT.create(TestBinding.class);
    assertEquals("b_c_d", t.a());
    assertEquals("default", t.b());
  }

  public void testCheckColorsAndShapes() {
    ColorsAndShapes s = GWT.create(ColorsAndShapes.class);
    // Red comes from Colors_b_C_d
    assertEquals("red_b_C_d", s.red());
    // Blue comes from Colors_b_C
    assertEquals("blue_b_C", s.blue());
    // Yellow comes from Colors_b
    assertEquals("yellow_b", s.yellow());
    // RedSquare comes from ColorsAndShapes
    assertEquals("red square", s.redSquare());
    // Circle comes from Shapes
    assertEquals("a circle", s.circle());
  }

  /**
   * Verify that nested annotations are looked up with both A$B names
   * and A_B names.  Note that $ takes precedence and only one file for a
   * given level in the inheritance tree will be used, so A$B_locale will
   * be used and A_B_locale ignored.
   */
  public void testNestedAnnotations() {
    Nested m = GWT.create(Nested.class);
    assertEquals("nested dollar b_C", m.nestedDollar());
    assertEquals("nested underscore b", m.nestedUnderscore());
  }

  public void testWalkUpColorTree() {
    Colors colors = GWT.create(Colors.class);
    assertEquals("red_b_C_d", colors.red());
    assertEquals("blue_b_C", colors.blue());
    assertEquals("yellow_b", colors.yellow());
  }

}
