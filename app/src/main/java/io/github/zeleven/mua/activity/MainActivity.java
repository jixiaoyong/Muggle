package io.github.zeleven.mua.activity;

import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.DisplayMetrics;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

import java.util.Locale;

import butterknife.BindString;
import butterknife.ButterKnife;
import io.github.zeleven.mua.R;
import io.github.zeleven.mua.fragment.Backable;
import io.github.zeleven.mua.fragment.FileListFragment;

public class MainActivity extends AppCompatActivity {
    @BindString(R.string.app_name)
    String appName;
    private SharedPreferences sharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        // get default shared preferences
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);

        // Setting language by shared preferences
        settingLanguage();

        // open file list fragment
        getSupportFragmentManager().beginTransaction().replace(
                R.id.fragment_container, new FileListFragment()).commit();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onBackPressed() {

        boolean iCustomBack = callOnFragmentBackPressed();
        if (!iCustomBack) {
            super.onBackPressed();
        }

    }

    private boolean callOnFragmentBackPressed() {
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        if ((currentFragment instanceof Backable)) {
            return ((Backable) currentFragment).onBackPressed();
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
