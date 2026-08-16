package com.github.tvbox.osc.ui.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.github.tvbox.osc.R;
import com.github.tvbox.osc.api.ApiConfig;
import com.github.tvbox.osc.api.DanmakuApi;
import com.github.tvbox.osc.base.BaseActivity;
import com.github.tvbox.osc.event.RefreshEvent;
import com.github.tvbox.osc.ui.dialog.DanmuApiDialog;
import com.github.tvbox.osc.util.DanmuHelper;
import com.github.tvbox.osc.util.FastClickCheckUtil;
import com.github.tvbox.osc.util.HawkConfig;
import com.orhanobut.hawk.Hawk;

import org.greenrobot.eventbus.EventBus;

/**
 * 弹幕设置页面
 */
public class DanmuSettingsActivity extends BaseActivity {
    private TextView tvDanmuOpenText;
    private TextView tvDanmuApiText;

    @Override
    protected int getLayoutResID() {
        return R.layout.activity_danmu_settings;
    }

    @Override
    protected void init() {
        initView();
        initData();
        initClickListener();
        // 设置焦点到第一个设置项
        findViewById(R.id.danmuOpen).requestFocus();
    }

    private void initView() {
        tvDanmuOpenText = findViewById(R.id.danmuOpenText);
        tvDanmuApiText = findViewById(R.id.danmuApiText);
    }

    private void initData() {
        tvDanmuOpenText.setText(DanmuHelper.isOpen() ? "开启" : "关闭");
        refreshDanmuApiText();
    }

    private void initClickListener() {
        // 弹幕开关设置
        findViewById(R.id.danmuOpen).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FastClickCheckUtil.check(v);
                boolean open = !DanmuHelper.isOpen();
                DanmuHelper.setOpen(open);
                tvDanmuOpenText.setText(open ? "开启" : "关闭");
                EventBus.getDefault().post(new RefreshEvent(RefreshEvent.TYPE_SET_DANMU_SETTINGS, open));
            }
        });

        // 弹幕地址设置
        findViewById(R.id.danmuApi).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FastClickCheckUtil.check(v);
                DanmuApiDialog dialog = new DanmuApiDialog(mContext);
                dialog.setOnListener(new DanmuApiDialog.OnListener() {
                    @Override
                    public void onChange(String api) {
                        refreshDanmuApiText();
                    }
                });
                dialog.show();
            }
        });
    }

    private void refreshDanmuApiText() {
        if (tvDanmuApiText == null) return;
        if (DanmakuApi.isUseDefault()) {
            tvDanmuApiText.setText("默认");
            return;
        }
        String custom = Hawk.get(HawkConfig.DANMU_API, "");
        if (!custom.isEmpty()) {
            tvDanmuApiText.setText("自定义");
            return;
        }
        String config = ApiConfig.get().getDanmaku();
        tvDanmuApiText.setText(config.isEmpty() ? "默认" : "接口");
    }
}
