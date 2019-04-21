package io.github.jixiaoyong.muggle.fragment;

import android.content.SharedPreferences;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;

import androidx.preference.PreferenceManager;

import java.util.Locale;

import butterknife.BindString;
import butterknife.BindView;
import io.github.jixiaoyong.muggle.R;
import io.github.jixiaoyong.muggle.fragment.base.BaseFragment;

public class HelpFragment extends BaseFragment {
    @BindString(R.string.drawer_item_help)
    String TITLE;
    @BindView(R.id.docs_webview)
    WebView webView;

    private SharedPreferences sharedPreferences;

    @Override
    public int getLayoutId() {
        return R.layout.fragment_help;
    }

    @Override
    public void initView() {
        toolbarTitle = TITLE;
        super.initView();
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        setWebView();
    }

    public void setWebView() {
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setDefaultFontSize(16);

        webView.setVerticalScrollBarEnabled(false);
        webView.setHorizontalScrollBarEnabled(false);
        webView.setWebChromeClient(new WebChromeClient());
        String value = sharedPreferences.getString("language", "");
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
        webView.loadUrl("file:///android_asset/markdown-cheatsheet-"
                + locale.getLanguage() + ".html");
    }
}
