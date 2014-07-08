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

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import com.goodow.realtime.core.Handler;
import com.goodow.realtime.store.Document;
import com.goodow.realtime.store.DocumentSaveStateChangedEvent;
import com.goodow.realtime.store.Model;
import com.goodow.realtime.store.UndoRedoStateChangedEvent;

public class Utils {
  public static void autoSetUndoRedoByDoc(Menu menu, Document doc, final RealtimeModel realtimeModel) {
    final Model model = doc.getModel();
    final MenuItem menu_undo = menu.findItem(R.id.menu_undo);
    final MenuItem menu_redo = menu.findItem(R.id.menu_redo);
    menu_undo.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
      @Override
      public boolean onMenuItemClick(MenuItem item) {
        model.undo();
        realtimeModel.updateUi();
        return false;
      }
    });
    menu_redo.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
      @Override
      public boolean onMenuItemClick(MenuItem item) {
        model.redo();
        realtimeModel.updateUi();
        return false;
      }
    });
    model.onUndoRedoStateChanged(new Handler<UndoRedoStateChangedEvent>() {
      @Override
      public void handle(UndoRedoStateChangedEvent event) {
        menu_redo.setEnabled(event.canRedo());
        menu_undo.setEnabled(event.canUndo());

      }
    });
  }

  public static void autoSetProgressBarByDoc(final ProgressBar progressBar, Document doc) {
    progressBar.setVisibility(View.INVISIBLE);
    doc.onDocumentSaveStateChanged(new Handler<DocumentSaveStateChangedEvent>() {
      @Override
      public void handle(DocumentSaveStateChangedEvent event) {
        if (event.isSaving() || event.isPending()) {
          progressBar.setVisibility(View.VISIBLE);
        } else {
          progressBar.setVisibility(View.INVISIBLE);
        }
      }
    });
  }

}
