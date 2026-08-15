package com.github.tvbox.osc.ui.dialog;

import android.content.Context;
import android.os.Handler;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.text.method.DigitsKeyListener;

import androidx.annotation.NonNull;

import com.github.tvbox.osc.R;
import com.github.tvbox.osc.util.HawkConfig;
import com.orhanobut.hawk.Hawk;

public class HomePasswordDialog extends BaseDialog {
    public interface OnVerifiedListener {
        void onVerified();
    }

    private final boolean verifyOnly;
    private final OnVerifiedListener onVerified;

    public HomePasswordDialog(@NonNull Context context) {
        this(context, false, null);
    }

    public HomePasswordDialog(@NonNull Context context, boolean verifyOnly, OnVerifiedListener onVerified) {
        super(context);
        this.verifyOnly = verifyOnly;
        this.onVerified = onVerified;
        setContentView(R.layout.dialog_home_password);
        EditText input = findViewById(R.id.passwordInput);
        String oldPassword = Hawk.get(HawkConfig.HOME_PASSWORD, "");
        TextView title = findViewById(R.id.passwordTitle);
        title.setText(verifyOnly ? "安全验证" : (oldPassword.isEmpty() ? "设置新密码" : "修改密码"));
        findViewById(R.id.passwordConfirm).setOnClickListener(v -> confirm(input));
        findViewById(R.id.passwordCancel).setOnClickListener(v -> dismiss());
        input.setKeyListener(DigitsKeyListener.getInstance("0123456789"));
        input.setOnKeyListener((v, keyCode, event) -> {
            if (event.getAction() == android.view.KeyEvent.ACTION_DOWN
                    && keyCode == android.view.KeyEvent.KEYCODE_DPAD_DOWN) {
                findViewById(R.id.passwordConfirm).requestFocus();
                return true;
            }
            return false;
        });
        input.setOnEditorActionListener((v, actionId, event) -> true);
        input.requestFocus();
    }

    private void confirm(EditText input) {
        String password = input.getText().toString().trim();
        if (verifyOnly) {
            if (password.equals(Hawk.get(HawkConfig.HOME_PASSWORD, ""))) {
                if (onVerified != null) onVerified.onVerified();
                dismiss();
            } else {
                input.setText("");
                Toast.makeText(getContext(), "密码错误", Toast.LENGTH_SHORT).show();
            }
            return;
        }
        if (password.isEmpty()) return;
        Hawk.put(HawkConfig.HOME_PASSWORD, password);
        Toast.makeText(getContext(), "密码设置成功", Toast.LENGTH_SHORT).show();
        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) imm.hideSoftInputFromWindow(input.getWindowToken(), 0);
        dismiss();
    }

    @Override
    public void show() {
        super.show();
        EditText input = findViewById(R.id.passwordInput);
        new Handler().postDelayed(() -> {
            input.requestFocus();
            InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) imm.showSoftInput(input, InputMethodManager.SHOW_IMPLICIT);
        }, 200);
    }
}
