package io.github.jixiaoyong.muggle.fragment;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import butterknife.BindString;
import butterknife.BindView;
import io.github.jixiaoyong.muggle.AppApplication;
import io.github.jixiaoyong.muggle.Constants;
import io.github.jixiaoyong.muggle.R;
import io.github.jixiaoyong.muggle.activity.MainActivity;
import io.github.jixiaoyong.muggle.api.bean.DeleteFileBody;
import io.github.jixiaoyong.muggle.api.bean.DeleteFileRespone;
import io.github.jixiaoyong.muggle.api.bean.RepoContent;
import io.github.jixiaoyong.muggle.fragment.base.BaseFragment;
import io.github.jixiaoyong.muggle.utils.AppOpener;
import io.github.jixiaoyong.muggle.utils.FileUtils;
import io.github.jixiaoyong.muggle.utils.Logger;
import io.github.jixiaoyong.muggle.utils.SPUtils;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

import static io.github.jixiaoyong.muggle.activity.MainActivity.selectRepo;
import static io.github.jixiaoyong.muggle.activity.MainActivity.userInfo;

public class SyncFragment extends BaseFragment {
    @BindString(R.string.drawer_item_sync)
    String TITLE;

    @BindView(R.id.login_github)
    Button loginGithub;

    @BindView(R.id.user_name)
    TextView userName;

    @BindView(R.id.select_repo)
    TextView selectRepoTv;

    @BindView(R.id.user_avatar)
    ImageView userAvatarImage;

    @BindView(R.id.repo_content)
    RecyclerView repoContentListView;


    @Override
    public int getLayoutId() {
        return R.layout.fragment_sync;
    }

    @Override
    public void initView() {
        toolbarTitle = TITLE;
        super.initView();

        if ("".equals(Constants.token) || userInfo == null || selectRepo == null) {
            loginGithub.setVisibility(View.VISIBLE);
        } else {
            loginGithub.setVisibility(View.GONE);
        }

        loginGithub.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AppOpener.openInCustomTabsOrBrowser(requireContext(), getOAuth2Url());
            }
        });

        if (selectRepo != null && userInfo != null) {
            userName.setText(userInfo.getName());
            selectRepoTv.setText(selectRepo.getName() + "\n( " + selectRepo.getHtmlUrl() + " )");

            Glide.with(this)
                    .load(userInfo.getAvatarUrl())
                    .centerCrop()
                    .placeholder(R.mipmap.ic_launcher_round)
                    .into(userAvatarImage);

            repoContentListView.setLayoutManager(new LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false));
            repoContentListView.setAdapter(new MAdapter(MainActivity.selectRepoContent));

            getRepoContent();

        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        AppOpener.openInCustomTabsOrBrowser(requireContext(), getOAuth2Url());
    }

    @Override
    public void onResume() {
        super.onResume();
        initView();
    }

    private void getRepoContent() {
        AppApplication.githubApiService.getUserRepoContent(selectRepo.getOwner().getLogin(),
                selectRepo.getName(), "")
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.newThread())
                .subscribe(new Observer<RepoContent[]>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(RepoContent[] repoContents) {
                        MainActivity.selectRepoContent.clear();
                        for (RepoContent r : repoContents) {
                            if ("file".equals(r.getType()) && r.getName().toLowerCase().endsWith(".md")) {
                                MainActivity.selectRepoContent.add(r);
                            }
                        }

                        repoContentListView.setAdapter(new MAdapter(MainActivity.selectRepoContent));

                        Logger.d("got contents size" + repoContents.length);
                    }

                    @Override
                    public void onError(Throwable e) {
                        Logger.e("get onError", e);
                        Constants.token = "";
                        SPUtils.putString(Constants.KEY_OAUTH2_TOKEN, Constants.token);
                        loginGithub.setVisibility(View.VISIBLE);
                        repoContentListView.setVisibility(View.GONE);
                    }

                    @Override
                    public void onComplete() {
                        Logger.d("get end");
                    }
                });
    }

    public String getOAuth2Url() {
        String randomState = UUID.randomUUID().toString();
        return Constants.OAUTH2_URL +
                "?client_id=" + Constants.MUGGLE_CLIENT_ID +
                "&scope=" + Constants.OAUTH2_SCOPE +
                "&state=" + randomState;
    }


    class MAdapter extends RecyclerView.Adapter<MAdapter.VH> {

        private List<RepoContent> contents;

        public MAdapter(List<RepoContent> _contents) {
            contents = _contents;
        }

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_github_repo_contents, parent, false);
            return new VH(view);
        }

        @Override
        public void onBindViewHolder(@NonNull final VH holder, int position) {
            final RepoContent repoContent = contents.get(position);
            holder.textView.setText(repoContent.getName());
            holder.download.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {
                    Request request = new Request.Builder().get().url(repoContent.getDownloadUrl()).build();
                    Call call = AppApplication.okHttpClient.newCall(request);
                    call.enqueue(new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            Logger.e("error", e);
                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            try {
                                requireActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(requireContext(), requireContext().getString(R.string.download_success),
                                                Toast.LENGTH_SHORT).show();
                                    }
                                });
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            Logger.d("ok");
                            FileUtils.saveFile(Constants.LOCAL_FILE_PATH + repoContent.getName(),
                                    response.body().byteStream(), true);
                        }
                    });
                }
            });

            holder.delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder deleteDialog = new AlertDialog.Builder(context);
                    deleteDialog.setTitle(context.getString(R.string.dialog_message_delete_files_confirm));
                    deleteDialog.setMessage(context.getString(R.string.delete_cloud_tips));
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
                                    AppApplication.githubApiService.deleteFile(
                                            selectRepo.getOwner().getLogin(), selectRepo.getName(), repoContent.getPath(),
                                            new DeleteFileBody("Delete By Muggle", repoContent.getSha()))
                                            .observeOn(AndroidSchedulers.mainThread())
                                            .subscribeOn(Schedulers.newThread())
                                            .subscribe(new Consumer<DeleteFileRespone>() {
                                                @Override
                                                public void accept(DeleteFileRespone deleteFileRespone) throws Exception {
                                                    Toast.makeText(requireContext(), requireContext().getString(R.string.delete_success),
                                                            Toast.LENGTH_SHORT).show();
                                                    getRepoContent();
                                                }
                                            }, new Consumer<Throwable>() {
                                                @Override
                                                public void accept(Throwable throwable) throws Exception {
                                                    Logger.e("error", throwable);
                                                }
                                            });
                                }
                            });
                    deleteDialog.show();


                }
            });

        }

        @Override
        public int getItemCount() {
            return contents.size();
        }

        class VH extends RecyclerView.ViewHolder {
            TextView textView;
            ImageView download;
            ImageView delete;

            VH(@NonNull View itemView) {
                super(itemView);
                textView = itemView.findViewById(R.id.textview);
                download = itemView.findViewById(R.id.download);
                delete = itemView.findViewById(R.id.delete);
            }
        }
    }


}
