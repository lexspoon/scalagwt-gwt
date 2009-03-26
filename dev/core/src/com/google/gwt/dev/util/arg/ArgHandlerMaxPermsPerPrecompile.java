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
package com.google.gwt.dev.util.arg;

import com.google.gwt.util.tools.ArgHandlerInt;

/**
 * Handles an argument for {@link OptionMaxPermsPerPrecompile}.
 */
public class ArgHandlerMaxPermsPerPrecompile extends ArgHandlerInt {

  private final OptionMaxPermsPerPrecompile options;

  public ArgHandlerMaxPermsPerPrecompile(OptionMaxPermsPerPrecompile options) {
    this.options = options;
  }

  @Override
  public String[] getDefaultArgs() {
    return new String[] {getTag(), "-1"};
  }

  @Override
  public String getPurpose() {
    return "maximum permutations to compile at a time";
  }

  @Override
  public String getTag() {
    return "-XmaxPermsPerPrecompile";
  }

  @Override
  public String[] getTagArgs() {
    return new String[] {"perms"};
  }

  @Override
  public boolean isUndocumented() {
    return true;
  }

  @Override
  public void setInt(int maxPerms) {
    options.setMaxPermsPerPrecompile(maxPerms);
  }
}
