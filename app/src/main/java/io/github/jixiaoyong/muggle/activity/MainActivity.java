package io.github.jixiaoyong.muggle.activity;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.BindString;
import butterknife.ButterKnife;
import io.github.jixiaoyong.muggle.Constants;
import io.github.jixiaoyong.muggle.R;
import io.github.jixiaoyong.muggle.api.bean.Repo;
import io.github.jixiaoyong.muggle.api.bean.RepoContent;
import io.github.jixiaoyong.muggle.api.bean.UserInfo;
import io.github.jixiaoyong.muggle.fragment.BackHolder;
import io.github.jixiaoyong.muggle.fragment.FileListFragment;
import io.github.jixiaoyong.muggle.utils.SPUtils;

public class MainActivity extends AppCompatActivity {
    @BindString(R.string.app_name)
    String appName;
    private SharedPreferences sharedPref;

    public static Repo selectRepo;
    public static List<RepoContent> selectRepoContent = new ArrayList<>();
    public static UserInfo userInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        selectRepo = SPUtils.getFromJson(Constants.KEY_SELECT_REPO_INFO, Repo.class);
        userInfo = SPUtils.getFromJson(Constants.KEY_USER_INFO, UserInfo.class);

        // get default shared preferences
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);

        // Setting language by shared preferences
        settingLanguage();

        // open file list fragment
        getSupportFragmentManager().beginTransaction().replace(
                R.id.fragment_container, new FileListFragment()).commit();

        setLightMode(this, true);
    }

    public void setLightMode(Activity activity, boolean isLightMode) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //切换到浅色状态栏模式，黑字
            activity.getWindow().getDecorView()
                    .setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        } else {
            //切换到深色模式，白字
            activity.getWindow().getDecorView()
                    .setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
        }
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

    public static RepoContent getGithubRepoConetnt(String fileName) {
        if (selectRepoContent != null && selectRepoContent.size() > 0) {
            for (RepoContent r : selectRepoContent) {
                if (fileName.equals(r.getName())) {
                    return r;
                }
            }
        }
        return null;
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
