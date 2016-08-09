package com.thespeakers_studio.thespeakersstudioapp.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.ViewGroup;
import android.view.Window;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.thespeakers_studio.thespeakersstudioapp.R;
import com.thespeakers_studio.thespeakersstudioapp.utils.AnalyticsHelper;

import static com.thespeakers_studio.thespeakersstudioapp.utils.LogUtils.LOGE;
import static com.thespeakers_studio.thespeakersstudioapp.utils.LogUtils.makeLogTag;

/**
 * Created by smcgi_000 on 8/8/2016.
 */
public class CuriousActivity extends BaseActivity {

    private static final String TAG = makeLogTag(CuriousActivity.class);
    private static final String SCREEN_LABEL = "Curious";

    private WebView mWebView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_PROGRESS);

        setContentView(R.layout.activity_curious);

        AnalyticsHelper.sendScreenView(SCREEN_LABEL);

        mWebView = (WebView) findViewById(R.id.webview);
        final Activity activity = this;

        String html = "<p>" + getResources().getString(R.string.curious_copy) + "</p>" +
                "<iframe src=\"https://app.acuityscheduling.com/schedule.php?owner=11767399&appointmentType=1019444\" width=\"100%\" height=\"800\" frameBorder=\"0\"></iframe>\n" +
                "<script src=\"https://d3gxy7nm8y4yjr.cloudfront.net/js/embed.js\" type=\"text/javascript\"></script>";

        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.setWebChromeClient(new WebChromeClient() {
            public void onProgressChanged(WebView view, int progress) {
                activity.setProgress(progress * 1000);
            }
        });

        mWebView.loadData(html, "text/html", null);
    }

    @Override
    protected int getSelfNavDrawerItem() {
        return NAVDRAWER_ITEM_CURIOUS;
    }

    @Override
    protected void setLayoutPadding(int actionBarSize) {
        ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams)
                mWebView.getLayoutParams();
        lp.topMargin = actionBarSize;
        mWebView.setLayoutParams(lp);
    }
}
