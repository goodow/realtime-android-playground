/*
 * Copyright 2014 Goodow.com
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

import android.app.ActionBar;
import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.ProgressBar;

import com.goodow.realtime.core.Handler;
import com.goodow.realtime.store.CollaborativeMap;
import com.goodow.realtime.store.Document;
import com.goodow.realtime.store.Model;
import com.goodow.realtime.store.Store;

public class BaseActivity extends Activity {
  protected ProgressBar pb_indeterminate;
  protected boolean active = false;
  protected Document doc;
  protected Menu menu;
  protected ActionBar actionBar;
  protected Store store = StoreProvider.get();
  protected RealtimeModel model;
  protected Model mod;
  protected CollaborativeMap root;

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.main, menu);
    this.menu = menu;
    return true;
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    pb_indeterminate = (ProgressBar) findViewById(R.id.pb_indeterminate);
    actionBar = this.getActionBar();
  }

  @Override
  protected void onResume() {
    super.onResume();
    active = true;
    pb_indeterminate.setVisibility(View.VISIBLE);
    Handler<Document> onLoaded = new Handler<Document>() {
      @Override
      public void handle(Document document) {
        if (!active) {
          document.close();
          return;
        }
        doc = document;
        mod = doc.getModel();
        root = mod.getRoot();
        Utils.autoSetProgressBarByDoc(pb_indeterminate, doc);
        Utils.autoSetUndoRedoByDoc(menu, doc, model);
        connect();
      }
    };
    store.load(MainActivity.ID, onLoaded, null, null);
  }

  @Override
  protected void onPause() {
    super.onPause();
    active = false;
    if (doc != null) {
      doc.close();
    }
  }

  protected void connect() {
    model.loadField();
    model.updateUi();
    model.connectUi();
    model.connectRealtime();
  }
}
