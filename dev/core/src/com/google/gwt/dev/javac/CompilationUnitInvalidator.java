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
package com.google.gwt.dev.javac;

import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.TreeLogger.HelpInfo;
import com.google.gwt.core.ext.TreeLogger.Type;
import com.google.gwt.dev.util.Util;
import com.google.gwt.dev.util.collect.Lists;

import org.eclipse.jdt.core.compiler.CategorizedProblem;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Helper class to invalidate units in a set based on errors or references to
 * other invalidate units.
 * 
 * TODO: ClassFileReader#hasStructuralChanges(byte[]) could help us optimize
 * this process!
 */
public class CompilationUnitInvalidator {

  @SuppressWarnings("deprecation")
  public static void reportErrors(TreeLogger logger, CompilationUnit unit) {
    TreeLogger branch = reportErrors(logger, unit.getProblems(),
        unit.getDisplayLocation(), unit.isError());
    if (branch != null) {
      Util.maybeDumpSource(branch, unit.getDisplayLocation(), unit.getSource(),
          unit.getTypeName());
    }
  }

  public static void retainValidUnits(TreeLogger logger,
      Collection<CompilationUnit> units) {
    retainValidUnits(logger, units, Collections.<ContentId> emptySet());
  }

  public static void retainValidUnits(TreeLogger logger,
      Collection<CompilationUnit> units, Set<ContentId> knownValidRefs) {
    logger = logger.branch(TreeLogger.TRACE, "Removing invalidated units");

    // Assume all units are valid at first.
    Set<CompilationUnit> currentlyValidUnits = new LinkedHashSet<CompilationUnit>();
    Set<ContentId> currentlyValidRefs = new HashSet<ContentId>(knownValidRefs);
    for (CompilationUnit unit : units) {
      if (!unit.isError()) {
        currentlyValidUnits.add(unit);
        currentlyValidRefs.add(unit.getContentId());
      }
    }

    boolean changed;
    do {
      changed = false;
      for (Iterator<CompilationUnit> it = currentlyValidUnits.iterator(); it.hasNext();) {
        CompilationUnit unitToCheck = it.next();
        List<String> invalidRefs = Lists.create();
        for (ContentId ref : unitToCheck.getDependencies()) {
          if (!currentlyValidRefs.contains(ref)) {
            invalidRefs = Lists.add(invalidRefs, ref.get());
          }
        }
        if (invalidRefs.size() > 0) {
          it.remove();
          currentlyValidRefs.remove(unitToCheck.getContentId());
          changed = true;
          TreeLogger branch = logger.branch(TreeLogger.DEBUG,
              "Compilation unit '" + unitToCheck
                  + "' is removed due to invalid reference(s):");
          Lists.sort(invalidRefs);
          for (String ref : invalidRefs) {
            branch.log(TreeLogger.DEBUG, ref);
          }
        }
      }
    } while (changed);

    units.retainAll(currentlyValidUnits);
  }

  private static TreeLogger reportErrors(TreeLogger logger,
      CategorizedProblem[] problems, String fileName, boolean isError) {
    if (problems == null || problems.length == 0) {
      return null;
    }
    TreeLogger branch = null;
    // Log the errors and GWT warnings.
    for (CategorizedProblem problem : problems) {
      TreeLogger.Type logLevel;
      if (problem.isError()) {
        // Log errors.
        logLevel = TreeLogger.ERROR;
        // Only log GWT-specific warnings.
      } else if (problem.isWarning() && problem instanceof GWTProblem) {
        logLevel = TreeLogger.WARN;
      } else {
        // Ignore all other problems.
        continue;
      }
      // Append 'Line #: msg' to the error message.
      StringBuffer msgBuf = new StringBuffer();
      int line = problem.getSourceLineNumber();
      if (line > 0) {
        msgBuf.append("Line ");
        msgBuf.append(line);
        msgBuf.append(": ");
      }
      msgBuf.append(problem.getMessage());

      HelpInfo helpInfo = null;
      if (problem instanceof GWTProblem) {
        GWTProblem gwtProblem = (GWTProblem) problem;
        helpInfo = gwtProblem.getHelpInfo();
      }
      if (branch == null) {
        Type branchType = isError ? TreeLogger.ERROR : TreeLogger.WARN;
        String branchString = isError ? "Errors" : "Warnings";
        branch = logger.branch(branchType, branchString + " in '" + fileName
            + "'", null);
      }
      branch.log(logLevel, msgBuf.toString(), null, helpInfo);
    }
    return branch;
  }
}
