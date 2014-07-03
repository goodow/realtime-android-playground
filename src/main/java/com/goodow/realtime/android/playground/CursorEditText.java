package com.goodow.realtime.android.playground;

import android.content.Context;
import android.text.Editable;
import android.util.AttributeSet;
import android.widget.EditText;
import android.widget.TextView;

/**
 * Created by liudenghui on 14-7-3.
 */
public class CursorEditText extends EditText {
  public CursorEditText(Context context) {
    super(context);
  }

  public CursorEditText(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public CursorEditText(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
  }

  private OnCursorChangedListener listener;
  private boolean isSetText;

  public void setOnCusorChangedListener(OnCursorChangedListener listener) {
    this.listener = listener;

  }

  @Override
  protected void onSelectionChanged(int selStart, int selEnd) {
    super.onSelectionChanged(selStart, selEnd);
    if (listener != null) {
      if(!isSetText) {
        listener.onCursorChanged(selStart, selEnd);
      } else {
        isSetText = false;
      }
    }
  }

  @Override
  public void setText(CharSequence text, BufferType type) {
    isSetText = true;
    super.setText(text, type);
  }
}
