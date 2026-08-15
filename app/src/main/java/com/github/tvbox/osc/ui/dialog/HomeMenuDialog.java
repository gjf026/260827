package com.github.tvbox.osc.ui.dialog;

import android.app.Activity;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;

import com.github.tvbox.osc.R;
import com.github.tvbox.osc.util.HawkConfig;
import com.orhanobut.hawk.Hawk;

import java.util.ArrayList;
import java.util.Arrays;

public class HomeMenuDialog extends BaseDialog {
    private static final ArrayList<String> MENUS = new ArrayList<>(Arrays.asList(
            "历史", "直播", "搜索", "推送", "收藏"));

    public HomeMenuDialog(@NonNull Context context) {
        super(context);
        if (context instanceof Activity) setOwnerActivity((Activity) context);
        setContentView(R.layout.dialog_home_menu);
        LinearLayout list = findViewById(R.id.homeMenuList);
        ArrayList<String> checked = Hawk.get(HawkConfig.HOME_MENU, new ArrayList<>(MENUS));
        for (String menu : MENUS) {
            CheckBox checkBox = new CheckBox(context);
            checkBox.setText(menu);
            checkBox.setTextColor(context.getResources().getColor(R.color.dialog_text_primary));
            checkBox.setTextSize(22);
            checkBox.setBackgroundResource(R.drawable.button_danmu_setting);
            checkBox.setPadding(20, 8, 20, 8);
            checkBox.setButtonTintList(new ColorStateList(
                    new int[][]{new int[]{android.R.attr.state_checked}, new int[]{}},
                    new int[]{0xff02f8e1, Color.WHITE}));
            checkBox.setFocusable(true);
            checkBox.setChecked(checked.contains(menu));
            checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked && !checked.contains(menu)) checked.add(menu);
                if (!isChecked) checked.remove(menu);
                Hawk.put(HawkConfig.HOME_MENU, checked);
            });
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.bottomMargin = context.getResources().getDimensionPixelSize(R.dimen.vs_10);
            list.addView(checkBox, params);
        }
    }
}
