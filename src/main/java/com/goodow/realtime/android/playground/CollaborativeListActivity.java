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
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.goodow.realtime.core.Handler;
import com.goodow.realtime.json.Json;
import com.goodow.realtime.store.CollaborativeList;
import com.goodow.realtime.store.Model;
import com.goodow.realtime.store.ObjectChangedEvent;

public class CollaborativeListActivity extends BaseActivity {

  private class ListAdapter extends BaseAdapter {
    private CollaborativeList collaborativeList;

    public void setCollaborativeList(CollaborativeList collaborativeList) {
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
      if(collaborativeList != null) {
        return collaborativeList.length();
      } else {
        return 0;
      }
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
      TextView textView;
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

    public void setValue(int postion, String item) {
      this.collaborativeList.set(postion, item);
      this.notifyDataSetChanged();
    }
  }

  public static void initializeModel(Model mod) {
    CollaborativeList list = mod.createList(
        Json.createArray().push("Cat").push("Dog").push("Sheep").push("Chicken"));
    mod.getRoot().set(LIST_KEY, list);
  }

  private int currentSelected = -1;

  private ListView listView;
  private EditText toSet;
  private EditText toAdd;
  private Button bt_addAnItem;
  private Button bt_removeSelection;
  private Button bt_clearList;
  private Button bt_setSelected;
  private ListAdapter adapter;

  private static final String LIST_KEY = "demo_list";

  private class ListModel implements RealtimeModel {
    private CollaborativeList list;

    @Override
    public void connectRealtime() {
      list.onObjectChanged(new Handler<ObjectChangedEvent>() {
        @Override
        public void handle(ObjectChangedEvent event) {
          if(!event.isLocal()) {
            updateUi();
          }
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
            adapter.removeItem(currentSelected);
            currentSelected = -1;
          }
        }
      });

      // clear
      bt_clearList.setOnClickListener(new OnClickListener() {

        @Override
        public void onClick(View v) {
          if (list.length() != 0) {
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
      adapter.setCollaborativeList((CollaborativeList) root.get(LIST_KEY));
      adapter.notifyDataSetChanged();
    }
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    setContentView(R.layout.activity_collaborativelist);
    super.onCreate(savedInstanceState);

    listView = (ListView) findViewById(R.id.CollaborativeList);
    toSet = (EditText) findViewById(R.id.selectItem);
    toAdd = (EditText) findViewById(R.id.AddAnItem);
    bt_addAnItem = (Button) findViewById(R.id.bt_addAnItem);
    bt_removeSelection = (Button) findViewById(R.id.bt_removeSelectItem);
    bt_clearList = (Button) findViewById(R.id.bt_clearTheList);
    bt_setSelected = (Button) findViewById(R.id.bt_setSelectItem);

    actionBar.setTitle("CollabrativeList Demo");

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
    adapter = new ListAdapter();
    listView.setAdapter(adapter);
    model = new ListModel();
  }

}
