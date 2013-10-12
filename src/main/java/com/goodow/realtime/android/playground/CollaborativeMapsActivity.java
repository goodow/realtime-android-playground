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
import com.goodow.realtime.Document;
import com.goodow.realtime.DocumentLoadedHandler;
import com.goodow.realtime.DocumentSaveStateChangedEvent;
import com.goodow.realtime.EventHandler;
import com.goodow.realtime.Model;
import com.goodow.realtime.ModelInitializerHandler;
import com.goodow.realtime.ObjectChangedEvent;
import com.goodow.realtime.Realtime;

import java.util.Arrays;

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

public class CollaborativeMapsActivity extends RoboActivity {

  static class ViewHolder {
    TextView keys;
    TextView values;
  }
  private class MapAdapter extends BaseAdapter {
    private CollaborativeMap collaborativeMap;

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
      int count = (collaborativeMap == null ? 0 : collaborativeMap.size());
      return count;
    }

    @Override
    public Object getItem(int position) {
      String[] string = new String[2];
      string[0] = collaborativeMap.keys()[position];
      string[1] = collaborativeMap.get(collaborativeMap.keys()[position]);
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
      holder.keys.setText(collaborativeMap.keys()[position]);
      holder.values.setText((String) collaborativeMap.get(collaborativeMap.keys()[position]));
      return view;
    }

    public void removeItem(String key) {
      this.collaborativeMap.remove(key);
      this.notifyDataSetChanged();
    }

    public void setCollaborativeMap(CollaborativeMap collaborativeMap) {
      this.collaborativeMap = collaborativeMap;
    }
  }

  private Document doc;
  private Model mod;
  private CollaborativeMap root;

  private MapAdapter adapter;

  @InjectView(R.id.CollaborativeMap)
  ListView listView;
  @InjectView(R.id.itemKey)
  EditText itemKey;
  @InjectView(R.id.itemValue)
  EditText itemValue;
  @InjectView(R.id.bt_removeSelectMap)
  Button bt_removeSelectItem;
  @InjectView(R.id.bt_clearTheMap)
  Button bt_clearTheMap;
  @InjectView(R.id.bt_putKeyAndValue)
  Button bt_putKeyAndValue;
  @InjectView(R.id.pb_indeterminateMap)
  private ProgressBar pbIndeterminate;
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
  private final RealtimeModel MapModel = new RealtimeModel() {
    private static final String Map_KEY = "demo_map";
    private CollaborativeMap map;

    @Override
    public void connectRealtime() {
      map.addObjectChangedListener(new EventHandler<ObjectChangedEvent>() {
        @Override
        public void handleEvent(ObjectChangedEvent event) {
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
          // key is not blank
          if (key != null) {
            adapter.addItem(key, value);
            map.set(key, value);
          }
        }

      });
      bt_removeSelectItem.setOnClickListener(new OnClickListener() {
        @Override
        public void onClick(View v) {
          String value = itemValue.getText().toString().trim();
          String key = itemKey.getText().toString().trim();
          if (key != null) {
            if (Arrays.asList(map.keys()).contains(key)) {
              if (value != null && value.equals(map.get(key))
                  || (value == null && map.get(key) == null)) {
                adapter.removeItem(key);
                map.remove(key);
              }

            }

          }
        }
      });
      bt_clearTheMap.setOnClickListener(new OnClickListener() {
        @Override
        public void onClick(View v) {
          map.clear();
          adapter.clear();
        }

      });
    }

    @Override
    public void initializeModel() {
      CollaborativeMap map = mod.createMap(null);
      map.set("1", "a");
      map.set("2", "b");
      map.set("3", "c");
      root.set(Map_KEY, map);
    }

    @Override
    public void loadField() {
      map = root.get(Map_KEY);
    }

    @Override
    public void updateUi() {
      adapter.setCollaborativeMap(map);
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
    setContentView(R.layout.activity_collaborativemaps);
    ActionBar actionBar = this.getActionBar();
    actionBar.setDisplayOptions(ActionBar.DISPLAY_HOME_AS_UP, ActionBar.DISPLAY_HOME_AS_UP);
    actionBar.setTitle("CollabrativeMaps Demo");
    Realtime.authorize("688185492143008835447", "68c8f4141821bdcc7a43f4233a2b732d3ed956b5");
    DocumentLoadedHandler onLoaded = new DocumentLoadedHandler() {
      @Override
      public void onLoaded(Document document) {
        pbIndeterminate.setVisibility(View.GONE);
        document.addDocumentSaveStateListener(saveStateHandler);
        doc = document;
        mod = doc.getModel();
        root = mod.getRoot();
        connectMap();
      }
    };
    ModelInitializerHandler opt_initializer = new ModelInitializerHandler() {
      @Override
      public void onInitializer(Model model) {
        mod = model;
        root = mod.getRoot();
        MapModel.initializeModel();
      }
    };
    pbIndeterminate.setVisibility(View.VISIBLE);
    Realtime.load("@tmp/demo", onLoaded, opt_initializer, null);
    adapter = new MapAdapter(null);
    listView.setAdapter(adapter);
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
