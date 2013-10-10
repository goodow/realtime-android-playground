package com.goodow.realtime.android.playground;

import com.goodow.realtime.CollaborativeList;
import com.goodow.realtime.CollaborativeMap;
import com.goodow.realtime.Document;
import com.goodow.realtime.DocumentLoadedHandler;
import com.goodow.realtime.EventHandler;
import com.goodow.realtime.Model;
import com.goodow.realtime.ModelInitializerHandler;
import com.goodow.realtime.ObjectChangedEvent;
import com.goodow.realtime.Realtime;

import android.app.ActionBar;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import roboguice.activity.RoboActivity;
import roboguice.inject.InjectView;

public class CollaborativeListActivity extends RoboActivity {
  private Document doc;
  private Model mod;
  private CollaborativeMap root;
  private String[] listString;

  @InjectView(R.id.CollaborativeList)
  ListView listView;
  @InjectView(R.id.selectItem)
  EditText selectItem;
  @InjectView(R.id.AddAnItem)
  EditText AddAnItem;
  @InjectView(R.id.bt_addAnItem)
  Button bt_addAnItem;

  private final RealtimeModel ListModel = new RealtimeModel() {
    private static final String List_KEY = "demo_list";
    private CollaborativeList list;

    @Override
    public void connectRealtime() {
      if (list != null) {
        list.addObjectChangedListener(new EventHandler<ObjectChangedEvent>() {
          @Override
          public void handleEvent(ObjectChangedEvent event) {
            updateUi();
          }
        });
      }

    }

    @Override
    public void connectUi() {
      // stringText.addTextChangedListener(new TextWatcher() {
      // @Override
      // public void afterTextChanged(Editable arg0) {
      // }
      //
      // @Override
      // public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
      // }
      //
      // @Override
      // public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
      // str.setText(stringText.getText().toString());
      // }
      // });
      bt_addAnItem.setOnClickListener(new OnClickListener() {

        @Override
        public void onClick(View v) {
          Toast.makeText(CollaborativeListActivity.this, "test", 1).show();
        }
      });
    }

    @Override
    public void initializeModel() {
      // ArrayList<String> arrayList = new ArrayList<String>(Arrays.asList("dog", "cat"));
      CollaborativeList list = mod.createList();
      list.push("Hello");
      list.push("World");
      root.set(List_KEY, list);
    }

    @Override
    public void loadField() {
      list = root.get(List_KEY);
    }

    @Override
    public void updateUi() {
      if (list != null) {
        listString = new String[list.asArray().length];
        for (int i = 0; i < list.asArray().length; i++) {
          listString[i] = list.asArray()[i].toString();
        }
        adapter.notifyDataSetChanged();
      }
    }
  };
  private ArrayAdapter<String> adapter;

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

    Realtime.load("@tmp/demo", onLoaded, opt_initializer, null);
    listString = new String[] {"a", "b", "c"};
    adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, listString);
    listView.setAdapter(adapter);

  }

  private void connectList() {
    ListModel.loadField();
    ListModel.updateUi();
    ListModel.connectUi();
    ListModel.connectRealtime();
  }

}
