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

import android.app.ActionBar;
import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.goodow.realtime.core.Handler;
import com.goodow.realtime.store.CollaborativeMap;
import com.goodow.realtime.store.CollaborativeString;
import com.goodow.realtime.store.Document;
import com.goodow.realtime.store.DocumentSaveStateChangedEvent;
import com.goodow.realtime.store.Model;
import com.goodow.realtime.store.UndoRedoStateChangedEvent;
import com.goodow.realtime.store.databinding.Databinding;

public class CollaborativeStringActivity extends Activity {

  public static void initializeModel(Model mod) {
    CollaborativeString string = mod.createString("Edit Me!");
    mod.getRoot().set(STR_KEY, string);
  }

  private static final String STR_KEY = "demo_string";
  private Document doc;
  private Model mod;
  private CollaborativeMap root;
  private EditText textView;
  private ProgressBar pbIndeterminate;
  private boolean active = false;

  private final RealtimeModel stringModel = new RealtimeModel() {
    private CollaborativeString str;

    @Override
    public void connectRealtime() {
    }

    @Override
    public void connectUi() {
      Databinding.bindString(str, textView);
    }

    @Override
    public void loadField() {
      str = root.get(STR_KEY);
    }

    @Override
    public void updateUi() {
      textView.setText(str.getText());
    }
  };

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.main, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case R.id.menu_undo:
        if (mod.canUndo()) {
          mod.undo();
          stringModel.updateUi();
        }
        break;
      case R.id.menu_redo:
        if (mod.canRedo()) {
          mod.redo();
          stringModel.updateUi();
        }
        break;
    }
    return super.onOptionsItemSelected(item);
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_collaborativestring);

    textView = (EditText) findViewById(R.id.editText);
    pbIndeterminate = (ProgressBar) findViewById(R.id.pb_indeterminate);

    ActionBar actionBar = this.getActionBar();
    actionBar.setTitle("CollaborativeString Demo");
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
        mod = doc.getModel();
        root = mod.getRoot();
        connectString();

        pbIndeterminate.setVisibility(View.GONE);
        doc.onDocumentSaveStateChanged(new Handler<DocumentSaveStateChangedEvent>() {
          @Override
          public void handle(DocumentSaveStateChangedEvent event) {
            if (event.isSaving() || event.isPending()) {
              pbIndeterminate.setVisibility(View.VISIBLE);
            } else {
              pbIndeterminate.setVisibility(View.GONE);
            }
          }
        });
        mod.onUndoRedoStateChanged(new Handler<UndoRedoStateChangedEvent>() {
          @Override
          public void handle(UndoRedoStateChangedEvent event) {
            event.canRedo();
          }
        });
      }
    };
    pbIndeterminate.setVisibility(View.VISIBLE);
    StoreProvider.get().load(MainActivity.ID, onLoaded, null, null);
  }

  private void connectString() {
    stringModel.loadField();
    stringModel.updateUi();
    stringModel.connectUi();
    stringModel.connectRealtime();
  }
}
