package io.github.jixiaoyong.muggle.fragment;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import com.flipboard.bottomsheet.commons.MenuSheetView;

import org.jetbrains.annotations.NotNull;

import io.github.jixiaoyong.muggle.BuildConfig;
import io.github.jixiaoyong.muggle.Constants;
import io.github.jixiaoyong.muggle.R;
import io.github.jixiaoyong.muggle.databinding.FragmentAboutBinding;
import io.github.jixiaoyong.muggle.fragment.base.DataBindingBaseFragment;
import io.github.jixiaoyong.muggle.viewmodel.MainActivityModel;

public class AboutFragment extends DataBindingBaseFragment<FragmentAboutBinding, MainActivityModel> {

    @Override
    public int getLayoutId() {
        return R.layout.fragment_about;
    }

    @Override
    protected void initView() {
        setToolbarTitle(getString(R.string.pref_title_about));
        super.initView();
        try {
            PackageInfo packageInfo = requireActivity().getPackageManager().getPackageInfo(
                    requireActivity().getPackageName(), 0);
            String versionName = packageInfo.versionName;
            int versionCode = packageInfo.versionCode;
            dataBinding.appVersionNumber.setText(versionName + " ( " + versionCode + " ) ");
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(getClass().getName(), e.getMessage());
            e.printStackTrace();
        }

        dataBinding.projectPageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openUri(Constants.PROJECT_PAGE_URL);
            }
        });

        dataBinding.contactBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMenuSheet(MenuSheetView.MenuType.GRID);
            }
        });
    }

    private void showMenuSheet(MenuSheetView.MenuType menuType) {
        MenuSheetView menuSheetView = new MenuSheetView(requireContext(), menuType, "Contact me via...",
                new MenuSheetView.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.email:
                                Intent intent = new Intent(Intent.ACTION_SENDTO);
                                intent.setData(Uri.parse("mailto:" + Constants.MY_EMAIL));
                                startActivity(intent);
                                break;
                            case R.id.github:
                                openUri(Constants.MY_GITHUB);
                                break;
                            case R.id.web:
                                String myWbeSite = Constants.MY_WBE_SITE;
                                myWbeSite += "?from=app&version=" + BuildConfig.VERSION_NAME
                                        + "&buildType=" + BuildConfig.BUILD_TYPE;
                                openUri(myWbeSite);
                                break;
                        }
                        if (dataBinding.bottomSheet.isSheetShowing()) {
                            dataBinding.bottomSheet.dismissSheet();
                        }
                        return true;
                    }
                });
        menuSheetView.inflateMenu(R.menu.about_bottomsheet_menu);
        dataBinding.bottomSheet.showWithSheetView(menuSheetView);
    }

    private void openUri(String uriString) {
        Uri uri = Uri.parse(uriString);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(intent);
    }

    @NotNull
    @Override
    protected Class getViewModelClass() {
        return MainActivityModel.class;
    }
}
