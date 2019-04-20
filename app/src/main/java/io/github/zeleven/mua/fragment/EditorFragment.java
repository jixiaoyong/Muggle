package io.github.zeleven.mua.fragment;

import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;

import org.greenrobot.eventbus.EventBus;

import butterknife.BindView;
import io.github.zeleven.mua.Constants;
import io.github.zeleven.mua.EditorAction;
import io.github.zeleven.mua.R;
import io.github.zeleven.mua.task.SaveFileTask;
import io.github.zeleven.mua.utils.FileUtils;
import io.github.zeleven.mua.view.CustomViewPager;

public class EditorFragment extends BaseEditorFragment implements Backable {
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
}
