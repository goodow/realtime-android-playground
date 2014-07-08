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
import com.goodow.realtime.store.CollaborativeMap;
import com.goodow.realtime.store.Model;
import com.goodow.realtime.store.ObjectChangedEvent;

public class CollaborativeMapActivity extends BaseActivity {

  static class ViewHolder {
    TextView keys;
    TextView values;
  }

  private class MapAdapter extends BaseAdapter {
    private CollaborativeMap collaborativeMap;

    public void setCollaborativeMap(CollaborativeMap collaborativeMap) {
      this.collaborativeMap = collaborativeMap;
    }

    public void addItem(String key, String value) {
      this.collaborativeMap.set(key, value);
      this.notifyDataSetChanged();
    }

    public void clear() {
      this.collaborativeMap.clear();
      this.notifyDataSetChanged();
    }

    @Override
    public int getCount() {
      if(collaborativeMap != null) {
        return collaborativeMap.size();
      } else {
        return 0;
      }
    }

    @Override
    public Object getItem(int position) {
      String[] string = new String[2];
      string[0] = collaborativeMap.keys().getString(position);
      string[1] = collaborativeMap.get(collaborativeMap.keys().getString(position));
      return string;

    }

    @Override
    public long getItemId(int position) {
      return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
      ViewHolder holder;
      View view;
      if (convertView != null) {
        view = convertView;
        holder = (ViewHolder) view.getTag();
      } else {
        view = View.inflate(getApplicationContext(), R.layout.collaborativemaps_item, null);
        holder = new ViewHolder();
        holder.keys = (TextView) view.findViewById(R.id.tv_key);
        holder.values = (TextView) view.findViewById(R.id.tv_value);
        view.setTag(holder);
      }
      holder.keys.setText(collaborativeMap.keys().getString(position));
      holder.values.setText((String) collaborativeMap.get(collaborativeMap.keys().getString(position)));
      return view;
    }

    public void removeItem(String key) {
      this.collaborativeMap.remove(key);
      this.notifyDataSetChanged();
    }

  }

  private MapAdapter adapter;
  private ListView listView;
  private EditText itemKey;
  private EditText itemValue;
  private Button bt_removeSelectItem;
  private Button bt_clearTheMap;
  private Button bt_putKeyAndValue;

  private static final String MAP_KEY = "demo_map";

  public static void initializeModel(Model mod) {
    CollaborativeMap map = mod.createMap(null);
    map.set("Key 1", "Value 1");
    map.set("Key 2", "Value 2");
    map.set("Key 3", "Value 3");
    map.set("Key 4", "Value 4");
    mod.getRoot().set(MAP_KEY, map);
  }

  private class MapModel implements RealtimeModel {
    private CollaborativeMap map;

    @Override
    public void connectRealtime() {
      map.onObjectChanged(new Handler<ObjectChangedEvent>() {
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
      bt_putKeyAndValue.setOnClickListener(new OnClickListener() {
        @Override
        public void onClick(View v) {
          String value = itemValue.getText().toString();
          String key = itemKey.getText().toString();
          adapter.addItem(key, value);
        }

      });
      bt_removeSelectItem.setOnClickListener(new OnClickListener() {
        @Override
        public void onClick(View v) {
          String key = itemKey.getText().toString();
          if (map.has(key)) {
            adapter.removeItem(key);
          }
        }
      });
      bt_clearTheMap.setOnClickListener(new OnClickListener() {
        @Override
        public void onClick(View v) {
          adapter.clear();
        }
      });
    }

    @Override
    public void loadField() {
      map = root.get(MAP_KEY);
    }

    @Override
    public void updateUi() {
      adapter.setCollaborativeMap((CollaborativeMap) root.get("demo_map"));
      adapter.notifyDataSetChanged();
    }
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    setContentView(R.layout.activity_collaborativemap);
    super.onCreate(savedInstanceState);

    listView = (ListView) findViewById(R.id.CollaborativeMap);
    itemKey = (EditText) findViewById(R.id.itemKey);
    itemValue = (EditText) findViewById(R.id.itemValue);
    bt_removeSelectItem = (Button) findViewById(R.id.bt_removeSelectMap);
    bt_clearTheMap = (Button) findViewById(R.id.bt_clearTheMap);
    bt_putKeyAndValue = (Button) findViewById(R.id.bt_putKeyAndValue);

    actionBar.setTitle("CollabrativeMap Demo");

    listView.setOnItemClickListener(new OnItemClickListener() {
      @Override
      public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Object obj = listView.getItemAtPosition(position);
        if (obj != null) {
          String[] item = (String[]) obj;
          itemKey.setText(item[0]);
          itemValue.setText(item[1]);
        }
      }
    });
    adapter = new MapAdapter();
    listView.setAdapter(adapter);
    model = new MapModel();
  }

}
