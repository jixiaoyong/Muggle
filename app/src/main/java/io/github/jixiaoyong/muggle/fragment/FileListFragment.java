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
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SearchView;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindString;
import butterknife.BindView;
import io.github.jixiaoyong.muggle.AppApplication;
import io.github.jixiaoyong.muggle.Constants;
import io.github.jixiaoyong.muggle.FileEntity;
import io.github.jixiaoyong.muggle.R;
import io.github.jixiaoyong.muggle.activity.MainActivity;
import io.github.jixiaoyong.muggle.adapter.FilesAdapter;
import io.github.jixiaoyong.muggle.api.bean.RepoContent;
import io.github.jixiaoyong.muggle.fragment.base.BaseFragment;
import io.github.jixiaoyong.muggle.task.QueryTask;
import io.github.jixiaoyong.muggle.utils.FileUtils;
import io.github.jixiaoyong.muggle.utils.Logger;
import io.github.jixiaoyong.muggle.utils.SPUtils;
import io.github.jixiaoyong.muggle.utils.StorageHelper;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import pub.devrel.easypermissions.EasyPermissions;
import pub.devrel.easypermissions.PermissionRequest;

import static io.github.jixiaoyong.muggle.activity.MainActivity.selectRepo;

public class FileListFragment extends BaseFragment implements EasyPermissions.PermissionCallbacks {
    private static final int REQUEST_WRITE_ES = 1;

    @BindView(R.id.drawer_layout)
    DrawerLayout drawerLayout;
    @BindView(R.id.swipe_refresh_layout)
    SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.file_list)
    RecyclerView fileListRecyclerView;
    @BindView(R.id.create_markdown_btn)
    FloatingActionButton createMarkdownBtn;
    @BindView(R.id.navigation_view)
    NavigationView navigationView;
    @BindView(R.id.empty_list)
    RelativeLayout emptyList;

    @BindString(R.string.app_name)
    String appName;
    private String root = Environment.getExternalStorageDirectory().toString();
    private String rootPath;

    private FilesAdapter adapter;
    private List<FileEntity> entityList;
    private List<FileEntity> beforeSearch;

    private SharedPreferences sharedPreferences;

    @Override
    public int getLayoutId() {
        return R.layout.fragment_filelist;
    }

    @Override
    public void initView() {
        toolbarTitle = appName;
        setDisplayHomeAsUpEnabled = false;
        super.initView();

        initVar(); // init variable
        setFab(); // set floating action button
        setHasOptionsMenu(true); // set has options menu
        setDrawerToggle(); // set toggle for drawerlayout
        setNavigationViewItemListener(); // set navigation view item listener

        if (!EasyPermissions.hasPermissions(requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            PermissionRequest request = new PermissionRequest.Builder(this,
                    REQUEST_WRITE_ES, Manifest.permission.WRITE_EXTERNAL_STORAGE).build();
            EasyPermissions.requestPermissions(request);
        } else {
            setRecyclerView(); // set recyclerview
        }
        setSwipeRefreshLayout(); // set swipe refresh layout

        refreshGitHubRepo();

    }

    public void initVar() {
        rootPath = root + "/" + appName + "/";
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    /**
     * Set visibility to VISIBLE for floating action button menu, and set listener for the menu.
     * If the button menu expanded, change background color for menu container.
     * Otherwise, change background to transparent.
     */
    public void setFab() {
        final Fragment fragment = new EditorFragment();
        Bundle args = new Bundle();
        args.putBoolean("FROM_FILE", false);
        fragment.setArguments(args);
        createMarkdownBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                context.getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, fragment)
                        .addToBackStack(null)
                        .commit();
            }
        });
    }

    /**
     * Set toggle for drawer in toolbar.
     */
    public void setDrawerToggle() {
        ActionBarDrawerToggle drawerToggle = new ActionBarDrawerToggle(context, drawerLayout,
                toolbar, R.string.toggle_drawer_open, R.string.toggle_drawer_close);
        drawerLayout.addDrawerListener(drawerToggle);
        drawerToggle.syncState();
    }

    /**
     * Set item listener for navigation view.Open new fragment for each click action.
     */
    public void setNavigationViewItemListener() {
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem item) {
                        Fragment selectedFragment = null;
                        switch (item.getItemId()) {
                            case R.id.sync:
                                boolean isLogin = false;
                                if (isLogin) {
                                    syncFileToGithub();
                                } else {
                                    selectedFragment = new SyncFragment();
                                }
//                        AlertDialog.Builder builder = new AlertDialog.Builder(context);
//                        builder.setMessage(R.string.dialog_message_alert_user);
//                        builder.setPositiveButton(R.string.ok,
//                                new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialog, int which) {
//                                dialog.cancel();
//                            }
//                        });
//                        builder.show();
                                break;
                            case R.id.theme:
                                break;
                            case R.id.help:
                                selectedFragment = new HelpFragment();
                                break;
                            case R.id.settings:
                                selectedFragment = new SettingsFragment();
                                break;
                        }
                        navigationView.setCheckedItem(item.getItemId());
                        if (selectedFragment != null) {
                            context.getSupportFragmentManager().beginTransaction()
                                    .replace(R.id.fragment_container, selectedFragment)
                                    .addToBackStack(null).commit();
                        }
                        return true;
                    }
                });
    }

    private void syncFileToGithub() {

    }

    public void setRecyclerView() {
        if (StorageHelper.isExternalStorageReadable()) {
            entityList = FileUtils.listFiles(rootPath);
            if (entityList != null && entityList.isEmpty()) {
                emptyList.setVisibility(View.VISIBLE);
                fileListRecyclerView.setVisibility(View.GONE);
            } else {
                fileListRecyclerView.setVisibility(View.VISIBLE);
                emptyList.setVisibility(View.GONE);
                adapter = new FilesAdapter(entityList);
                fileListRecyclerView.setLayoutManager(new LinearLayoutManager(context));
                fileListRecyclerView.setItemAnimator(new DefaultItemAnimator());
                fileListRecyclerView.addItemDecoration(new DividerItemDecoration(context,
                        DividerItemDecoration.VERTICAL));
                fileListRecyclerView.setAdapter(adapter);
            }
        } else {
            Toast.makeText(context, R.string.toast_message_sdcard_unavailable,
                    Toast.LENGTH_SHORT).show();
        }
    }

    public void setSwipeRefreshLayout() {
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeRefreshLayout.setRefreshing(true);

                refreshGitHubRepo();

                if (!EasyPermissions.hasPermissions(requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    PermissionRequest request = new PermissionRequest.Builder(requireActivity(),
                            REQUEST_WRITE_ES, Manifest.permission.WRITE_EXTERNAL_STORAGE).build();
                    EasyPermissions.requestPermissions(request);
                } else {
//                    if (entityList != null && adapter != null) {
//                    Toast.makeText(context, entityList.size() + "", Toast.LENGTH_SHORT).show();
//                        entityList.clear();
//                        entityList.addAll(FileUtils.listFiles(rootPath));
                    setRecyclerView();
//                    }
                }
                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    private void refreshGitHubRepo() {
        if (selectRepo == null) {
            return;
        }
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
                        if (!EasyPermissions.hasPermissions(requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                            PermissionRequest request = new PermissionRequest.Builder(requireActivity(),
                                    REQUEST_WRITE_ES, Manifest.permission.WRITE_EXTERNAL_STORAGE).build();
                            EasyPermissions.requestPermissions(request);
                        } else {
                            setRecyclerView();
                        }
                        Logger.d("got contents size" + repoContents.length);
                    }

                    @Override
                    public void onError(Throwable e) {
                        Logger.e("get onError", e);
                        Constants.token = "";
                        SPUtils.putString(Constants.KEY_OAUTH2_TOKEN, Constants.token);
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
        switch (item.getItemId()) {
            case R.id.sort:
                AlertDialog.Builder sortDialog = new AlertDialog.Builder(context);
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
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void initSearchView(Menu menu) {
        MenuItem searchItem = menu.findItem(R.id.search);
        SearchManager searchManager = (SearchManager) context.getSystemService
                (Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(context.getComponentName()));
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
                        Toast.makeText(context, R.string.toast_message_sdcard_unavailable,
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
            setRecyclerView();
        }
    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        if (requestCode == REQUEST_WRITE_ES) {
            Toast.makeText(requireContext(), getString(R.string.no_write_sdcard_permission), Toast.LENGTH_SHORT).show();
        }
    }

}
