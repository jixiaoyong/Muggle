package io.github.jixiaoyong.muggle;

import android.os.Environment;

import org.jetbrains.annotations.NotNull;

import java.io.File;

public class Constants {
    public static final String MY_EMAIL = "jixiaoyong1995@gamil.com";
    public static final String MY_GITHUB = "http://github.com/jixiaoyong";
    public static final String MY_FEEDBACK_WEB = "https://muggleapp.tk/feedback.html";
    public static final String MY_WBE_SITE = "https://jixiaoyong.github.io/";
    public static final String PROJECT_PAGE_URL = "https://muggleapp.tk/";

    public static final String BUNDLE_KEY_FROM_FILE = "FROM_FILE";
    public static final String BUNDLE_KEY_SAVED = "SAVED";
    public static final String BUNDLE_KEY_FILE_NAME = "FILE_NAME";
    public static final String BUNDLE_KEY_FILE_PATH = "FILE_PATH";


    public static final String LOCAL_FILE_PATH = Environment.getExternalStorageDirectory().toString()
            + File.separator + "Muggle" + File.separator;

    public final static String GITHUB_BASE_URL = "https://github.com/";
    public final static String GITHUB_API_BASE_URL = "https://api.github.com/";

    public final static String OAUTH2_SCOPE = "user,repo,gist,notifications";

    // Fields from default config.
    public static final String MUGGLE_CLIENT_ID = BuildConfig.MUGGLE_CLIENT_ID;
    public static final String MUGGLE_CLIENT_SECRET = BuildConfig.MUGGLE_CLIENT_SECRET;
    public final static String OAUTH2_URL = GITHUB_BASE_URL + "login/oauth/authorize";

    public static String token = "";

    public static String KEY_SELECT_REPO_INFO = "SELECT_REPO_INFO";
    public static String KEY_USER_INFO = "USER_INFO";
    @NotNull
    public static final String KEY_OAUTH2_TOKEN = "OAUTH2_TOKEN";
}
