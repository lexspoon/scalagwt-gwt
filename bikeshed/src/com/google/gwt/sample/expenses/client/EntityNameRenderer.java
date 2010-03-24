/*
 * Copyright 2010 Google Inc.
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
package com.google.gwt.sample.expenses.client;

import com.google.gwt.sample.expenses.shared.EmployeeKey;
import com.google.gwt.sample.expenses.shared.ExpensesEntityKey;
import com.google.gwt.sample.expenses.shared.ExpensesEntityFilter;
import com.google.gwt.sample.expenses.shared.ReportKey;
import com.google.gwt.user.client.ui.Renderer;

/**
 * Renders the name of {@link ExpensesEntityKey}s.
 */
//TODO i18n
public class EntityNameRenderer implements Renderer<ExpensesEntityKey<?>> {
  private final ExpensesEntityFilter<String> filter = new ExpensesEntityFilter<String>() {
    public String filter(EmployeeKey employeeKey) {
      return "Employees";
    }

    public String filter(ReportKey reportKey) {
      return "Reports";
    }
  };

  public String render(ExpensesEntityKey<?> entity) {
    return entity.accept(filter);
  }
}
