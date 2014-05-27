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

import com.goodow.realtime.android.AndroidPlatform;
import com.goodow.realtime.java.JavaWebSocket;
import com.goodow.realtime.store.Store;
import com.goodow.realtime.store.impl.DefaultStore;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Maintains a singleton instance for obtaining the bus. Ideally this would be replaced with a more
 * efficient means such as through injection directly into interested classes.
 */
public final class StoreProvider {
  private static final String SERVER = "realtime.goodow.com:1986";
  static {
    AndroidPlatform.register();
    // adb shell setprop log.tag.JavaWebSocket DEBUG
    Logger.getLogger(JavaWebSocket.class.getName()).setLevel(Level.ALL);
  }
  private static final Store INSTANCE = new DefaultStore("ws://" + SERVER + "/channel/websocket",
      null);

  public static Store get() {
    return INSTANCE;
  }

  private StoreProvider() {
    // No instances.
  }
}
