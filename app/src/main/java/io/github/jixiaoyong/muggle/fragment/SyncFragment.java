package io.github.jixiaoyong.muggle.fragment;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.io.IOException;
import java.util.List;

import io.github.jixiaoyong.muggle.AppApplication;
import io.github.jixiaoyong.muggle.Constants;
import io.github.jixiaoyong.muggle.R;
import io.github.jixiaoyong.muggle.activity.LoginActivity;
import io.github.jixiaoyong.muggle.activity.MainActivity;
import io.github.jixiaoyong.muggle.api.bean.DeleteFileBody;
import io.github.jixiaoyong.muggle.api.bean.DeleteFileRespone;
import io.github.jixiaoyong.muggle.api.bean.RepoContent;
import io.github.jixiaoyong.muggle.databinding.FragmentSyncBinding;
import io.github.jixiaoyong.muggle.utils.AppOpener;
import io.github.jixiaoyong.muggle.utils.FileUtils;
import io.github.jixiaoyong.muggle.utils.GitUtils;
import io.github.jixiaoyong.muggle.utils.Logger;
import io.github.jixiaoyong.muggle.utils.SPUtils;
import io.github.jixiaoyong.muggle.viewmodel.MainActivityModel;
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

public class SyncFragment extends Fragment {

    protected AppCompatActivity context; // compatActivity object

    private FragmentSyncBinding dataBinding;
    private MainActivityModel viewModel;

    protected Toolbar toolbar;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        dataBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_sync, container, false);
        viewModel = ViewModelProviders.of(requireActivity()).get(MainActivityModel.class);
        viewModel.getToken().setValue(Constants.token);
        viewModel.getUserInfo().setValue(userInfo);
        dataBinding.setViewModel(viewModel);
        context = (AppCompatActivity) getActivity();

        initView();

        dataBinding.setLifecycleOwner(this);
        return dataBinding.getRoot();
    }

    public void initView() {

        viewModel.getSelectRepoContent().observe(this, new androidx.lifecycle.Observer<List<RepoContent>>() {
            @Override
            public void onChanged(List<RepoContent> repoContents) {
                dataBinding.repoContent.setLayoutManager(new LinearLayoutManager(requireContext(),
                        RecyclerView.VERTICAL, false));
                dataBinding.repoContent.setAdapter(new MAdapter(repoContents));
                dataBinding.repoContent.addItemDecoration(new DividerItemDecoration(context,
                        DividerItemDecoration.VERTICAL));
            }
        });

        dataBinding.swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getRepoContent();
                dataBinding.swipeRefreshLayout.setRefreshing(false);
            }
        });

        toolbar = dataBinding.getRoot().findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setTitle(R.string.drawer_item_sync);
            context.setSupportActionBar(toolbar);
            context.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        viewModel.isLogin().observe(this, new androidx.lifecycle.Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                setHasOptionsMenu(aBoolean);
            }
        });

        dataBinding.loginGithub.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AppOpener.openInCustomTabsOrBrowser(requireContext(), GitUtils.getOAuth2Url());
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        viewModel.checkLogin();
        getRepoContent();

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.sync_fragment_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.logout:

                break;
            case R.id.change_repo:
                startActivity(new Intent(requireContext(), LoginActivity.class));
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void getRepoContent() {
        if (selectRepo == null) {
            return;
        }
        viewModel.getSelectRepo().setValue(selectRepo);
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

                        viewModel.getSelectRepoContent().setValue(MainActivity.selectRepoContent);

                        Logger.d("got contents size" + repoContents.length);
                    }

                    @Override
                    public void onError(Throwable e) {
                        Logger.e("get onError", e);
                        Constants.token = "";
                        SPUtils.putString(Constants.KEY_OAUTH2_TOKEN, Constants.token);
                        viewModel.isLogin().postValue(false);
                    }

                    @Override
                    public void onComplete() {
                        Logger.d("get end");
                    }
                });
    }


    class MAdapter extends RecyclerView.Adapter<MAdapter.VH> {

        private List<RepoContent> contents;

        MAdapter(List<RepoContent> _contents) {
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
        public void onBindViewHolder(@NonNull final VH holder, final int position) {
            final RepoContent repoContent = contents.get(position);
            String fileName = repoContent.getName();
            holder.textView.setText(fileName);
            if (GitUtils.gitSHA1(Constants.LOCAL_FILE_PATH + fileName).equals(repoContent.getSha())) {
                //the file already been downloaded
                holder.download.setVisibility(View.GONE);
                Logger.d(fileName + "  local sha:" + GitUtils.gitSHA1(Constants.LOCAL_FILE_PATH + fileName)
                        + " git sha:" + repoContent.getSha());
            } else {
                holder.download.setVisibility(View.VISIBLE);
            }

            holder.download.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {

                    AlertDialog.Builder deleteDialog = new AlertDialog.Builder(context);
                    deleteDialog.setTitle(context.getString(R.string.dialog_message_download_files_confirm));
                    deleteDialog.setMessage(context.getString(R.string.download_and_overwritte_tips));
                    deleteDialog.setNegativeButton(R.string.cancel,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.cancel();
                                }
                            });
                    deleteDialog.setPositiveButton(R.string.menu_item_download,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
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
                                            FileUtils.saveFile(Constants.LOCAL_FILE_PATH + repoContent.getName(),
                                                    response.body().byteStream(), true);
                                            try {
                                                requireActivity().runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        notifyItemChanged(position);
                                                    }
                                                });
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    });
                                }
                            });
                    deleteDialog.show();

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
