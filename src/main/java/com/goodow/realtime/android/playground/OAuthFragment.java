/*
 * Copyright 2012 Goodow.com
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.goodow.realtime.android.playground;

import android.app.DialogFragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class OAuthFragment extends DialogFragment {

  private static class MyWebViewClient extends WebViewClient {
    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
      // check if the login was successful and the access token returned
      // this test depend of your API
      if (url.contains("access_token=")) {
        // save your token
        return true;
      }
      Log.i(MyWebViewClient.class.getName(), "Login Failed");
      return false;
    }
  }

  private WebView webViewOauth;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    // Retrieve the webview
    View v = inflater.inflate(R.layout.oauth_screen, container, false);
    webViewOauth = (WebView) v.findViewById(R.id.web_oauth);
    getDialog().setTitle("登录");
    return v;
  }

  @Override
  public void onViewCreated(View arg0, Bundle arg1) {
    super.onViewCreated(arg0, arg1);
    // load the url of the oAuth login page
    webViewOauth.loadUrl("http://retech.goodow.com/good.js/good/auth/ServiceLogin.html");
    // set the web client
    webViewOauth.setWebViewClient(new MyWebViewClient());
    // activates JavaScript (just in case)
    WebSettings webSettings = webViewOauth.getSettings();
    webSettings.setJavaScriptEnabled(true);
  }
}