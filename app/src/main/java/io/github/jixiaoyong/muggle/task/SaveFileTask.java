package io.github.jixiaoyong.muggle.task;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.text.TextUtils;
import android.widget.Toast;

import io.github.jixiaoyong.muggle.R;
import io.github.jixiaoyong.muggle.utils.FileUtils;

public class SaveFileTask extends AsyncTask<Void, Void, Boolean> {
    private Context context;
    private String filePath;
    private String fileName;
    private String content;
    private Response response;
    private boolean forceRewrite;

    public SaveFileTask(Context context, String filePath, String fileName, String content,
                        boolean forceRewrite, Response response) {
        this.context = context;
        this.filePath = filePath;
        this.fileName = fileName;
        this.content = content;
        this.response = response;
        this.forceRewrite = forceRewrite;
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        boolean result;
        if (TextUtils.isEmpty(fileName)) {
            toastMessage(R.string.toast_file_name_can_not_empty);
            result = false;
        } else {
            result = FileUtils.saveFile(filePath, content, forceRewrite);
            if (result) {
                toastMessage(R.string.toast_saved);
            } else {
                toastMessage(R.string.toast_file_name_exists);
            }
        }
        return result;
    }

    @Override
    protected void onPostExecute(Boolean aBoolean) {
        response.taskFinish(aBoolean);
    }

    public void toastMessage(final int resId) {
        Handler handler = new Handler(context.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context, resId, Toast.LENGTH_LONG).show();
            }
        });
    }

    public interface Response {
        void taskFinish(Boolean result);
    }
}
