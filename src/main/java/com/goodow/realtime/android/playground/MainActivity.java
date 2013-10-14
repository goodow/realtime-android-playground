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

import com.goodow.realtime.Document;
import com.goodow.realtime.DocumentLoadedHandler;
import com.goodow.realtime.Model;
import com.goodow.realtime.ModelInitializerHandler;
import com.goodow.realtime.Realtime;

import com.google.api.client.http.HttpTransport;

import java.util.logging.Level;
import java.util.logging.Logger;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import roboguice.activity.RoboActivity;
import roboguice.inject.InjectView;

public class MainActivity extends RoboActivity {
  static {
    // To enable logging of HTTP requests and responses (including URL,
    // headers, and content)
    Logger.getLogger(HttpTransport.class.getName()).setLevel(Level.CONFIG);
  }
  @InjectView(R.id.userId)
  EditText userIdText;
  @InjectView(R.id.accessToken)
  EditText accessTokenText;
  @InjectView(R.id.docId)
  EditText docIdText;

  /**
   * CollaborativeLists
   * 
   * @param view
   */
  public void collaborativeListsButton(View view) {
    startActivity(new Intent(this, CollaborativeListsActivity.class));
  }

  /**
   * CollaborativeMaps
   * 
   * @param view
   */
  public void collaborativeMapsButton(View view) {
    startActivity(new Intent(this, CollaborativeMapsActivity.class));
  }

  /**
   * CollaborativeStrings
   * 
   * @param view
   */
  public void collaborativeStringsButton(View view) {
    startActivity(new Intent(this, CollaborativeStringsActivity.class));
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    userIdText.setText(ConstantValues.userId);
    accessTokenText.setText(ConstantValues.accessToken);
    docIdText.setText(ConstantValues.documentId);
    Realtime.authorize(userIdText.getText().toString(), accessTokenText.getText().toString());
    DocumentLoadedHandler onLoaded = new DocumentLoadedHandler() {

      @Override
      public void onLoaded(Document document) {

      }
    };
    ModelInitializerHandler opt_initializer = new ModelInitializerHandler() {
      @Override
      public void onInitializer(Model model) {
        CollaborativeStringsActivity.initializeModel(model);
        CollaborativeListsActivity.initializeModel(model);
        CollaborativeMapsActivity.initializeModel(model);
      }
    };
    Realtime.load(ConstantValues.userId, onLoaded, opt_initializer, null);

  }
}