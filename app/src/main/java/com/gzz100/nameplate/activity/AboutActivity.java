package com.gzz100.nameplate.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.gzz100.nameplate.R;
import com.gzz100.nameplate.utils.system.UpdateUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class AboutActivity extends BaseActivity {

    private static final int UPDATE_CODE = 100;
    private static final int IS_LATEST = 200;
    String mResult;
    UpdateInfo mUpdateInfo;
    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UPDATE_CODE:
                    showAlertDialog();
                    break;
                case IS_LATEST:
                    Toast.makeText(AboutActivity.this, R.string.tos_isLatest, Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };
    @BindView(R.id.tv_update_check)
    TextView mTvUpdateCheck;
    @BindView(R.id.tv_current_version)
    TextView mTvCurrentVersion;
    @BindView(R.id.tv_use_help)
    TextView mTvHelp;
    @BindView(R.id.rl_webSite)
    RelativeLayout rlWebsite;
    private void showAlertDialog() {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.foundUpdate)+mUpdateInfo.version)
                .setMessage(mUpdateInfo.whatsNews)
                .setPositiveButton(R.string.update, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(AboutActivity.this, R.string.tos_update, Toast.LENGTH_SHORT).show();
                        UpdateUtil.download(AboutActivity.this, mUpdateInfo.downloadUrl, getString(R.string.updateName));
                    }
                })
                .setNegativeButton(R.string.ignore, null)
                .show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        ButterKnife.bind(this);
        setVersion();
    }

    private void setVersion() {
        try {
            PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            mTvCurrentVersion.setText(getString(R.string.version)+packageInfo.versionName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void connectServer() {
        HttpURLConnection conn = null;
        try {
            URL url = new URL("http://www.gzz100.com/download/nameplate/android/updateInfo.json");
            conn = (HttpURLConnection) url.openConnection();

            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setConnectTimeout(30000);
            conn.setReadTimeout(30000);
            conn.setRequestProperty("contentType", "utf-8");
            conn.setRequestProperty("Accept-Charset", "utf-8");
            conn.setRequestProperty("Content-type", "text/html");
            conn.connect();
            int responseCode = conn.getResponseCode();
            if (responseCode == conn.HTTP_OK) {
                InputStream inputStream = conn.getInputStream();
                BufferedReader in = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
                StringBuffer buffer = new StringBuffer();
                String line = "";
                while ((line = in.readLine()) != null) {
                    buffer.append(line);
                }
                mResult = buffer.toString();

                JSONObject jsonObject = new JSONObject(buffer.toString());
                String version = jsonObject.getString("versionName");
                String name = jsonObject.getString("name");
                String downloadUrl = jsonObject.getString("url");
                String whatsnew = jsonObject.getString("whatsNew");
                mUpdateInfo = new UpdateInfo(version, name, downloadUrl, whatsnew);
                boolean needToUpdate = compareVersion(version);
                if (needToUpdate) {
                    mHandler.sendEmptyMessage(UPDATE_CODE);
                } else {
                    mHandler.sendEmptyMessage(IS_LATEST);
                }
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        } finally {
            conn.disconnect();
        }
   }


    private boolean compareVersion(String versionName) {
        try {
            PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            if (!versionName.equals(packageInfo.versionName)){
                return true;
            }

        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }

    @OnClick({R.id.tv_update_check,R.id.tv_use_help,R.id.rl_webSite})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_update_check:
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        connectServer();
                    }
                }).start();
                break;
            case R.id.tv_use_help:
                startActivity(new Intent(this,HelpActivity.class));
                break;
            case R.id.rl_webSite:
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                Uri content_url = Uri.parse("http://www.gzz100.com");
                intent.setData(content_url);
                startActivity(intent);
                break;
        }
    }

    class UpdateInfo {
        String version;
        String name;
        String downloadUrl;
        String whatsNews;

        public UpdateInfo(String version, String name, String downloadUrl, String whatsNews) {
            this.version = version;
            this.name = name;
            this.downloadUrl = downloadUrl;
            this.whatsNews = whatsNews;
        }

    }

    @Override
    public void onCreateCustomToolBar(Toolbar toolbar) {
        toolbar.setTitle(R.string.about);
    }
}
