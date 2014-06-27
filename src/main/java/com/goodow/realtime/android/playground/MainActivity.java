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

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import com.goodow.realtime.core.Handler;
import com.goodow.realtime.store.Document;
import com.goodow.realtime.store.Model;
import com.goodow.realtime.store.Store;

public class MainActivity extends Activity {
  static final String ID = "playground/0";
  private Store store;
  private Document doc;
  private EditText userIdText;
  private EditText accessTokenText;
  private EditText docIdText;
  private boolean active = false;

  /**
   * CollaborativeList
   */
  public void collaborativeListsButton(View view) {
    startActivity(new Intent(this, CollaborativeListActivity.class));
  }

  /**
   * CollaborativeMap
   */
  public void collaborativeMapsButton(View view) {
    startActivity(new Intent(this, CollaborativeMapActivity.class));
  }

  /**
   * CollaborativeString
   */
  public void collaborativeStringsButton(View view) {
    startActivity(new Intent(this, CollaborativeStringActivity.class));
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    store = StoreProvider.get();

    userIdText = (EditText) findViewById(R.id.userId);
    accessTokenText = (EditText) findViewById(R.id.accessToken);
    docIdText = (EditText) findViewById(R.id.docId);
    docIdText.setText(ID);
  }

  @Override
  protected void onPause() {
    super.onPause();

    active = false;
    if (doc != null) {
      doc.close();
    }
  }

  @Override
  protected void onResume() {
    super.onResume();
    active = true;

    Handler<Document> onLoaded = new Handler<Document>() {
      @Override
      public void handle(Document document) {
        if (!active) {
          document.close();
          return;
        }
        doc = document;
      }
    };
    Handler<Model> opt_initializer = new Handler<Model>() {
      @Override
      public void handle(Model model) {
        CollaborativeStringActivity.initializeModel(model);
        CollaborativeListActivity.initializeModel(model);
        CollaborativeMapActivity.initializeModel(model);
      }
    };
    store.load(docIdText.getText().toString(), onLoaded, opt_initializer, null);
  }
}