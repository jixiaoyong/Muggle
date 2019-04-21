package io.github.jixiaoyong.muggle.fragment;

import android.Manifest;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import butterknife.BindView;
import io.github.jixiaoyong.muggle.Constants;
import io.github.jixiaoyong.muggle.EditorAction;
import io.github.jixiaoyong.muggle.R;
import io.github.jixiaoyong.muggle.fragment.base.BaseEditorFragment;
import io.github.jixiaoyong.muggle.task.SaveFileTask;
import io.github.jixiaoyong.muggle.utils.FileUtils;
import io.github.jixiaoyong.muggle.view.CustomViewPager;
import pub.devrel.easypermissions.EasyPermissions;
import pub.devrel.easypermissions.PermissionRequest;

public class EditorFragment extends BaseEditorFragment implements BackHolder
        , EasyPermissions.PermissionCallbacks {
    private static final int REQUEST_WRITE_ES = 1;


    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.editor_viewpager)
    ViewPager editorViewPager;

    @Override
    public int getLayoutId() {
        return R.layout.fragment_editor;
    }

    @Override
    public void initView() {
        getArgs();
        super.initView();
        toolbar.setTitle("");
        context.setSupportActionBar(toolbar);
        context.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setHasOptionsMenu(true);
        setViewPager();
        setViewPagerListener();
    }

    public void getArgs() {
        Bundle args = getArguments();
        if (args != null) {
            fromFile = args.getBoolean(Constants.BUNDLE_KEY_FROM_FILE);
            if (fromFile) {
                isFileSaved = args.getBoolean(Constants.BUNDLE_KEY_SAVED);
                fileName = args.getString(Constants.BUNDLE_KEY_FILE_NAME);
                filePath = args.getString(Constants.BUNDLE_KEY_FILE_PATH);
                if (filePath != null) {
                    fileContent = FileUtils.readContentFromPath(filePath, true);
                }
            }
        }
    }

    public void setViewPager() {
        final ScreenSlidePagerAdapter adapter = new ScreenSlidePagerAdapter(
                getChildFragmentManager());
        editorViewPager.setAdapter(adapter);
        ((CustomViewPager) editorViewPager).setSwipeable(true);
    }

    public void setViewPagerListener() {
        editorViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                if (position == 1) {
                    EventBus.getDefault().post(true);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
    }

    @Override
    public boolean onBackPressed() {
        Log.e("TAG", " onBackPressed ");

        if (!EasyPermissions.hasPermissions(requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            PermissionRequest request = new PermissionRequest.Builder(this,
                    REQUEST_WRITE_ES, Manifest.permission.WRITE_EXTERNAL_STORAGE).build();
            EasyPermissions.requestPermissions(request);
            return true;
        }

        if (!isFileSaved) {
            showSaveFileDialog(false);
            return true;
        } else if (isContentChanged) {
            showSaveContentDialog();
            return true;
        }

        return false;
    }

    private void showSaveContentDialog() {
        AlertDialog.Builder saveDialog = new AlertDialog.Builder(context);
        saveDialog.setTitle(R.string.dialog_title_save_file);
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
                        filePath = rootPath + fileName + ".md";
                        new SaveFileTask(context, filePath, fileName,
                                currentContent, true, new SaveFileTask.Response() {
                            @Override
                            public void taskFinish(Boolean result) {
                                isContentChanged = !result; // change isFileSaved value to true if save success
                                requireActivity().onBackPressed();
                            }
                        }).execute();
                    }
                });

        saveDialog.show();
    }


    private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {
        public ScreenSlidePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            Fragment fragment;
            if (position == 0) {
                fragment = new EditFragment();
            } else {
                fragment = new PreviewFragment();
            }
            return fragment;
        }

        @Override
        public int getCount() {
            return 2;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        EditorAction editorAction = new EditorAction(context);
        switch (item.getItemId()) {
            case R.id.preview:
                // switch to preview page
                editorAction.toggleKeyboard(0);
                editorViewPager.setCurrentItem(1, true);
                break;
            case R.id.edit:
                // switch to edit page
                editorViewPager.setCurrentItem(0, true);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
        if (requestCode == REQUEST_WRITE_ES) {
            showSaveFileDialog(false);
        }
    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        if (requestCode == REQUEST_WRITE_ES) {
            Toast.makeText(requireContext(), getString(R.string.no_write_sdcard_permission), Toast.LENGTH_SHORT).show();
        }
    }

}
