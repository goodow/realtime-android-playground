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

import com.goodow.realtime.CollaborativeMap;
import com.goodow.realtime.CollaborativeString;
import com.goodow.realtime.Document;
import com.goodow.realtime.DocumentLoadedHandler;
import com.goodow.realtime.EventHandler;
import com.goodow.realtime.Model;
import com.goodow.realtime.ModelInitializerHandler;
import com.goodow.realtime.ObjectChangedEvent;
import com.goodow.realtime.Realtime;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.widget.EditText;
import roboguice.activity.RoboActivity;
import roboguice.inject.InjectView;

public class MainActivity extends RoboActivity {
  private static final String STR_KEY = "demo_string";
  private Document doc;
  private Model mod;
  private CollaborativeMap root;
  private CollaborativeString str;
  @InjectView(R.id.editText)
  EditText editText;

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.main, menu);
    return true;
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    Realtime.authorize("889452220137502675385", "59e77d779ab69874b07d50f3e35d49c90cd68582");
    DocumentLoadedHandler onLoaded = new DocumentLoadedHandler() {
      @Override
      public void onLoaded(Document document) {
        doc = document;
        mod = doc.getModel();
        root = mod.getRoot();

        connectString();
      }
    };
    ModelInitializerHandler opt_initializer = new ModelInitializerHandler() {
      @Override
      public void onInitializer(Model model) {
        mod = model;
        root = mod.getRoot();
        initializeString();
      }
    };
    Realtime.load("@tmp/test", onLoaded, opt_initializer, null);
  }

  private void connectString() {
    str = root.get(STR_KEY);
    editText.addTextChangedListener(new TextWatcher() {
      @Override
      public void afterTextChanged(Editable arg0) {
      }

      @Override
      public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
      }

      @Override
      public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
        str.setText(editText.getText().toString());
      }
    });
    str.addObjectChangedListener(new EventHandler<ObjectChangedEvent>() {
      @Override
      public void handleEvent(ObjectChangedEvent event) {
        if (!event.isLocal) {
          editText.setText(str.getText());
        }
      }
    });
  }

  private void initializeString() {
    CollaborativeString string = mod.createString("Edit Me!");
    root.set(STR_KEY, string);
  }

}
