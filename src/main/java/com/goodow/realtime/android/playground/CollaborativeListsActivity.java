package com.goodow.realtime.android.playground;

import com.goodow.realtime.CollaborativeList;
import com.goodow.realtime.CollaborativeMap;
import com.goodow.realtime.Document;
import com.goodow.realtime.DocumentLoadedHandler;
import com.goodow.realtime.DocumentSaveStateChangedEvent;
import com.goodow.realtime.EventHandler;
import com.goodow.realtime.Model;
import com.goodow.realtime.ModelInitializerHandler;
import com.goodow.realtime.ObjectChangedEvent;
import com.goodow.realtime.Realtime;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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

    @Override
    public int getCount() {
      return arrayList.size();
    }

    @Override
    public Object getItem(int position) {
      return arrayList.get(position);
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
      textView.setText(arrayList.get(position));
      return view;
    }
  }

  private final EventHandler<DocumentSaveStateChangedEvent> saveStateHandler =
      new EventHandler<DocumentSaveStateChangedEvent>() {
        @Override
        public void handleEvent(DocumentSaveStateChangedEvent event) {
          if (event.isSaving || event.isPending) {
            // 正在联网中,显示progressbar
            pbIndeterminate.setVisibility(View.VISIBLE);
          } else {
            // 联网完成,隐藏progressbar
            pbIndeterminate.setVisibility(View.GONE);
          }
        }
      };
  @InjectView(R.id.pb_indeterminateList)
  private ProgressBar pbIndeterminate;

  private Document doc;
  private Model mod;
  private CollaborativeMap root;
  private int selectPosition;

  private List<String> arrayList;
  @InjectView(R.id.CollaborativeList)
  ListView listView;
  @InjectView(R.id.selectItem)
  EditText selectItem;
  @InjectView(R.id.AddAnItem)
  EditText AddAnItem;

  @InjectView(R.id.bt_addAnItem)
  Button bt_addAnItem;
  @InjectView(R.id.bt_removeSelectItem)
  Button bt_removeSelectItem;
  @InjectView(R.id.bt_clearTheList)
  Button bt_clearTheList;
  @InjectView(R.id.bt_setSelectItem)
  Button bt_setSelectItem;

  private final RealtimeModel ListModel = new RealtimeModel() {
    private static final String List_KEY = "demo_list";
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

    /**
     * 此处添加item,删除item,并且更新UI
     */
    @Override
    public void connectUi() {
      // 添加一个item
      bt_addAnItem.setOnClickListener(new OnClickListener() {

        @Override
        public void onClick(View v) {
          String addAnItem = AddAnItem.getText().toString().trim();
          if (AddAnItem != null) {
            // CollaborativeList里面添加一个元素,添加到结尾
            list.insert(list.length(), addAnItem);
            // 在ArrayList结尾增加一个元素
            arrayList.add(addAnItem);
            // 更新适配器
            adapter.notifyDataSetChanged();
          }
        }
      });
      // 清除一个item,此处有位置
      bt_removeSelectItem.setOnClickListener(new OnClickListener() {

        @Override
        public void onClick(View v) {
          String item = selectItem.getText().toString().trim();
          // item不为空白
          if (arrayList.size() != 0 && item.equals(arrayList.get(selectPosition))) {
            // CollaborativeList里面移除一个元素
            list.remove(selectPosition);
            // 在ArrayList里面移除一个元素
            arrayList.remove(selectPosition);
            // 更新适配器
            adapter.notifyDataSetChanged();
          }

        }
      });
      // 清除所有的item
      bt_clearTheList.setOnClickListener(new OnClickListener() {

        @Override
        public void onClick(View v) {
          if (arrayList.size() != 0) {
            // 移除CollaborativeList里面的元素
            list.clear();
            // 在ArrayList里面的元素
            arrayList.clear();
            // 更新适配器
            adapter.notifyDataSetChanged();
          }
        }
      });
      bt_setSelectItem.setOnClickListener(new OnClickListener() {

        @Override
        public void onClick(View v) {
          arrayList.set(selectPosition, selectItem.getText().toString());
          adapter.notifyDataSetChanged();
        }
      });
    }

    @Override
    public void initializeModel() {
      CollaborativeList list = mod.createList();
      list.push("Hello");
      list.push("World");
      root.set(List_KEY, list);
    }

    @Override
    public void loadField() {
      list = root.get(List_KEY);
    }

    // 将服务器传过来的数据，展示到界面,更新adapter
    @Override
    public void updateUi() {
      String[] listString = new String[list.asArray().length];
      for (int i = 0; i < list.asArray().length; i++) {
        listString[i] = list.asArray()[i].toString();
      }
      arrayList = new ArrayList<String>(Arrays.asList(listString));
      // TODO更新适配器
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

    Realtime.authorize("688185492143008835447", "68c8f4141821bdcc7a43f4233a2b732d3ed956b5");
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
    ModelInitializerHandler opt_initializer = new ModelInitializerHandler() {
      @Override
      public void onInitializer(Model model) {
        mod = model;
        root = mod.getRoot();
        ListModel.initializeModel();
      }
    };
    pbIndeterminate.setVisibility(View.VISIBLE);
    Realtime.load("@tmp/demo", onLoaded, opt_initializer, null);
    arrayList = new ArrayList<String>();
    adapter = new ListAdapter();
    listView.setAdapter(adapter);
    listView.setOnItemClickListener(new OnItemClickListener() {
      @Override
      public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        // Set select item's value
        selectItem.setText(arrayList.get(position));
        // save postion
        selectPosition = position;
      }
    });

  }

  private void connectList() {
    ListModel.loadField();
    ListModel.updateUi();
    ListModel.connectUi();
    ListModel.connectRealtime();
  }

}
