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

// plugin.cpp : Implementation of Cplugin
#include "stdafx.h"
#include "comutil.h"
#include "plugin.h"
#include "BrowserChannel.h"
#include "JavaObject.h"
#include "HostChannel.h"
#include "IESessionHandler.h"
#include "LoadModuleMessage.h"
#include "ServerMethods.h"

// Cplugin

STDMETHODIMP Cplugin::connect(BSTR burl, BSTR sessionKey, BSTR bhostedServer,
    BSTR bmoduleName, BSTR bhostedHtmlVersion, VARIANT_BOOL* ret)
{
  // TODO: Add your implementation code here
  LPOLECLIENTSITE site;
  IOleContainer* container = NULL;
  IHTMLDocument2* doc = NULL;
  IHTMLWindow2* window = NULL;

  this->GetClientSite(&site);
  site->GetContainer(&container);
  container->QueryInterface(IID_IHTMLDocument2, (void**)&doc);
  container->Release();

  doc->get_parentWindow(&window);
  doc->Release();

  std::string hostedServer = BSTRToUTF8(bhostedServer);
  size_t index = hostedServer.find(':');
  if (index == std::string::npos) {
    *ret = false;
    return S_OK;
  }
  std::string hostPart = hostedServer.substr(0, index);
  std::string portPart = hostedServer.substr(index + 1);

  HostChannel* channel = new HostChannel();

  if (!channel->connectToHost(
      hostPart.c_str(),
      atoi(portPart.c_str()))) {
    *ret = false;
    return S_OK;
  }

  sessionHandler.reset(new IESessionHandler(channel, window));

  std::string hostedHtmlVersion = BSTRToUTF8(bhostedHtmlVersion);

  // TODO: add support for a range of protocol versions when we add them
  if (!channel->init(sessionHandler.get(), BROWSERCHANNEL_PROTOCOL_VERSION,
      BROWSERCHANNEL_PROTOCOL_VERSION, hostedHtmlVersion)) {
    *ret = false;
    return S_OK;
  }

  std::string url = BSTRToUTF8(burl);
  std::string tabKey = ""; // TODO(jat): add support for tab identity
  std::string sessionKey = BSTRToUTF8(bsessionKey);
  std::string moduleName = BSTRToUTF8(bmoduleName);
  IOmNavigator* navigator;
  _bstr_t userAgent;

  window->get_navigator(&navigator);
  navigator->get_userAgent(userAgent.GetAddress());

  LoadModuleMessage::send(*channel, url, tabKey, sessionKey, moduleName,
      BSTRToUTF8(userAgent), sessionHandler.get());

  navigator->Release();

  *ret = true;
  return S_OK;
}

STDMETHODIMP CPlugin::init(IDispatch* jsniContext, VARIANT_BOOL* ret) {
  *ret = true;
  return S_OK;
}

STDMETHODIMP Cplugin::testObject(IDispatch** ret)
{
  IJavaObject* toRet;
  CJavaObject::CreateInstance(&toRet);
  *ret = toRet;
  // TODO: Add your implementation code here

  return S_OK;
}
