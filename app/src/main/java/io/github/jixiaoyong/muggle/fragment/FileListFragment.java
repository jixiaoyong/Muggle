package io.github.jixiaoyong.muggle.fragment;

import android.Manifest;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.navigation.NavigationView;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import io.github.jixiaoyong.muggle.AppApplication;
import io.github.jixiaoyong.muggle.Constants;
import io.github.jixiaoyong.muggle.FileEntity;
import io.github.jixiaoyong.muggle.R;
import io.github.jixiaoyong.muggle.activity.MainActivity;
import io.github.jixiaoyong.muggle.adapter.FilesAdapterKt;
import io.github.jixiaoyong.muggle.api.bean.Repo;
import io.github.jixiaoyong.muggle.api.bean.RepoContent;
import io.github.jixiaoyong.muggle.databinding.FragmentFilelistBinding;
import io.github.jixiaoyong.muggle.fragment.base.BaseFragment;
import io.github.jixiaoyong.muggle.task.QueryTask;
import io.github.jixiaoyong.muggle.utils.FileUtils;
import io.github.jixiaoyong.muggle.utils.Logger;
import io.github.jixiaoyong.muggle.utils.SPUtils;
import io.github.jixiaoyong.muggle.utils.StorageHelper;
import io.github.jixiaoyong.muggle.viewmodel.MainActivityModel;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import pub.devrel.easypermissions.EasyPermissions;
import pub.devrel.easypermissions.PermissionRequest;

public class FileListFragment extends BaseFragment<FragmentFilelistBinding, MainActivityModel>
        implements EasyPermissions.PermissionCallbacks {
    private static final int REQUEST_WRITE_ES = 1;

    private String appName = "";
    private String root = Environment.getExternalStorageDirectory().toString();
    private String rootPath;

    private FilesAdapterKt adapter;
    private List<FileEntity> entityList;
    private List<FileEntity> beforeSearch;

    private SharedPreferences sharedPreferences;

    @Override
    public void initView() {
        appName = requireContext().getString(R.string.app_name);
        setToolbarTitle(appName);
        setDisplayHomeAsUpEnabled(false);
        super.initView();

        dataBinding.setViewModel(viewModel);

        initVar(); // init variable
        setFab(); // set floating action button
        setHasOptionsMenu(true); // set has options menu
        setDrawerToggle(); // set toggle for drawerlayout
        setNavigationViewItemListener(); // set navigation view item listener

        dataBinding.fileList.setLayoutManager(new LinearLayoutManager(compatActivity));
        dataBinding.fileList.setItemAnimator(new DefaultItemAnimator());
        dataBinding.fileList.addItemDecoration(new DividerItemDecoration(compatActivity,
                DividerItemDecoration.VERTICAL));

        viewModel.getLocalFileList().observe(this, new androidx.lifecycle.Observer<List<FileEntity>>() {
            @Override
            public void onChanged(List<FileEntity> fileEntities) {
                adapter = new FilesAdapterKt(fileEntities);
                dataBinding.fileList.setAdapter(adapter);
                adapter.checkVersion();
            }
        });

        if (!EasyPermissions.hasPermissions(requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            PermissionRequest request = new PermissionRequest.Builder(this,
                    REQUEST_WRITE_ES, Manifest.permission.WRITE_EXTERNAL_STORAGE).build();
            EasyPermissions.requestPermissions(request);
        } else {
            refreshLocalFileList(); // set recyclerview
        }
        setSwipeRefreshLayout(); // set swipe refresh layout
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshGitHubRepo();
    }

    private void initVar() {
        rootPath = root + "/" + appName + "/";
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(compatActivity);
    }

    /**
     * Set visibility to VISIBLE for floating action button menu, and set listener for the menu.
     * If the button menu expanded, change background color for menu container.
     * Otherwise, change background to transparent.
     */
    private void setFab() {
        final Fragment fragment = new EditorFragment();
        Bundle args = new Bundle();
        args.putBoolean("FROM_FILE", false);
        fragment.setArguments(args);
        dataBinding.createMarkdownBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                compatActivity.getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, fragment)
                        .addToBackStack(null)
                        .commit();
            }
        });
    }

    /**
     * Set toggle for drawer in toolbar.
     */
    private void setDrawerToggle() {
        ActionBarDrawerToggle drawerToggle = new ActionBarDrawerToggle(compatActivity, dataBinding.drawerLayout,
                toolbar, R.string.toggle_drawer_open, R.string.toggle_drawer_close);
        dataBinding.drawerLayout.addDrawerListener(drawerToggle);
        drawerToggle.syncState();
    }

    /**
     * Set item listener for navigation view.Open new fragment for each click action.
     */
    private void setNavigationViewItemListener() {
        dataBinding.navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem item) {
                        Fragment selectedFragment = null;
                        switch (item.getItemId()) {
                            case R.id.sync:
                                selectedFragment = new SyncFragment();
                                break;
                            case R.id.theme:
                                Toast.makeText(requireContext(), getString(R.string.change_theme_sorry_tips),
                                        Toast.LENGTH_SHORT).show();
                                break;
                            case R.id.help:
                                selectedFragment = new HelpFragment();
                                break;
                            case R.id.settings:
                                selectedFragment = new SettingsFragment();
                                break;
                        }
                        dataBinding.navigationView.setCheckedItem(item.getItemId());
                        if (selectedFragment != null) {
                            compatActivity.getSupportFragmentManager().beginTransaction()
                                    .replace(R.id.fragment_container, selectedFragment)
                                    .addToBackStack(null).commit();
                        }
                        return true;
                    }
                });
    }


    private void refreshLocalFileList() {
        if (StorageHelper.isExternalStorageReadable()) {
            entityList = FileUtils.listFiles(rootPath);
            viewModel.getLocalFileList().setValue(entityList);
        } else {
            Toast.makeText(compatActivity, R.string.toast_message_sdcard_unavailable,
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void setSwipeRefreshLayout() {
        dataBinding.swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                dataBinding.swipeRefreshLayout.setRefreshing(true);
                if (!EasyPermissions.hasPermissions(requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    PermissionRequest request = new PermissionRequest.Builder(requireActivity(),
                            REQUEST_WRITE_ES, Manifest.permission.WRITE_EXTERNAL_STORAGE).build();
                    EasyPermissions.requestPermissions(request);
                } else {
                    refreshGitHubRepo();
                }
                dataBinding.swipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    private void refreshGitHubRepo() {
        Repo repo = viewModel.getSelectRepo().getValue();
        if (repo == null) {
            Logger.d("selectRepo==null,return");
            return;
        }
        AppApplication.githubApiService.getUserRepoContent(repo.getOwner().getLogin(),
                repo.getName(), "")
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.newThread())
                .subscribe(new Observer<RepoContent[]>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(RepoContent[] repoContents) {
                        List<RepoContent> onlineRepoContents = new ArrayList<>();
                        for (RepoContent r : repoContents) {
                            if ("file".equals(r.getType()) && r.getName().toLowerCase().endsWith(".md")) {
                                onlineRepoContents.add(r);
                            }
                        }

                        //for old
                        MainActivity.selectRepoContent.clear();
                        MainActivity.selectRepoContent.addAll(onlineRepoContents);

                        entityList = FileUtils.listFiles(rootPath);
                        viewModel.getLocalFileList().setValue(entityList);
                        viewModel.getSelectRepoContent().setValue(onlineRepoContents);

                        Logger.d("got contents size" + onlineRepoContents.size());
                    }

                    @Override
                    public void onError(Throwable e) {
                        Logger.e("get onError", e);
                        Constants.token = "";
                        SPUtils.putString(Constants.KEY_OAUTH2_TOKEN, Constants.token);

                        viewModel.getToken().setValue("");

                        refreshLocalFileList();
                    }

                    @Override
                    public void onComplete() {
                        Logger.d("get end");
                    }
                });
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
        inflater.inflate(R.menu.filelist_fragment_menu, menu);
        initSearchView(menu);
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        if (item.getItemId() == R.id.sort) {
            AlertDialog.Builder sortDialog = new AlertDialog.Builder(compatActivity);
            sortDialog.setTitle(R.string.menu_item_sort);
            int sortTypeIndex = sharedPreferences.getInt("SORT_TYPE_INDEX", 0);
            sortDialog.setSingleChoiceItems(R.array.sort_options, sortTypeIndex,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    });
            sortDialog.setNegativeButton(R.string.cancel,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
            sortDialog.show();
        }
        return super.onOptionsItemSelected(item);
    }

    private void initSearchView(Menu menu) {
        MenuItem searchItem = menu.findItem(R.id.search);
        SearchManager searchManager = (SearchManager) compatActivity.getSystemService
                (Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(compatActivity.getComponentName()));
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (!TextUtils.isEmpty(query)) {
                    // search files
                    if (StorageHelper.isExternalStorageReadable()) {
                        beforeSearch = new ArrayList<>(entityList);
                        new QueryTask(rootPath, query, new QueryTask.Response() {
                            @Override
                            public void onTaskFinish(List<FileEntity> entityList) {
                                FileListFragment.this.entityList.clear();
                                FileListFragment.this.entityList.addAll(entityList);
                                adapter.notifyDataSetChanged();
                            }
                        }).execute();
                    } else {
                        Toast.makeText(compatActivity, R.string.toast_message_sdcard_unavailable,
                                Toast.LENGTH_SHORT).show();
                    }
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        searchItem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                if (entityList != null && adapter != null && beforeSearch != null) {
                    entityList.clear();
                    entityList.addAll(beforeSearch);
                    adapter.notifyDataSetChanged();
                }
                return true;
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
        if (requestCode == REQUEST_WRITE_ES) {
            refreshLocalFileList();
        }
    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        if (requestCode == REQUEST_WRITE_ES) {
            Toast.makeText(requireContext(), getString(R.string.no_write_sdcard_permission), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public int getLayoutId() {
        return R.layout.fragment_filelist;
    }

    @NotNull
    @Override
    protected Class<MainActivityModel> getViewModelClass() {
        return MainActivityModel.class;
    }

}
