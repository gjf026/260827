package com.github.tvbox.osc.ui.dialog;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;

public class PasswordEditText extends androidx.appcompat.widget.AppCompatEditText {
    public interface OnBackKeyListener {
        boolean onBackKey();
    }

    private OnBackKeyListener onBackKeyListener;

    public PasswordEditText(Context context) {
        super(context);
    }

    public PasswordEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PasswordEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setOnBackKeyListener(OnBackKeyListener listener) {
        onBackKeyListener = listener;
    }

    @Override
    public boolean onKeyPreIme(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_ESCAPE || keyCode == KeyEvent.KEYCODE_BACK)
                && event.getAction() == KeyEvent.ACTION_DOWN
                && onBackKeyListener != null && onBackKeyListener.onBackKey()) {
            return true;
        }
        return super.onKeyPreIme(keyCode, event);
    }
}
