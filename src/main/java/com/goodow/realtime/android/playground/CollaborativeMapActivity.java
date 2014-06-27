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
import com.goodow.realtime.core.Handler;
import com.goodow.realtime.store.CollaborativeMap;
import com.goodow.realtime.store.Document;
import com.goodow.realtime.store.DocumentSaveStateChangedEvent;
import com.goodow.realtime.store.Model;
import com.goodow.realtime.store.ObjectChangedEvent;
import com.goodow.realtime.store.Store;

public class CollaborativeMapActivity extends Activity {

  static class ViewHolder {
    TextView keys;
    TextView values;
  }

  private class MapAdapter extends BaseAdapter {
    private final CollaborativeMap collaborativeMap;

    /**
     * @param collaborativeMap
     */
    public MapAdapter(CollaborativeMap collaborativeMap) {
      super();
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
      return collaborativeMap.size();
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

  private Store store = StoreProvider.get();
  private Document doc;
  private Model mod;
  private CollaborativeMap root;
  private boolean active = false;

  private MapAdapter adapter;

  private ListView listView;
  private EditText itemKey;
  private EditText itemValue;
  private Button bt_removeSelectItem;
  private Button bt_clearTheMap;
  private Button bt_putKeyAndValue;
  private ProgressBar pbIndeterminate;

  private static final String MAP_KEY = "demo_map";

  public static void initializeModel(Model mod) {
    CollaborativeMap map = mod.createMap(null);
    map.set("Key 1", "Value 1");
    map.set("Key 2", "Value 2");
    map.set("Key 3", "Value 3");
    map.set("Key 4", "Value 4");
    mod.getRoot().set(MAP_KEY, map);
  }

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
  private final RealtimeModel MapModel = new RealtimeModel() {

    private CollaborativeMap map;

    @Override
    public void connectRealtime() {
      map.onObjectChanged(new Handler<ObjectChangedEvent>() {
        @Override
        public void handle(ObjectChangedEvent event) {
          updateUi();
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
      adapter.notifyDataSetChanged();
    }
  };

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.main, menu);
    return true;
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
        pbIndeterminate.setVisibility(View.GONE);
        document.onDocumentSaveStateChanged(saveStateHandler);
        doc = document;
        mod = doc.getModel();
        root = mod.getRoot();
        adapter = new MapAdapter((CollaborativeMap) root.get("demo_map"));
        listView.setAdapter(adapter);
        connectMap();
      }
    };
    pbIndeterminate.setVisibility(View.VISIBLE);
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
    setContentView(R.layout.activity_collaborativemap);

    listView = (ListView) findViewById(R.id.CollaborativeMap);
    itemKey = (EditText) findViewById(R.id.itemKey);
    itemValue = (EditText) findViewById(R.id.itemValue);
    bt_removeSelectItem = (Button) findViewById(R.id.bt_removeSelectMap);
    bt_clearTheMap = (Button) findViewById(R.id.bt_clearTheMap);
    bt_putKeyAndValue = (Button) findViewById(R.id.bt_putKeyAndValue);
    pbIndeterminate = (ProgressBar) findViewById(R.id.pb_indeterminateMap);

    ActionBar actionBar = this.getActionBar();
//    actionBar.setDisplayOptions(ActionBar.DISPLAY_HOME_AS_UP, ActionBar.DISPLAY_HOME_AS_UP);
//    actionBar.setTitle("CollabrativeMap Demo");

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
  }

  private void connectMap() {
    MapModel.loadField();
    MapModel.updateUi();
    MapModel.connectUi();
    MapModel.connectRealtime();
  }

}
