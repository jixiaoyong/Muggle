package io.github.jixiaoyong.muggle.activity;

import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.DisplayMetrics;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.preference.PreferenceManager;

import java.util.Locale;

import io.github.jixiaoyong.muggle.Constants;
import io.github.jixiaoyong.muggle.R;
import io.github.jixiaoyong.muggle.api.bean.Repo;
import io.github.jixiaoyong.muggle.api.bean.UserInfo;
import io.github.jixiaoyong.muggle.fragment.BackHolder;
import io.github.jixiaoyong.muggle.fragment.FileListFragment;
import io.github.jixiaoyong.muggle.utils.AppUtils;
import io.github.jixiaoyong.muggle.utils.SPUtils;
import io.github.jixiaoyong.muggle.viewmodel.MainActivityModel;

public class MainActivity extends AppCompatActivity {

    private SharedPreferences sharedPref;
    private MainActivityModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // get default shared preferences
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);

        // Setting language by shared preferences
        settingLanguage();

        // open file list fragment
        getSupportFragmentManager().beginTransaction().replace(
                R.id.fragment_container, new FileListFragment()).commit();

        AppUtils.setLightMode(this, true);

        viewModel = ViewModelProviders.of(this).get(MainActivityModel.class);
    }

    @Override
    protected void onResume() {
        super.onResume();

        //兼容其他尚未改动的地方
        Constants.token = SPUtils.getString(Constants.KEY_OAUTH2_TOKEN, "");

        //为启用了JetPack的类提供
        viewModel.getSelectRepo().setValue(SPUtils.getFromJson(Constants.KEY_SELECT_REPO_INFO, Repo.class));
        viewModel.getToken().setValue(SPUtils.getString(Constants.KEY_OAUTH2_TOKEN, ""));
        viewModel.getUserInfo().setValue(SPUtils.getFromJson(Constants.KEY_USER_INFO, UserInfo.class));
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onBackPressed() {
        boolean isCustomBackPressed = callOnFragmentBackPressed();
        if (!isCustomBackPressed) {
            super.onBackPressed();
        }
    }

    private boolean callOnFragmentBackPressed() {
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        if ((currentFragment instanceof BackHolder)) {
            return ((BackHolder) currentFragment).onBackPressed();
        }
        return false;
    }

    public void settingLanguage() {
        Resources res = getResources();
        Configuration cfg = res.getConfiguration();
        DisplayMetrics metrics = res.getDisplayMetrics();
        String value = sharedPref.getString("language", "");
        Locale locale;
        if (value.equals("auto")) {
            locale = Locale.getDefault();
        } else {
            if (value.contains("_")) {
                String[] parts = value.split("_");
                locale = new Locale(parts[0], parts[1]);
            } else {
                locale = new Locale(value);
            }
        }
        cfg.setLocale(locale);
        res.updateConfiguration(cfg, metrics);
    }
}
