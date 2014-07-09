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

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;

import com.goodow.realtime.core.Handler;
import com.goodow.realtime.store.CollaborativeString;
import com.goodow.realtime.store.IndexReference;
import com.goodow.realtime.store.Model;
import com.goodow.realtime.store.TextDeletedEvent;
import com.goodow.realtime.store.TextInsertedEvent;

public class CollaborativeStringActivity extends BaseActivity {

  public static void initializeModel(Model mod) {
    CollaborativeString string = mod.createString("Edit Me!");
    mod.getRoot().set(STR_KEY, string);
  }

  private IndexReference cursorStart;
  private IndexReference cursorEnd;
  private CursorEditText stringText;

  private static final String STR_KEY = "demo_string";

  private class StringModel implements RealtimeModel {
    private CollaborativeString str;
    private boolean isTextChangedLocal = true;
    private boolean isCursorChangedLocal = true;

    @Override
    public void connectRealtime() {
      str.onTextDeleted(new Handler<TextDeletedEvent>() {
        @Override
        public void handle(TextDeletedEvent event) {
          if(!event.isLocal()) {
            isTextChangedLocal = false;
            isCursorChangedLocal = false;
            stringText.getText().delete(event.index(), event.index() + event.text().length());
            reSelectText();
          }
        }
      });
      str.onTextInserted(new Handler<TextInsertedEvent>() {
        @Override
        public void handle(TextInsertedEvent event) {
          if(!event.isLocal()) {
            isTextChangedLocal = false;
            isCursorChangedLocal = false;
            stringText.getText().insert(event.index(), event.text());
            reSelectText();
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
        public void onTextChanged(CharSequence s, int start, int before, int count) {
          if (!isTextChangedLocal) {
            isTextChangedLocal = true;
            return;
          }
          str.setText(stringText.getText().toString());
        }

      });
      stringText.setOnCusorChangedListener(new CursorEditText.OnCursorChangedListener() {
        @Override
        public void onCursorChanged(int startIndex, int endIndex) {
          if (!isCursorChangedLocal) {
            isCursorChangedLocal = true;
            return;
          }
          cursorStart.setIndex(startIndex);
          cursorEnd.setIndex(endIndex);
        }
      });
    }

    @Override
    public void loadField() {
      str = root.get(STR_KEY);
      cursorStart = str.registerReference(0, false);
      cursorEnd = str.registerReference(0, false);
    }

    @Override
    public void updateUi() {
      stringText.setText(str.getText());
    }

    private void reSelectText() {
      stringText.setSelection(guardedCursor(cursorStart.index()), guardedCursor(cursorEnd.index()));
      if(stringText.getSelectionStart() != stringText.getSelectionEnd()) {
        stringText.performLongClick();
      }
    }

    private int guardedCursor(int cursor) {
      return cursor < 0 ? 0 : cursor;
    }
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    setContentView(R.layout.activity_collaborativestring);
    super.onCreate(savedInstanceState);

    stringText = (CursorEditText) findViewById(R.id.editText);

    actionBar.setTitle("CollaborativeString Demo");
    model = new StringModel();
  }

}
