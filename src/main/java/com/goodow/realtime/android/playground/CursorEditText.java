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

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.EditText;

public class CursorEditText extends EditText {

  public interface OnCursorChangedListener {
    void onCursorChanged(int startIndex, int endIndex);
  }

  public CursorEditText(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  private OnCursorChangedListener listener;

  public void setOnCusorChangedListener(OnCursorChangedListener listener) {
    this.listener = listener;

  }

  @Override
  protected void onSelectionChanged(int selStart, int selEnd) {
    super.onSelectionChanged(selStart, selEnd);
    if (listener != null && selStart!=selEnd) {
      listener.onCursorChanged(selStart, selEnd);
    }
  }

  @Override
  public boolean onTouchEvent(MotionEvent event) {
    boolean bool = super.onTouchEvent(event);
    if (listener != null && event.getAction() == MotionEvent.ACTION_UP) {
      listener.onCursorChanged(getSelectionStart(), getSelectionEnd());
    }
    return bool;
  }
}
