package com.github.tvbox.osc.ui.fragment;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.OpenableColumns;
import android.os.Handler;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;

import com.github.tvbox.osc.R;
import com.github.tvbox.osc.api.ApiConfig;
import com.github.tvbox.osc.base.BaseActivity;
import com.github.tvbox.osc.base.BaseLazyFragment;
import com.github.tvbox.osc.bean.SourceBean;
import com.github.tvbox.osc.event.RefreshEvent;
import com.github.tvbox.osc.ui.activity.DanmuSettingsActivity;
import com.github.tvbox.osc.ui.activity.LocalFileActivity;
import com.github.tvbox.osc.ui.activity.PersonalSettingsActivity;
import com.github.tvbox.osc.ui.activity.PlaySettingsActivity;
import com.github.tvbox.osc.ui.activity.SettingActivity;
import com.github.tvbox.osc.ui.adapter.ApiHistoryDialogAdapter;
import com.github.tvbox.osc.ui.adapter.SelectDialogAdapter;
import com.github.tvbox.osc.ui.dialog.AboutDialog;
import com.github.tvbox.osc.ui.dialog.ApiDialog;
import com.github.tvbox.osc.ui.dialog.ApiHistoryDialog;
import com.github.tvbox.osc.ui.dialog.BackupDialog;
import com.github.tvbox.osc.ui.dialog.HomeMenuDialog;
import com.github.tvbox.osc.ui.dialog.HomePasswordDialog;
import com.github.tvbox.osc.ui.dialog.SearchRemoteTvDialog;
import com.github.tvbox.osc.ui.dialog.SelectDialog;
import com.github.tvbox.osc.util.FastClickCheckUtil;
import com.github.tvbox.osc.util.FileUtils;
import com.github.tvbox.osc.util.HawkConfig;
import com.github.tvbox.osc.util.HistoryHelper;
import com.github.tvbox.osc.util.LOG;
import com.github.tvbox.osc.util.OkGoHelper;
import com.github.tvbox.osc.viewmodel.SourceViewModel;
import com.orhanobut.hawk.Hawk;
import com.hjq.permissions.OnPermissionCallback;
import com.hjq.permissions.Permission;
import com.hjq.permissions.XXPermissions;

import org.greenrobot.eventbus.EventBus;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * @author pj567
 * @date :2020/12/23
 * @description:
 */
public class ModelSettingFragment extends BaseLazyFragment {
    private static final int REQUEST_LOCAL_CONFIG = 1001;
    private View lastFocusedView = null; // 记住焦点位置
    private TextView tvDebugOpen;
    private TextView tvApi;
    private TextView tvApiLine;
    private View llApi;
    private TextView tvHomeApi;
    private ApiDialog apiDialog;
    private boolean selectLocalLive;

    public static ModelSettingFragment newInstance() {
        return new ModelSettingFragment().setArguments();
    }

    public ModelSettingFragment setArguments() {
        return this;
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_model;
    }

    @Override
    protected void init() {
        tvDebugOpen = findViewById(R.id.tvDebugOpen);
        llApi = findViewById(R.id.llApi);
        tvApi = findViewById(R.id.tvApi);
        tvApiLine = findViewById(R.id.tvHomeApiTop);
        tvHomeApi = findViewById(R.id.tvHomeApiTop);

        tvDebugOpen.setText(Hawk.get(HawkConfig.DEBUG_OPEN, false) ? "已打开" : "已关闭");
        tvApi.setText(Hawk.get(HawkConfig.API_URL, ""));
        refreshApiLineText();
        tvHomeApi.setText(ApiConfig.get().getHomeSourceBean().getName());

        // 调试模式
        findViewById(R.id.llDebug).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FastClickCheckUtil.check(v);
                Hawk.put(HawkConfig.DEBUG_OPEN, !Hawk.get(HawkConfig.DEBUG_OPEN, false));
                tvDebugOpen.setText(Hawk.get(HawkConfig.DEBUG_OPEN, false) ? "已打开" : "已关闭");
            }
        });

        // 配置地址
        findViewById(R.id.llApi).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FastClickCheckUtil.check(v);
                apiDialog = new ApiDialog(mActivity);
                ApiDialog dialog = apiDialog;
                EventBus.getDefault().register(dialog);
                dialog.setOnListener(new ApiDialog.OnListener() {
                    @Override
                    public void onchange(String api) {
                        String oldApi = Hawk.get(HawkConfig.API_URL, "");
                        Hawk.put(HawkConfig.API_URL, api);
                        if (!HistoryHelper.isApiLineHistory(api)) {
                            HistoryHelper.clearApiLineList();
                        }
                        tvApi.setText(api);
                        refreshApiLineText();
                        if (!oldApi.equals(api)) {
                            restartAppAfterConfigChanged();
                        }
                    }

                    @Override
                    public void onLocalConfig(boolean live) {
                        openLocalConfig(live);
                    }
                });
                dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        ((BaseActivity) mActivity).hideSysBar();
                        EventBus.getDefault().unregister(dialog);
                        apiDialog = null;
                    }
                });
                dialog.show();
            }
        });

        // 首页站源
        findViewById(R.id.llHomeApiTop).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FastClickCheckUtil.check(v);
                List<SourceBean> sites = ApiConfig.get().getSwitchSourceBeanList();
                if (sites.size() > 0) {
                    SelectDialog<SourceBean> dialog = new SelectDialog<>(mActivity);
                    dialog.setTip("请选择首页数据源");
                    int select = sites.indexOf(ApiConfig.get().getHomeSourceBean());
                    if (select < 0) select = 0;
                    dialog.setAdapter(new SelectDialogAdapter.SelectDialogInterface<SourceBean>() {
                        @Override
                        public void click(SourceBean value, int pos) {
                            dialog.dismiss();
                            ApiConfig.get().setSourceBean(value);
                            tvHomeApi.setText(ApiConfig.get().getHomeSourceBean().getName());
                            EventBus.getDefault().post(new RefreshEvent(RefreshEvent.TYPE_HOME_SOURCE_CHANGE));
                        }

                        @Override
                        public String getDisplay(SourceBean val) {
                            return val.getName();
                        }
                    }, new DiffUtil.ItemCallback<SourceBean>() {
                        @Override
                        public boolean areItemsTheSame(@NonNull @NotNull SourceBean oldItem, @NonNull @NotNull SourceBean newItem) {
                            return oldItem == newItem;
                        }

                        @Override
                        public boolean areContentsTheSame(@NonNull @NotNull SourceBean oldItem, @NonNull @NotNull SourceBean newItem) {
                            return oldItem.getKey().equals(newItem.getKey());
                        }
                    }, sites, select);
                    dialog.show();
                }
            }
        });

        // 播放设置 - 跳转到 PlaySettingsActivity
        findViewById(R.id.llPlaySettings).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FastClickCheckUtil.check(v);
                lastFocusedView = v; // 记住焦点位置
                startActivity(new Intent(getActivity(), PlaySettingsActivity.class));
            }
        });

        // 弹幕设置 - 跳转到 DanmuSettingsActivity
        findViewById(R.id.llDanmuSettings).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FastClickCheckUtil.check(v);
                lastFocusedView = v; // 记住焦点位置
                startActivity(new Intent(getActivity(), DanmuSettingsActivity.class));
            }
        });

        // 个性设置 - 跳转到 PersonalSettingsActivity
        findViewById(R.id.llPersonalSettings).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FastClickCheckUtil.check(v);
                lastFocusedView = v; // 记住焦点位置
                startActivity(new Intent(getActivity(), PersonalSettingsActivity.class));
            }
        });

        // 密码锁
        findViewById(R.id.llPasswordLock).setOnClickListener(v -> new HomePasswordDialog(mActivity).show());

        // 数据备份
        findViewById(R.id.llBackup).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FastClickCheckUtil.check(v);
                BackupDialog dialog = new BackupDialog(mActivity);
                dialog.show();
            }
        });

        // 清空缓存
        findViewById(R.id.llClearCache).setOnClickListener((view -> onClickClearCache(view)));

        // 关于
        findViewById(R.id.llAbout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FastClickCheckUtil.check(v);
                AboutDialog dialog = new AboutDialog(mActivity);
                dialog.show();
            }
        });

        SettingActivity.callback = new SettingActivity.DevModeCallback() {
            @Override
            public void onChange() {
                findViewById(R.id.llDebug).setVisibility(View.VISIBLE);
            }
        };

        // 设置焦点到第一个设置项（配置地址）
        findViewById(R.id.llApi).requestFocus();
    }

    private void restartAppAfterConfigChanged() {
        Toast.makeText(mContext, "配置已切换,即将重新加载!", Toast.LENGTH_SHORT).show();
        clearConfigSwitchCache();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mActivity != null && !mActivity.isFinishing()) {
                    mActivity.onBackPressed();
                }
            }
        }, 2500);
    }

    private void clearConfigSwitchCache() {
        try {
            SourceViewModel.clearRuntimeCache();
            LOG.i("echo-clear-config-switch-cache");
        } catch (Exception e) {
            LOG.i("echo-clear-config-switch-cache-error:" + e.getMessage());
            e.printStackTrace();
        }
    }

    private void refreshApiLineText() {
        if (tvApiLine == null) return;
        tvApiLine.setText(ApiConfig.get().getHomeSourceBean().getName());
    }

    private void openLocalConfig(boolean live) {
        selectLocalLive = live;
        if (!XXPermissions.isGranted(mContext, Permission.Group.STORAGE)) {
            Toast.makeText(getContext(), "请选择文件前需要先授予存储权限", Toast.LENGTH_SHORT).show();
            XXPermissions.with(mActivity)
                    .permission(Permission.Group.STORAGE)
                    .request(new OnPermissionCallback() {
                        @Override
                        public void onGranted(List<String> permissions, boolean all) {
                            if (all) {
                                Toast.makeText(getContext(), "已获得存储权限", Toast.LENGTH_SHORT).show();
                                openLocalFileActivity(selectLocalLive);
                            }
                        }

                        @Override
                        public void onDenied(List<String> permissions, boolean never) {
                            if (never) {
                                Toast.makeText(getContext(), "获取存储权限失败,请在系统设置中开启", Toast.LENGTH_SHORT).show();
                                XXPermissions.startPermissionActivity(mActivity, permissions);
                            } else {
                                Toast.makeText(getContext(), "获取存储权限失败", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
            return;
        }
        openLocalFileActivity(live);
    }

    private void openLocalFileActivity(boolean live) {
        Intent intent = new Intent(mContext, LocalFileActivity.class);
        intent.putExtra(LocalFileActivity.EXTRA_LIVE, live);
        startActivityForResult(intent, REQUEST_LOCAL_CONFIG);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode != REQUEST_LOCAL_CONFIG || resultCode != android.app.Activity.RESULT_OK || data == null || data.getData() == null) {
            return;
        }
        String api = localConfigToApi(data.getData());
        if (api == null || api.isEmpty()) {
            Toast.makeText(getContext(), "读取本地配置失败", Toast.LENGTH_SHORT).show();
            return;
        }
        if (apiDialog != null) {
            apiDialog.setLocalApi(api, selectLocalLive);
        }
    }

    private String localConfigToApi(Uri uri) {
        String path = getPathFromUri(uri);
        if (path == null || path.isEmpty()) {
            path = copyUriToLocalConfig(uri);
        }
        if (path == null || path.isEmpty()) {
            return "";
        }
        String storageRoot = Environment.getExternalStorageDirectory().getAbsolutePath();
        if (path.startsWith(storageRoot)) {
            return "clan://localhost/" + path.substring(storageRoot.length()).replaceFirst("^/+", "");
        }
        path = copyUriToLocalConfig(uri);
        if (path != null && path.startsWith(storageRoot)) {
            return "clan://localhost/" + path.substring(storageRoot.length()).replaceFirst("^/+", "");
        }
        return "";
    }

    private String getPathFromUri(Uri uri) {
        try {
            if ("file".equalsIgnoreCase(uri.getScheme())) {
                return uri.getPath();
            }
            if (DocumentsContract.isDocumentUri(mContext, uri)) {
                String docId = DocumentsContract.getDocumentId(uri);
                if ("com.android.externalstorage.documents".equals(uri.getAuthority())) {
                    String[] split = docId.split(":");
                    if (split.length > 1 && "primary".equalsIgnoreCase(split[0])) {
                        return Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + split[1];
                    }
                }
                if ("com.android.providers.downloads.documents".equals(uri.getAuthority()) && docId.startsWith("raw:")) {
                    return docId.substring(4);
                }
            }
        } catch (Throwable ignored) {
        }
        return null;
    }

    private String copyUriToLocalConfig(Uri uri) {
        InputStream input = null;
        FileOutputStream output = null;
        try {
            input = mContext.getContentResolver().openInputStream(uri);
            if (input == null) return "";
            File dir = new File(FileUtils.getExternalCachePath(), "config");
            if (!dir.exists()) dir.mkdirs();
            File file = new File(dir, getDisplayName(uri));
            output = new FileOutputStream(file);
            byte[] buffer = new byte[8192];
            int length;
            while ((length = input.read(buffer)) != -1) {
                output.write(buffer, 0, length);
            }
            return file.getAbsolutePath();
        } catch (Throwable th) {
            th.printStackTrace();
            return "";
        } finally {
            try {
                if (output != null) output.close();
            } catch (Throwable ignored) {
            }
            try {
                if (input != null) input.close();
            } catch (Throwable ignored) {
            }
        }
    }

    private String getDisplayName(Uri uri) {
        String name = "local_config.json";
        Cursor cursor = null;
        try {
            cursor = mContext.getContentResolver().query(uri, null, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                int index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if (index >= 0) {
                    String displayName = cursor.getString(index);
                    if (displayName != null && !displayName.isEmpty()) {
                        name = displayName;
                    }
                }
            }
        } catch (Throwable ignored) {
        } finally {
            if (cursor != null) cursor.close();
        }
        return name;
    }

    private void onClickClearCache(View v) {
        FastClickCheckUtil.check(v);
        String cachePath = FileUtils.getCachePath();
        File cacheDir = new File(cachePath);
        new Thread(() -> {
            try {
                ApiConfig.get().clearSpiderCache();
                if (cacheDir.exists()) FileUtils.cleanDirectory(cacheDir);
                FileUtils.clearSpiderCacheFiles();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (mActivity != null) {
                    mActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            restartAppAfterCacheCleared();
                        }
                    });
                }
            }
        }).start();
    }

    private void restartAppAfterCacheCleared() {
        Toast.makeText(mContext, "缓存已清空,即将回到主页!", Toast.LENGTH_LONG).show();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                restartApp();
            }
        }, 2500);
    }

    private void restartApp() {
        if (mContext == null) return;
        Intent intent = mContext.getPackageManager().getLaunchIntentForPackage(mContext.getPackageName());
        if (intent != null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            System.exit(0);
        }
    }

    public static SearchRemoteTvDialog loadingSearchRemoteTvDialog;
    public static List<String> remoteTvHostList;
    public static boolean foundRemoteTv;

    @Override
    public void onResume() {
        super.onResume();
        // 恢复焦点到之前的位置
        if (lastFocusedView != null) {
            lastFocusedView.requestFocus();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        SettingActivity.callback = null;
    }
}
