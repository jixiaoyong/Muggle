package io.github.jixiaoyong.muggle.fragment;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

import java.util.Locale;

import io.github.jixiaoyong.muggle.R;

public class HelpFragment extends Fragment {
    protected AppCompatActivity context; // compatActivity object
    protected View view; // fragment view object
    protected Toolbar toolbar;
    private String toolbarTitle;

    // If true, set back arrow in toolbar.
    private boolean setDisplayHomeAsUpEnabled = true;

    WebView webView;

    private SharedPreferences sharedPreferences;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        view = inflater.inflate(getLayoutId(), container, false);
        context = (AppCompatActivity) getActivity();
        webView = view.findViewById(R.id.docs_webview);
        initView();
        return view;
    }


    public int getLayoutId() {
        return R.layout.fragment_help;
    }

    public void initView() {
        toolbarTitle = getString(R.string.drawer_item_help);
        toolbar = view.findViewById(R.id.toolbar);
        if (toolbar != null) {
            if (toolbarTitle != null) {
                toolbar.setTitle(toolbarTitle);
            }
            context.setSupportActionBar(toolbar);
            if (setDisplayHomeAsUpEnabled) {
                context.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            }
        }
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        setWebView();
    }

    private void setWebView() {
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
