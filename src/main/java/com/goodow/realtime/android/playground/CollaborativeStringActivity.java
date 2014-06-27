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
import android.text.Editable;
import android.text.TextWatcher;
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
import com.goodow.realtime.store.ObjectChangedEvent;
import com.goodow.realtime.store.Store;

public class CollaborativeStringActivity extends Activity {

  public static void initializeModel(Model mod) {
    CollaborativeString string = mod.createString("Edit Me!");
    mod.getRoot().set(STR_KEY, string);
  }

  private Store store = StoreProvider.get();
  private Document doc;
  private Model mod;
  private CollaborativeMap root;
  private EditText stringText;
  private ProgressBar pbIndeterminate;
  private boolean autoClose;

  private static final String STR_KEY = "demo_string";
  private final Handler<DocumentSaveStateChangedEvent> saveStateHandler =
      new Handler<DocumentSaveStateChangedEvent>() {
        @Override
        public void handle(DocumentSaveStateChangedEvent event) {
          if (event.isSaving() || event.isPending()) {
            pbIndeterminate.setVisibility(View.VISIBLE);
          } else {
            pbIndeterminate.setVisibility(View.GONE);
          }
        }
      };

  private final RealtimeModel stringModel = new RealtimeModel() {

    private CollaborativeString str;

    @Override
    public void connectRealtime() {
      str.onObjectChanged(new Handler<ObjectChangedEvent>() {
        @Override
        public void handle(ObjectChangedEvent event) {
          if (!event.isLocal()) {
            updateUi();
          }
        }
      });
    }

    @Override
    public void connectUi() {
      stringText.addTextChangedListener(new TextWatcher() {
        @Override
        public void afterTextChanged(Editable arg0) {
        }

        @Override
        public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
        }

        @Override
        public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
          str.setText(stringText.getText().toString());
        }
      });
    }

    @Override
    public void loadField() {
      str = root.get(STR_KEY);
    }

    @Override
    public void updateUi() {
      stringText.setText(str.getText());
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

    stringText = (EditText) findViewById(R.id.editText);
    pbIndeterminate = (ProgressBar) findViewById(R.id.pb_indeterminate);

    ActionBar actionBar = this.getActionBar();
    actionBar.setTitle("CollaborativeString Demo");
  }

  @Override
  protected void onPause() {
    super.onPause();
    if(doc != null) {
      doc.close();
      doc = null;
      autoClose = false;
    } else {
      autoClose = true;
    }
  }

  @Override
  protected void onResume() {
    super.onResume();

    Handler<Document> onLoaded = new Handler<Document>() {
      @Override
      public void handle(Document document) {
        pbIndeterminate.setVisibility(View.GONE);
        document.onDocumentSaveStateChanged(saveStateHandler);

        doc = document;
        if(autoClose){
          doc.close();
          doc = null;
          autoClose = false;
          return;
        }
        mod = doc.getModel();
        root = mod.getRoot();

        connectString();
      }
    };
    pbIndeterminate.setVisibility(View.VISIBLE);
    store.load(MainActivity.ID, onLoaded, null, null);
  }

  private void connectString() {
    stringModel.loadField();
    stringModel.updateUi();
    stringModel.connectUi();
    stringModel.connectRealtime();
  }
}
