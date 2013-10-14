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

import com.goodow.realtime.CollaborativeList;
import com.goodow.realtime.CollaborativeMap;
import com.goodow.realtime.Document;
import com.goodow.realtime.DocumentLoadedHandler;
import com.goodow.realtime.DocumentSaveStateChangedEvent;
import com.goodow.realtime.EventHandler;
import com.goodow.realtime.Model;
import com.goodow.realtime.ObjectChangedEvent;
import com.goodow.realtime.Realtime;

import android.app.ActionBar;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import roboguice.activity.RoboActivity;
import roboguice.inject.InjectView;

public class CollaborativeListsActivity extends RoboActivity {

  private class ListAdapter extends BaseAdapter {
    private CollaborativeList collaborativeList;

    public ListAdapter(CollaborativeList collaborativeList) {
      super();
      this.collaborativeList = collaborativeList;
    }

    // add a item
    public void addItem(String item) {
      this.collaborativeList.push(item);
      this.notifyDataSetChanged();
    }

    public void clear() {
      this.collaborativeList.clear();
      this.notifyDataSetChanged();
    }

    @Override
    public int getCount() {
      int count = (collaborativeList == null ? 0 : collaborativeList.length());
      return count;
    }

    @Override
    public Object getItem(int position) {
      return collaborativeList.get(position);
    }

    @Override
    public long getItemId(int position) {
      return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
      View view;
      TextView textView = null;
      if (convertView != null) {
        view = convertView;
      } else {
        view = View.inflate(getApplicationContext(), R.layout.collaborativelists_item, null);
      }
      textView = (TextView) view.findViewById(R.id.tv_item);
      textView.setText((String) collaborativeList.get(position));
      return view;
    }

    public void removeItem(int index) {
      this.collaborativeList.remove(index);
      this.notifyDataSetChanged();
    }

    public void setCollaborativeList(CollaborativeList collaborativeList) {
      this.collaborativeList = collaborativeList;
    }

    public void setValue(int postion, String item) {
      this.collaborativeList.set(postion, item);
      this.notifyDataSetChanged();
    }
  }

  public static void initializeModel(Model mod) {
    CollaborativeList list = mod.createList();
    list.push("Hello");
    list.push("World");
    mod.getRoot().set(LIST_KEY, list);
  }

  private final EventHandler<DocumentSaveStateChangedEvent> saveStateHandler =
      new EventHandler<DocumentSaveStateChangedEvent>() {
        @Override
        public void handleEvent(DocumentSaveStateChangedEvent event) {
          if (event.isSaving || event.isPending) {
            pbIndeterminate.setVisibility(View.VISIBLE);
          } else {
            pbIndeterminate.setVisibility(View.GONE);
          }
        }
      };

  @InjectView(R.id.pb_indeterminateList)
  private ProgressBar pbIndeterminate;
  private Document doc;
  private Model mod;
  private CollaborativeMap root;

  private int currentSelected;
  @InjectView(R.id.CollaborativeList)
  ListView listView;

  @InjectView(R.id.selectItem)
  EditText toSet;
  @InjectView(R.id.AddAnItem)
  EditText toAdd;
  @InjectView(R.id.bt_addAnItem)
  Button bt_addAnItem;
  @InjectView(R.id.bt_removeSelectItem)
  Button bt_removeSelection;
  @InjectView(R.id.bt_clearTheList)
  Button bt_clearList;
  @InjectView(R.id.bt_setSelectItem)
  Button bt_setSelected;

  private static final String LIST_KEY = "demo_list";

  private final RealtimeModel listModel = new RealtimeModel() {
    private CollaborativeList list;

    @Override
    public void connectRealtime() {
      list.addObjectChangedListener(new EventHandler<ObjectChangedEvent>() {
        @Override
        public void handleEvent(ObjectChangedEvent event) {
          updateUi();
        }
      });
    }

    @Override
    public void connectUi() {
      // add an item
      bt_addAnItem.setOnClickListener(new OnClickListener() {

        @Override
        public void onClick(View v) {
          String newItem = toAdd.getText().toString();
          adapter.addItem(newItem);
        }
      });
      bt_removeSelection.setOnClickListener(new OnClickListener() {

        @Override
        public void onClick(View v) {

          if (currentSelected != -1) {
            String item = toSet.getText().toString();
            String realtimeList = list.get(currentSelected);
            // item not null
            if (list.length() > 0
                && ((item != null && item.equals(realtimeList)) || (item == null && realtimeList == null))) {
              // list.remove(selectPosition);
              adapter.removeItem(currentSelected);
              currentSelected = -1;
            }
          }
        }
      });

      // clear
      bt_clearList.setOnClickListener(new OnClickListener() {

        @Override
        public void onClick(View v) {
          if (list.length() != 0) {
            // list.clear();
            adapter.clear();
            currentSelected = -1;

          }
        }
      });
      bt_setSelected.setOnClickListener(new OnClickListener() {

        @Override
        public void onClick(View v) {
          if (currentSelected != -1) {
            String select = toSet.getText().toString();
            list.set(currentSelected, select);
            adapter.setValue(currentSelected, select);
            currentSelected = -1;
          }
        }
      });
    }

    @Override
    public void loadField() {
      list = root.get(LIST_KEY);
    }

    @Override
    public void updateUi() {
      adapter.setCollaborativeList(list);
      adapter.notifyDataSetChanged();
    }
  };

  private ListAdapter adapter;

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
        }
        break;
      case R.id.menu_redo:
        if (mod.canRedo()) {
          mod.redo();
        }
        break;
    }
    return super.onOptionsItemSelected(item);
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_collaborativelists);
    ActionBar actionBar = this.getActionBar();
    actionBar.setDisplayOptions(ActionBar.DISPLAY_HOME_AS_UP, ActionBar.DISPLAY_HOME_AS_UP);
    actionBar.setTitle("CollabrativeLists Demo");
    DocumentLoadedHandler onLoaded = new DocumentLoadedHandler() {
      @Override
      public void onLoaded(Document document) {
        pbIndeterminate.setVisibility(View.GONE);
        document.addDocumentSaveStateListener(saveStateHandler);
        doc = document;
        mod = doc.getModel();
        root = mod.getRoot();
        connectList();
      }
    };
    pbIndeterminate.setVisibility(View.VISIBLE);
    Realtime.load(ConstantValues.documentId, onLoaded, null, null);
    adapter = new ListAdapter(null);
    listView.setAdapter(adapter);
    listView.setOnItemClickListener(new OnItemClickListener() {
      @Override
      public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Object obj = listView.getItemAtPosition(position);
        // Set select item's value
        toSet.setText((String) obj);
        // save postion
        currentSelected = position;
      }
    });

  }

  private void connectList() {
    listModel.loadField();
    listModel.updateUi();
    listModel.connectUi();
    listModel.connectRealtime();
  }
}
