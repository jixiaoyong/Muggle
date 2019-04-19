package io.github.zeleven.mua;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import butterknife.ButterKnife;

public abstract class BaseEditorFragment extends Fragment {
    protected AppCompatActivity context;

    protected static boolean saved = false;
    protected static boolean fromFile = false;
    protected static String fileName;
    protected static String filePath;
    protected static String fileContent;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(getLayoutId(), container, false);
        ButterKnife.bind(this, view);
        context = (AppCompatActivity) getContext();
        initView();
        return view;
    }

    public void initView() {
        if (!fromFile) {
            saved = false;
            fileName = null;
            filePath = null;
            fileContent = null;
        }
    }

    public abstract int getLayoutId();
}
