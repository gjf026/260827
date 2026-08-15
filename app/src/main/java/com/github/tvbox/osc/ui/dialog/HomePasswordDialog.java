package com.github.tvbox.osc.ui.dialog;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.text.InputFilter;
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
        getWindow().setSoftInputMode(android.view.WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        setCanceledOnTouchOutside(true);
        setCancelable(true);
        setOnKeyListener((dialog, keyCode, event) -> {
            if ((keyCode == android.view.KeyEvent.KEYCODE_ESCAPE
                    || keyCode == android.view.KeyEvent.KEYCODE_BACK)
                    && event.getAction() == android.view.KeyEvent.ACTION_DOWN) {
                dismiss();
                return true;
            }
            return false;
        });
        EditText input = findViewById(R.id.passwordInput);
        ((PasswordEditText) input).setOnBackKeyListener(() -> {
            dismiss();
            return true;
        });
        String oldPassword = Hawk.get(HawkConfig.HOME_PASSWORD, "");
        TextView title = findViewById(R.id.passwordTitle);
        title.setText(verifyOnly ? "安全验证" : (oldPassword.isEmpty() ? "设置新密码" : "修改密码"));
        findViewById(R.id.passwordConfirm).setOnClickListener(v -> confirm(input));
        findViewById(R.id.passwordCancel).setOnClickListener(v -> dismiss());
        input.setKeyListener(DigitsKeyListener.getInstance("0123456789"));
        input.setFilters(new InputFilter[]{new InputFilter.LengthFilter(8)});
        input.setOnKeyListener((v, keyCode, event) -> {
            if (event.getAction() == android.view.KeyEvent.ACTION_DOWN
                    && keyCode == android.view.KeyEvent.KEYCODE_DPAD_DOWN) {
                findViewById(R.id.passwordConfirm).requestFocus();
                return true;
            }
            return false;
        });
        input.setOnEditorActionListener((v, actionId, event) -> true);
        setupKeypad(input);
        input.requestFocus();
    }

    private void setupKeypad(EditText input) {
        LinearLayout keypad = findViewById(R.id.passwordKeypad);
        String[][] keys = {{"1", "2", "3", "删除"}, {"4", "5", "6", "重置"}, {"7", "8", "9", "0"}};
        TextView[][] buttons = new TextView[keys.length][keys[0].length];
        for (int rowIndex = 0; rowIndex < keys.length; rowIndex++) {
            LinearLayout row = new LinearLayout(getContext());
            row.setOrientation(LinearLayout.HORIZONTAL);
            for (int columnIndex = 0; columnIndex < keys[rowIndex].length; columnIndex++) {
                String key = keys[rowIndex][columnIndex];
                TextView button = new TextView(getContext());
                button.setId(View.generateViewId());
                button.setText(key);
                button.setTextColor(getContext().getResources().getColor(R.color.dialog_text_primary));
                button.setTextSize(22);
                button.setGravity(android.view.Gravity.CENTER);
                button.setBackgroundResource(R.drawable.button_danmu_setting);
                button.setFocusable(true);
                button.setClickable(true);
                button.setOnClickListener(v -> {
                    if ("删除".equals(key)) {
                        if (input.length() > 0) input.getText().delete(input.length() - 1, input.length());
                    } else if ("重置".equals(key)) {
                        input.setText("");
                    } else if (input.length() < 8) {
                        input.append(key);
                    }
                });
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        0, getContext().getResources().getDimensionPixelSize(R.dimen.vs_60), 1.0f);
                int margin = getContext().getResources().getDimensionPixelSize(R.dimen.vs_5);
                params.setMargins(margin, margin, margin, margin);
                row.addView(button, params);
                buttons[rowIndex][columnIndex] = button;
            }
            keypad.addView(row, new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        }
        TextView confirm = findViewById(R.id.passwordConfirm);
        TextView cancel = findViewById(R.id.passwordCancel);
        confirm.setNextFocusDownId(buttons[0][0].getId());
        cancel.setNextFocusDownId(buttons[0][3].getId());
        keypad.post(() -> {
            int buttonWidth = buttons[0][0].getWidth();
            ViewGroup.LayoutParams confirmParams = confirm.getLayoutParams();
            confirmParams.width = buttonWidth;
            confirm.setLayoutParams(confirmParams);
            ViewGroup.LayoutParams cancelParams = cancel.getLayoutParams();
            cancelParams.width = buttonWidth;
            cancel.setLayoutParams(cancelParams);
        });
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
        if (password.length() < 4) {
            Toast.makeText(getContext(), "请输入4~8位数字", Toast.LENGTH_SHORT).show();
            return;
        }
        Hawk.put(HawkConfig.HOME_PASSWORD, password);
        Toast.makeText(getContext(), "密码设置成功", Toast.LENGTH_SHORT).show();
        dismiss();
    }
}
