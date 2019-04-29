package io.github.jixiaoyong.muggle.fragment;

import android.view.Menu;
import android.view.MenuInflater;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.jetbrains.annotations.NotNull;

import io.github.jixiaoyong.muggle.ContentChangedEvent;
import io.github.jixiaoyong.muggle.R;
import io.github.jixiaoyong.muggle.databinding.FragmentPreviewBinding;
import io.github.jixiaoyong.muggle.fragment.base.DatabindingBaseEditorFragmentKt;
import io.github.jixiaoyong.muggle.viewmodel.MainActivityModel;


public class PreviewFragment extends DatabindingBaseEditorFragmentKt<FragmentPreviewBinding, MainActivityModel> {

    private boolean pageFinish = false;

    @Override
    public int getLayoutId() {
        return R.layout.fragment_preview;
    }

    @Override
    public void initView() {
        super.initView();
        setHasOptionsMenu(true);
        configWebView();
    }

    @Subscribe
    public void onContentChangedEvent(ContentChangedEvent event) {
        if (event.content != null && !event.content.equals("")) {
            loadMarkdown(event.content);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.preview_fragment_menu, menu);
    }

    public void configWebView() {
        WebSettings webSettings = dataBinding.markdownContent.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);

        dataBinding.markdownContent.setVerticalScrollBarEnabled(false);
        dataBinding.markdownContent.setHorizontalScrollBarEnabled(false);
        dataBinding.markdownContent.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                if (newProgress == 100) {
                    pageFinish = true;
                }
            }
        });
        dataBinding.markdownContent.loadUrl("file:///android_asset/markdown.html");
    }

    public void loadMarkdown(String markdown) {
        if (pageFinish) {
            String content = markdown.replace("\n", "\\n").replace("\"", "\\\"")
                    .replace("'", "\\'");

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
                dataBinding.markdownContent.evaluateJavascript("javascript:parseMarkdown(\"" + content + "\");", null);
            } else {
                dataBinding.markdownContent.loadUrl("javascript:parseMarkdown(\"" + content + "\");");
            }
        }
    }

    @NotNull
    @Override
    protected Class<MainActivityModel> getViewModelClass() {
        return MainActivityModel.class;
    }
}
