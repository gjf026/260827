package com.github.tvbox.osc.ui.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;

import com.github.tvbox.osc.R;
import com.github.tvbox.osc.api.ApiConfig;
import com.github.tvbox.osc.base.BaseActivity;
import com.github.tvbox.osc.bean.IJKCode;
import com.github.tvbox.osc.ui.adapter.SelectDialogAdapter;
import com.github.tvbox.osc.ui.dialog.SelectDialog;
import com.github.tvbox.osc.ui.dialog.XWalkInitDialog;
import com.github.tvbox.osc.util.FastClickCheckUtil;
import com.github.tvbox.osc.util.HawkConfig;
import com.github.tvbox.osc.util.PlayerHelper;
import com.orhanobut.hawk.Hawk;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * 播放设置页面
 */
public class PlaySettingsActivity extends BaseActivity {
    private TextView tvPlay;
    private TextView tvMediaCodec;
    private TextView tvRender;
    private TextView tvParseWebView;
    private TextView tvScale;
    private TextView tvShowPreviewText;
    private TextView tvAutoSwitchLineText;
    private TextView tvIjkCachePlay;

    @Override
    protected int getLayoutResID() {
        return R.layout.activity_play_settings;
    }

    @Override
    protected void init() {
        initView();
        initData();
        initClickListener();
        // 设置焦点到第一个设置项
        findViewById(R.id.llPlay).requestFocus();
    }

    private void initView() {
        tvPlay = findViewById(R.id.tvPlay);
        tvMediaCodec = findViewById(R.id.tvMediaCodec);
        tvRender = findViewById(R.id.tvRenderType);
        tvParseWebView = findViewById(R.id.tvParseWebView);
        tvScale = findViewById(R.id.tvScaleType);
        tvShowPreviewText = findViewById(R.id.showPreviewText);
        tvAutoSwitchLineText = findViewById(R.id.autoSwitchLineText);
        tvIjkCachePlay = findViewById(R.id.tvIjkCachePlay);
    }

    private void initData() {
        tvPlay.setText(PlayerHelper.getPlayerName(Hawk.get(HawkConfig.PLAY_TYPE, 0)));
        tvMediaCodec.setText(Hawk.get(HawkConfig.IJK_CODEC, "硬解码"));
        tvRender.setText(PlayerHelper.getRenderName(Hawk.get(HawkConfig.PLAY_RENDER, 0)));
        tvParseWebView.setText(Hawk.get(HawkConfig.PARSE_WEBVIEW, true) ? "系统自带" : "XWalkView");
        tvScale.setText(PlayerHelper.getScaleName(Hawk.get(HawkConfig.PLAY_SCALE, 0)));
        tvShowPreviewText.setText(Hawk.get(HawkConfig.SHOW_PREVIEW, true) ? "开启" : "关闭");
        tvAutoSwitchLineText.setText(Hawk.get(HawkConfig.AUTO_SWITCH_LINE, true) ? "开启" : "关闭");
        tvIjkCachePlay.setText(Hawk.get(HawkConfig.IJK_CACHE_PLAY, true) ? "开启" : "关闭");
    }

    private void initClickListener() {
        // 播放器设置
        findViewById(R.id.llPlay).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FastClickCheckUtil.check(v);
                int playerType = Hawk.get(HawkConfig.PLAY_TYPE, 0);
                int defaultPos = 0;
                ArrayList<Integer> players = PlayerHelper.getExistPlayerTypes();
                ArrayList<Integer> renders = new ArrayList<>();
                for (int p = 0; p < players.size(); p++) {
                    renders.add(p);
                    if (players.get(p) == playerType) {
                        defaultPos = p;
                    }
                }
                SelectDialog<Integer> dialog = new SelectDialog<Integer>(mContext);
                dialog.setTip("请选择默认播放器");
                dialog.setAdapter(new SelectDialogAdapter.SelectDialogInterface<Integer>() {
                    @Override
                    public void click(Integer value, int pos) {
                        Integer thisPlayerType = players.get(pos);
                        Hawk.put(HawkConfig.PLAY_TYPE, thisPlayerType);
                        tvPlay.setText(PlayerHelper.getPlayerName(thisPlayerType));
                        PlayerHelper.init();
                    }

                    @Override
                    public String getDisplay(Integer val) {
                        Integer playerType = players.get(val);
                        return PlayerHelper.getPlayerName(playerType);
                    }
                }, new DiffUtil.ItemCallback<Integer>() {
                    @Override
                    public boolean areItemsTheSame(@NonNull @NotNull Integer oldItem, @NonNull @NotNull Integer newItem) {
                        return oldItem.intValue() == newItem.intValue();
                    }

                    @Override
                    public boolean areContentsTheSame(@NonNull @NotNull Integer oldItem, @NonNull @NotNull Integer newItem) {
                        return oldItem.intValue() == newItem.intValue();
                    }
                }, renders, defaultPos);
                dialog.show();
            }
        });

        // IJK解码方式设置
        findViewById(R.id.llMediaCodec).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<IJKCode> ijkCodes = ApiConfig.get().getIjkCodes();
                if (ijkCodes == null || ijkCodes.size() == 0)
                    return;
                FastClickCheckUtil.check(v);

                int defaultPos = 0;
                String ijkSel = Hawk.get(HawkConfig.IJK_CODEC, "硬解码");
                for (int j = 0; j < ijkCodes.size(); j++) {
                    if (ijkSel.equals(ijkCodes.get(j).getName())) {
                        defaultPos = j;
                        break;
                    }
                }

                SelectDialog<IJKCode> dialog = new SelectDialog<IJKCode>(mContext);
                dialog.setTip("请选择IJK解码");
                dialog.setAdapter(new SelectDialogAdapter.SelectDialogInterface<IJKCode>() {
                    @Override
                    public void click(IJKCode value, int pos) {
                        value.selected(true);
                        tvMediaCodec.setText(value.getName());
                    }

                    @Override
                    public String getDisplay(IJKCode val) {
                        return val.getName();
                    }
                }, new DiffUtil.ItemCallback<IJKCode>() {
                    @Override
                    public boolean areItemsTheSame(@NonNull @NotNull IJKCode oldItem, @NonNull @NotNull IJKCode newItem) {
                        return oldItem == newItem;
                    }

                    @Override
                    public boolean areContentsTheSame(@NonNull @NotNull IJKCode oldItem, @NonNull @NotNull IJKCode newItem) {
                        return oldItem.getName().equals(newItem.getName());
                    }
                }, ijkCodes, defaultPos);
                dialog.show();
            }
        });

        // 渲染方式设置
        findViewById(R.id.llRender).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FastClickCheckUtil.check(v);
                int defaultPos = Hawk.get(HawkConfig.PLAY_RENDER, 0);
                ArrayList<Integer> renders = new ArrayList<>();
                renders.add(0);
                renders.add(1);
                SelectDialog<Integer> dialog = new SelectDialog<Integer>(mContext);
                dialog.setTip("请选择默认渲染方式");
                dialog.setAdapter(new SelectDialogAdapter.SelectDialogInterface<Integer>() {
                    @Override
                    public void click(Integer value, int pos) {
                        Hawk.put(HawkConfig.PLAY_RENDER, value);
                        tvRender.setText(PlayerHelper.getRenderName(value));
                        PlayerHelper.init();
                    }

                    @Override
                    public String getDisplay(Integer val) {
                        return PlayerHelper.getRenderName(val);
                    }
                }, new DiffUtil.ItemCallback<Integer>() {
                    @Override
                    public boolean areItemsTheSame(@NonNull @NotNull Integer oldItem, @NonNull @NotNull Integer newItem) {
                        return oldItem.intValue() == newItem.intValue();
                    }

                    @Override
                    public boolean areContentsTheSame(@NonNull @NotNull Integer oldItem, @NonNull @NotNull Integer newItem) {
                        return oldItem.intValue() == newItem.intValue();
                    }
                }, renders, defaultPos);
                dialog.show();
            }
        });

        // 嗅探Webview设置
        findViewById(R.id.llParseWebVew).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FastClickCheckUtil.check(v);
                boolean useSystem = !Hawk.get(HawkConfig.PARSE_WEBVIEW, true);
                Hawk.put(HawkConfig.PARSE_WEBVIEW, useSystem);
                tvParseWebView.setText(Hawk.get(HawkConfig.PARSE_WEBVIEW, true) ? "系统自带" : "XWalkView");
                if (!useSystem) {
                    Toast.makeText(mContext, "注意: XWalkView只适用于部分低Android版本，Android5.0以上推荐使用系统自带", Toast.LENGTH_LONG).show();
                    XWalkInitDialog dialog = new XWalkInitDialog(mContext);
                    dialog.setOnListener(new XWalkInitDialog.OnListener() {
                        @Override
                        public void onchange() {
                        }
                    });
                    dialog.show();
                }
            }
        });

        // 画面缩放设置
        findViewById(R.id.llScale).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FastClickCheckUtil.check(v);
                int defaultPos = Hawk.get(HawkConfig.PLAY_SCALE, 0);
                ArrayList<Integer> players = new ArrayList<>();
                players.add(0);
                players.add(1);
                players.add(2);
                players.add(3);
                players.add(4);
                players.add(5);
                SelectDialog<Integer> dialog = new SelectDialog<Integer>(mContext);
                dialog.setTip("请选择默认画面缩放");
                dialog.setAdapter(new SelectDialogAdapter.SelectDialogInterface<Integer>() {
                    @Override
                    public void click(Integer value, int pos) {
                        Hawk.put(HawkConfig.PLAY_SCALE, value);
                        tvScale.setText(PlayerHelper.getScaleName(value));
                    }

                    @Override
                    public String getDisplay(Integer val) {
                        return PlayerHelper.getScaleName(val);
                    }
                }, new DiffUtil.ItemCallback<Integer>() {
                    @Override
                    public boolean areItemsTheSame(@NonNull @NotNull Integer oldItem, @NonNull @NotNull Integer newItem) {
                        return oldItem.intValue() == newItem.intValue();
                    }

                    @Override
                    public boolean areContentsTheSame(@NonNull @NotNull Integer oldItem, @NonNull @NotNull Integer newItem) {
                        return oldItem.intValue() == newItem.intValue();
                    }
                }, players, defaultPos);
                dialog.show();
            }
        });

        // 窗口预览设置
        findViewById(R.id.showPreview).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FastClickCheckUtil.check(v);
                Hawk.put(HawkConfig.SHOW_PREVIEW, !Hawk.get(HawkConfig.SHOW_PREVIEW, true));
                tvShowPreviewText.setText(Hawk.get(HawkConfig.SHOW_PREVIEW, true) ? "开启" : "关闭");
            }
        });

        // 自动换线设置
        findViewById(R.id.autoSwitchLine).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FastClickCheckUtil.check(v);
                boolean enable = !Hawk.get(HawkConfig.AUTO_SWITCH_LINE, true);
                Hawk.put(HawkConfig.AUTO_SWITCH_LINE, enable);
                tvAutoSwitchLineText.setText(enable ? "开启" : "关闭");
            }
        });

        // IJK缓存设置
        findViewById(R.id.llIjkCachePlay).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FastClickCheckUtil.check(v);
                Hawk.put(HawkConfig.IJK_CACHE_PLAY, !Hawk.get(HawkConfig.IJK_CACHE_PLAY, true));
                tvIjkCachePlay.setText(Hawk.get(HawkConfig.IJK_CACHE_PLAY, true) ? "开启" : "关闭");
            }
        });
    }
}