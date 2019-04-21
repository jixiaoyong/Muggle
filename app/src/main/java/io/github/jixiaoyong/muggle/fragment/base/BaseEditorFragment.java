package io.github.jixiaoyong.muggle.fragment.base;

import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import butterknife.ButterKnife;
import io.github.jixiaoyong.muggle.R;
import io.github.jixiaoyong.muggle.task.SaveFileTask;

public abstract class BaseEditorFragment extends Fragment {
    protected AppCompatActivity context;

    protected static boolean isFileSaved = false;
    protected static boolean isContentChanged = false;
    protected static boolean fromFile = false;
    protected static String fileName;
    protected static String filePath;
    protected static String fileContent;
    protected static String currentContent;
    protected static String rootPath = Environment.getExternalStorageDirectory().toString() + "/${app_name}/";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootPath = rootPath.replace("${app_name}", getString(R.string.app_name));

        View view = inflater.inflate(getLayoutId(), container, false);
        ButterKnife.bind(this, view);
        context = (AppCompatActivity) getContext();
        initView();
        return view;
    }

    public void initView() {
        if (!fromFile) {
            isFileSaved = false;
            fileName = null;
            filePath = null;
            fileContent = "";
            currentContent = "";
        }
    }

    protected void showSaveFileDialog(final boolean forceRewrite) {
        AlertDialog.Builder saveDialog = new AlertDialog.Builder(context);
        saveDialog.setTitle(R.string.dialog_title_save_file);

        LayoutInflater inflater = context.getLayoutInflater();
        final View view = inflater.inflate(R.layout.dialog_save_file, null);
        final EditText fileNameET = view.findViewById(R.id.file_name);

        saveDialog.setView(view);
        saveDialog.setNeutralButton(R.string.dialog_btn_discard, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                isFileSaved = true;
                isContentChanged = false;
                requireActivity().onBackPressed();
            }
        });
        saveDialog.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        saveDialog.setPositiveButton(R.string.dialog_btn_save,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        fileName = fileNameET.getText().toString();
                        filePath = rootPath + fileName + ".md";
                        new SaveFileTask(context, filePath, fileName,
                                currentContent, forceRewrite, new SaveFileTask.Response() {
                            @Override
                            public void taskFinish(Boolean result) {
                                isFileSaved = result; // change isFileSaved value to true if save success
                            }
                        }).execute();
                    }
                });

        saveDialog.show();
    }

    public abstract int getLayoutId();
}
