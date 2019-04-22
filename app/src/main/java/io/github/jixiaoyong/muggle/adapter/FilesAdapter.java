package io.github.jixiaoyong.muggle.adapter;

import android.content.DialogInterface;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import io.github.jixiaoyong.muggle.AppApplication;
import io.github.jixiaoyong.muggle.Constants;
import io.github.jixiaoyong.muggle.FileEntity;
import io.github.jixiaoyong.muggle.R;
import io.github.jixiaoyong.muggle.activity.MainActivity;
import io.github.jixiaoyong.muggle.api.bean.Committer;
import io.github.jixiaoyong.muggle.api.bean.CreateFileBody;
import io.github.jixiaoyong.muggle.api.bean.CreateNewFileRespone;
import io.github.jixiaoyong.muggle.api.bean.GetCommitRespone;
import io.github.jixiaoyong.muggle.api.bean.RepoContent;
import io.github.jixiaoyong.muggle.api.bean.UpdateFileBody;
import io.github.jixiaoyong.muggle.api.bean.UpdateFileRespone;
import io.github.jixiaoyong.muggle.fragment.EditorFragment;
import io.github.jixiaoyong.muggle.utils.FileUtils;
import io.github.jixiaoyong.muggle.utils.GitUtils;
import io.github.jixiaoyong.muggle.utils.Logger;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

import static io.github.jixiaoyong.muggle.activity.MainActivity.selectRepo;
import static io.github.jixiaoyong.muggle.activity.MainActivity.userInfo;

public class FilesAdapter extends RecyclerView.Adapter<FilesAdapter.ViewHolder> {
    private List<FileEntity> dataSet;
    private AppCompatActivity context;

    public FilesAdapter(List<FileEntity> entityList) {
        dataSet = (entityList == null) ? new ArrayList<FileEntity>() : entityList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (context == null) {
            context = (MainActivity) parent.getContext();
        }
        View itemView = LayoutInflater.from(context)
                .inflate(R.layout.file_list_item, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        if (position >= dataSet.size()) {
            holder.fileName.setVisibility(View.GONE);
            holder.fileContent.setVisibility(View.INVISIBLE);
            holder.updateToGithub.setVisibility(View.GONE);
            holder.fileDate.setVisibility(View.GONE);
            holder.fileType.setVisibility(View.GONE);
            holder.fileSync.setVisibility(View.GONE);
            return;
        }

        final FileEntity entity = dataSet.get(position);
        final String fileName = entity.getName();
        holder.fileName.setText(fileName);

        String content = FileUtils.readContentFromFile(new File(entity.getAbsolutePath()), false);
        if (content.length() == 0) {
            holder.fileContent.setVisibility(View.GONE);
        } else {
            content = content.length() > 500 ? content.substring(0, 500) : content;
            holder.fileContent.setText(content);
        }

        holder.fileDate.setText(DateUtils.getRelativeTimeSpanString(entity.getLastModified(),
                System.currentTimeMillis(), DateUtils.FORMAT_ABBREV_ALL));

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Fragment fragment = new EditorFragment();

                Bundle args = new Bundle();
                args.putBoolean(Constants.BUNDLE_KEY_SAVED, true);
                args.putBoolean(Constants.BUNDLE_KEY_FROM_FILE, true);
                args.putString(Constants.BUNDLE_KEY_FILE_NAME,
                        FileUtils.stripExtension(entity.getName()));
                args.putString(Constants.BUNDLE_KEY_FILE_PATH, entity.getAbsolutePath());

                fragment.setArguments(args);
                context.getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, fragment)
                        .addToBackStack(null)
                        .commit();
            }
        });

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                AlertDialog.Builder deleteDialog = new AlertDialog.Builder(context);
                deleteDialog.setTitle(context.getString(R.string.dialog_message_delete_files_confirm));
                deleteDialog.setMessage(context.getString(R.string.delete_local_tips));
                deleteDialog.setNegativeButton(R.string.cancel,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });
                deleteDialog.setPositiveButton(R.string.menu_item_delete,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                boolean deleteResult = FileUtils.deleteFile(entity.getAbsolutePath());
                                if (deleteResult) {
                                    Toast.makeText(context, context.getString(R.string.delete_success), Toast.LENGTH_SHORT).show();
                                    dataSet.remove(position);
                                    notifyItemChanged(position);
                                }
                            }
                        });
                deleteDialog.show();
                return true;
            }
        });

        final RepoContent githubContent = MainActivity.getGithubRepoConetnt(fileName);

        holder.updateToGithub.setVisibility(View.VISIBLE);
        if (dataSet.get(position).getIsSynced() == 0) {
            holder.updateToGithub.setVisibility(View.INVISIBLE);
            holder.fileSync.setText("已同步");
            holder.fileType.setBackgroundResource(R.drawable.ic_file_synced);
        } else if (dataSet.get(position).getIsSynced() == -1) {
            holder.fileSync.setText("待上传");
            holder.fileType.setBackgroundResource(R.drawable.ic_file_wait_upload);
        } else if (dataSet.get(position).getIsSynced() == 1) {
            holder.fileSync.setText("待下载");
            holder.updateToGithub.setBackgroundResource(R.drawable.ic_download);
            holder.fileType.setBackgroundResource(R.drawable.ic_file_to_download);
        } else {
            holder.fileSync.setText("本地文件");
            holder.fileType.setBackgroundResource(R.drawable.ic_file_local);
        }

        if (userInfo != null && selectRepo != null) {
            holder.updateToGithub.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (githubContent != null) {
                        if (dataSet.get(position).getIsSynced() == -1) {
                            //本地升级到在线
                            AppApplication.githubApiService.updateFile(selectRepo.getOwner().getLogin(),
                                    selectRepo.getName(), githubContent.getPath(), new UpdateFileBody(
                                            "update by Muggle",
                                            FileUtils.getByte64EncodeContent(Constants.LOCAL_FILE_PATH + fileName),
                                            githubContent.getSha(),
                                            new Committer(userInfo.getName(), userInfo.getEmail())))
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribeOn(Schedulers.newThread())
                                    .subscribe(new Consumer<UpdateFileRespone>() {
                                        @Override
                                        public void accept(UpdateFileRespone updateFileRespone) throws Exception {
                                            Toast.makeText(holder.itemView.getContext(),
                                                    holder.itemView.getContext().getString(R.string.upgrade_success), Toast.LENGTH_SHORT).show();
                                            Logger.d(updateFileRespone.getContent());
                                            notifyItemChanged(position);
                                        }
                                    }, new Consumer<Throwable>() {
                                        @Override
                                        public void accept(Throwable throwable) throws Exception {
                                            Logger.e("error", throwable);
                                        }
                                    });
                        } else if (dataSet.get(position).getIsSynced() == 1) {
                            //在线升级到本地，覆盖本地
                            Request request = new Request.Builder().get().url(githubContent.getDownloadUrl()).build();
                            Call call = AppApplication.okHttpClient.newCall(request);
                            call.enqueue(new Callback() {
                                @Override
                                public void onFailure(Call call, IOException e) {
                                    Logger.e("error", e);
                                }

                                @Override
                                public void onResponse(Call call, Response response) throws IOException {
                                    try {
                                        context.runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                Toast.makeText(context, context.getString(R.string.upgrade_success),
                                                        Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }

                                    FileUtils.saveFile(Constants.LOCAL_FILE_PATH + githubContent.getName(),
                                            response.body().byteStream(), true);
                                    Logger.d("save ok");

                                    context.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            checkVersion();
                                            notifyItemChanged(position);
                                            Logger.d("say hello ok");
                                        }
                                    });
                                }
                            });
                        }

                    } else {
                        //本地新建到在线
                        AppApplication.githubApiService.createNewFile(selectRepo.getOwner().getLogin(),
                                selectRepo.getName(), fileName, new CreateFileBody(
                                        "update by Muggle",
                                        FileUtils.getByte64EncodeContent(Constants.LOCAL_FILE_PATH + fileName),
                                        new Committer(userInfo.getName(), userInfo.getEmail())))
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribeOn(Schedulers.newThread())
                                .subscribe(new Consumer<CreateNewFileRespone>() {
                                    @Override
                                    public void accept(CreateNewFileRespone createNewFileRespone) throws Exception {
                                        Toast.makeText(holder.itemView.getContext(),
                                                holder.itemView.getContext().getString(R.string.upload_success), Toast.LENGTH_SHORT).show();
                                        Logger.d(createNewFileRespone.getContent());

                                        MainActivity.selectRepoContent.add(createNewFileRespone.getContent());
                                        checkVersion();
                                    }
                                }, new Consumer<Throwable>() {
                                    @Override
                                    public void accept(Throwable throwable) throws Exception {
                                        Logger.e("error", throwable);
                                    }
                                });
                    }
                }
            });
        } else {
            holder.updateToGithub.setVisibility(View.GONE);
        }
    }

    public void checkVersion() {
        List<FileEntity> newDataSet = new ArrayList<>();
        newDataSet.addAll(dataSet);
        if (newDataSet.size() <= 0) {
            return;
        }
        for (FileEntity data : newDataSet) {
            final RepoContent githubContent = MainActivity.getGithubRepoConetnt(data.getName());
            if (githubContent != null) {
                Logger.d(data.getName() + " githubContent.getSha():" + githubContent.getSha());
                Logger.d(data.getName() + " GitUtils.gitSHA1(data.getAbsolutePath()):"
                        + GitUtils.gitSHA1(data.getAbsolutePath()));
                if (!githubContent.getSha().equals(GitUtils.gitSHA1(data.getAbsolutePath()))) {
                    checkLastUpdateTime(githubContent, dataSet.indexOf(data));
                } else {
                    data.setIsSynced(0);
                    dataSet.set(dataSet.indexOf(data), data);
                }
            } else {
                data.setIsSynced(2);
                dataSet.set(dataSet.indexOf(data), data);
            }
        }

    }

    /**
     * @param githubContent
     * @param position
     */
    private void checkLastUpdateTime(final RepoContent githubContent, final int position) {
        final FileEntity entity = dataSet.get(position);

        SimpleDateFormat sdf = new SimpleDateFormat("YYYY-MM-dd'T'HH:mm:ssZ");
//        SimpleDateFormat sdf = new SimpleDateFormat("YYYY-MM-DD'T'HH:MM:SSZ");
        final String formData = sdf.format(new Date(entity.getLastModified()));
//        Logger.d(entity.getName() + " format data is " + formData);

        AppApplication.githubApiService.getUserRepoCommit(selectRepo.getOwner().getLogin(),
                selectRepo.getName(), githubContent.getPath(), formData)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.newThread())
                .subscribe(new Consumer<GetCommitRespone[]>() {
                    @Override
                    public void accept(GetCommitRespone[] getCommitRespones) throws Exception {

                        if (getCommitRespones.length > 0) {
                            Logger.d(githubContent.getName() + "在线有更新");
                            entity.setIsSynced(1);
                        } else {
                            Logger.d(githubContent.getName() + "本地已经是最新的，需要同步到云上");
                            entity.setIsSynced(-1);
                        }
                        dataSet.set(position, entity);
                        notifyItemChanged(position);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        Logger.e("error", throwable);
                    }
                });
    }

    @Override
    public int getItemCount() {
        return dataSet.size() + 1;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView fileName, fileContent, fileDate, fileSync;
        ImageView updateToGithub, fileType;

        public ViewHolder(View itemView) {
            super(itemView);
            fileName = itemView.findViewById(R.id.file_name);
            fileContent = itemView.findViewById(R.id.file_content);
            fileDate = itemView.findViewById(R.id.file_date);
            fileType = itemView.findViewById(R.id.file_type);
            fileSync = itemView.findViewById(R.id.file_sync);
            updateToGithub = itemView.findViewById(R.id.file_update_github);
        }
    }
}
