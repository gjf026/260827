package com.github.tvbox.osc.ui.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;

import com.github.tvbox.osc.R;
import com.github.tvbox.osc.api.ApiConfig;
import com.github.tvbox.osc.base.BaseActivity;
import com.github.tvbox.osc.ui.adapter.SelectDialogAdapter;
import com.github.tvbox.osc.ui.dialog.HomeMenuDialog;
import com.github.tvbox.osc.ui.dialog.HomePasswordDialog;
import com.github.tvbox.osc.ui.dialog.SelectDialog;
import com.github.tvbox.osc.util.FastClickCheckUtil;
import com.github.tvbox.osc.util.HawkConfig;
import com.github.tvbox.osc.util.HistoryHelper;
import com.github.tvbox.osc.util.OkGoHelper;
import com.github.tvbox.osc.util.PlayerHelper;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.FileCallback;
import com.lzy.okgo.model.Progress;
import com.lzy.okgo.model.Response;
import com.orhanobut.hawk.Hawk;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;

import okhttp3.HttpUrl;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;

/**
 * 个性设置页面
 */
public class PersonalSettingsActivity extends BaseActivity {
    private TextView tvHomeDefaultShow;
    private TextView tvHomeRec;
    private TextView tvRecStyleText;
    private TextView tvSearchView;
    private TextView tvFastSearchText;
    private TextView tvHistoryMerge;
    private TextView tvHistoryNum;
    private TextView tvm3u8AdText;
    private TextView tvDns;

    @Override
    protected int getLayoutResID() {
        return R.layout.activity_personal_settings;
    }

    @Override
    protected void init() {
        initView();
        initData();
        initClickListener();
        // 设置焦点到第一个设置项
        findViewById(R.id.tvHomeLive).requestFocus();
    }

    private void initView() {
        tvHomeDefaultShow = findViewById(R.id.tvHomeText);
        tvHomeRec = findViewById(R.id.tvHomeRec);
        tvRecStyleText = findViewById(R.id.showRecStyleText);
        tvSearchView = findViewById(R.id.tvSearchView);
        tvFastSearchText = findViewById(R.id.showFastSearchText);
        tvHistoryMerge = findViewById(R.id.tvHistoryMerge);
        tvHistoryNum = findViewById(R.id.tvHistoryNum);
        tvm3u8AdText = findViewById(R.id.m3u8AdText);
        tvDns = findViewById(R.id.tvDns);
    }

    private void initData() {
        tvHomeDefaultShow.setText(Hawk.get(HawkConfig.DEFAULT_LOAD_LIVE, false) ? "直播" : "点播");
        tvHomeRec.setText(getHomeRecName(Hawk.get(HawkConfig.HOME_REC, HawkConfig.DEFAULT_HOME_REC)));
        tvRecStyleText.setText(Hawk.get(HawkConfig.HOME_REC_STYLE, false) ? "是" : "否");
        tvSearchView.setText(getSearchView(Hawk.get(HawkConfig.SEARCH_VIEW, 0)));
        tvFastSearchText.setText(Hawk.get(HawkConfig.FAST_SEARCH_MODE, true) ? "开启" : "关闭");
        tvHistoryMerge.setText(Hawk.get(HawkConfig.HISTORY_MERGE, false) ? "开启" : "关闭");
        tvHistoryNum.setText(HistoryHelper.getHistoryNumName(Hawk.get(HawkConfig.HISTORY_NUM, 0)));
        tvm3u8AdText.setText(Hawk.get(HawkConfig.M3U8_PURIFY, false) ? "开启" : "关闭");
        tvDns.setText(OkGoHelper.dnsHttpsList.get(Hawk.get(HawkConfig.DOH_URL, 0)));
    }

    private void initClickListener() {
        // 下次进入设置
        findViewById(R.id.tvHomeLive).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FastClickCheckUtil.check(v);
                Hawk.put(HawkConfig.DEFAULT_LOAD_LIVE, !Hawk.get(HawkConfig.DEFAULT_LOAD_LIVE, false));
                tvHomeDefaultShow.setText(Hawk.get(HawkConfig.DEFAULT_LOAD_LIVE, false) ? "直播" : "点播");
            }
        });

        // 首页推荐设置
        findViewById(R.id.llHomeRec).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FastClickCheckUtil.check(v);
                int defaultPos = Hawk.get(HawkConfig.HOME_REC, HawkConfig.DEFAULT_HOME_REC);
                ArrayList<Integer> types = new ArrayList<>();
                types.add(0);
                types.add(1);
                types.add(2);
                SelectDialog<Integer> dialog = new SelectDialog<Integer>(mContext);
                dialog.setTip("请选择首页列表数据");
                dialog.setAdapter(new SelectDialogAdapter.SelectDialogInterface<Integer>() {
                    @Override
                    public void click(Integer value, int pos) {
                        Hawk.put(HawkConfig.HOME_REC, value);
                        tvHomeRec.setText(getHomeRecName(value));
                    }

                    @Override
                    public String getDisplay(Integer val) {
                        return getHomeRecName(val);
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
                }, types, defaultPos);
                dialog.show();
            }
        });

        // 首页多行设置
        findViewById(R.id.llHomeRecStyle).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FastClickCheckUtil.check(v);
                Hawk.put(HawkConfig.HOME_REC_STYLE, !Hawk.get(HawkConfig.HOME_REC_STYLE, false));
                tvRecStyleText.setText(Hawk.get(HawkConfig.HOME_REC_STYLE, false) ? "是" : "否");
            }
        });

        // 首页菜单设置
        findViewById(R.id.llHomeApi).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new HomeMenuDialog(mContext).show();
            }
        });

        // 首页源锁定（占位项，不绑定功能）
        findViewById(R.id.llHomeSourceLock).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 占位项，不执行任何操作
            }
        });

        // 搜索展示设置
        findViewById(R.id.llSearchView).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FastClickCheckUtil.check(v);
                int defaultPos = Hawk.get(HawkConfig.SEARCH_VIEW, 0);
                ArrayList<Integer> types = new ArrayList<>();
                types.add(0);
                types.add(1);
                SelectDialog<Integer> dialog = new SelectDialog<Integer>(mContext);
                dialog.setTip("请选择搜索视图");
                dialog.setAdapter(new SelectDialogAdapter.SelectDialogInterface<Integer>() {
                    @Override
                    public void click(Integer value, int pos) {
                        Hawk.put(HawkConfig.SEARCH_VIEW, value);
                        tvSearchView.setText(getSearchView(value));
                    }

                    @Override
                    public String getDisplay(Integer val) {
                        return getSearchView(val);
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
                }, types, defaultPos);
                dialog.show();
            }
        });

        // 聚合搜索设置
        findViewById(R.id.showFastSearch).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FastClickCheckUtil.check(v);
                Hawk.put(HawkConfig.FAST_SEARCH_MODE, !Hawk.get(HawkConfig.FAST_SEARCH_MODE, true));
                tvFastSearchText.setText(Hawk.get(HawkConfig.FAST_SEARCH_MODE, true) ? "开启" : "关闭");
            }
        });

        // 历史合并设置
        findViewById(R.id.llHistoryMerge).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FastClickCheckUtil.check(v);
                boolean historyMerge = !Hawk.get(HawkConfig.HISTORY_MERGE, false);
                Hawk.put(HawkConfig.HISTORY_MERGE, historyMerge);
                tvHistoryMerge.setText(historyMerge ? "开启" : "关闭");
            }
        });

        // 历史记录设置
        findViewById(R.id.llHistoryNum).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FastClickCheckUtil.check(v);
                int defaultPos = Hawk.get(HawkConfig.HISTORY_NUM, 0);
                ArrayList<Integer> types = new ArrayList<>();
                types.add(0);
                types.add(1);
                types.add(2);
                SelectDialog<Integer> dialog = new SelectDialog<Integer>(mContext);
                dialog.setTip("保留历史记录数量");
                dialog.setAdapter(new SelectDialogAdapter.SelectDialogInterface<Integer>() {
                    @Override
                    public void click(Integer value, int pos) {
                        Hawk.put(HawkConfig.HISTORY_NUM, value);
                        tvHistoryNum.setText(HistoryHelper.getHistoryNumName(value));
                    }

                    @Override
                    public String getDisplay(Integer val) {
                        return HistoryHelper.getHistoryNumName(val);
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
                }, types, defaultPos);
                dialog.show();
            }
        });

        // 换张壁纸设置
        findViewById(R.id.llWp).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FastClickCheckUtil.check(v);
                if (!ApiConfig.get().wallpaper.isEmpty())
                    OkGo.<File>get(ApiConfig.get().wallpaper).execute(new FileCallback(getFilesDir().getAbsolutePath(), "wp") {
                        @Override
                        public void onSuccess(Response<File> response) {
                            ((BaseActivity) mContext).changeWallpaper(true);
                        }

                        @Override
                        public void onError(Response<File> response) {
                            super.onError(response);
                        }

                        @Override
                        public void downloadProgress(Progress progress) {
                            super.downloadProgress(progress);
                        }
                    });
            }
        });

        // 重置壁纸设置
        findViewById(R.id.llWpRecovery).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FastClickCheckUtil.check(v);
                File wp = new File(getFilesDir().getAbsolutePath() + "/wp");
                if (wp.exists())
                    wp.delete();
                ((BaseActivity) mContext).changeWallpaper(true);
            }
        });

        // 去广告设置
        findViewById(R.id.m3u8Ad).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FastClickCheckUtil.check(v);
                boolean is_purify = Hawk.get(HawkConfig.M3U8_PURIFY, false);
                Hawk.put(HawkConfig.M3U8_PURIFY, !is_purify);
                tvm3u8AdText.setText(!is_purify ? "开启" : "关闭");
            }
        });

        // 安全DNS设置
        findViewById(R.id.llDns).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FastClickCheckUtil.check(v);
                int dohUrl = Hawk.get(HawkConfig.DOH_URL, 0);

                SelectDialog<String> dialog = new SelectDialog<String>(mContext);
                dialog.setTip("请选择安全DNS");
                dialog.setAdapter(new SelectDialogAdapter.SelectDialogInterface<String>() {
                    @Override
                    public void click(String value, int pos) {
                        tvDns.setText(OkGoHelper.dnsHttpsList.get(pos));
                        Hawk.put(HawkConfig.DOH_URL, pos);
                        OkGoHelper.reloadDns();
                        IjkMediaPlayer.toggleDotPort(pos > 0);
                    }

                    @Override
                    public String getDisplay(String val) {
                        return val;
                    }
                }, new DiffUtil.ItemCallback<String>() {
                    @Override
                    public boolean areItemsTheSame(@NonNull @NotNull String oldItem, @NonNull @NotNull String newItem) {
                        return oldItem.equals(newItem);
                    }

                    @Override
                    public boolean areContentsTheSame(@NonNull @NotNull String oldItem, @NonNull @NotNull String newItem) {
                        return oldItem.equals(newItem);
                    }
                }, OkGoHelper.dnsHttpsList, dohUrl);
                dialog.show();
            }
        });
    }

    private String getHomeRecName(int type) {
        if (type == 1) {
            return "站点推荐";
        } else if (type == 2) {
            return "观看历史";
        } else {
            return "豆瓣热播";
        }
    }

    private String getSearchView(int type) {
        if (type == 0) {
            return "文字列表";
        } else {
            return "缩略图";
        }
    }
}
