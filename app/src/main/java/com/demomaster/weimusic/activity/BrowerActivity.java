package com.demomaster.weimusic.activity;

import android.os.Bundle;

import com.demomaster.weimusic.R;
import com.demomaster.weimusic.ui.fragment.WebViewFragment;

public class BrowerActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_brower);
    }
    private void jumpUrl(String url) {
        Bundle bundle = new Bundle();
        bundle.putString("URL", url);
        // webViewFragment.setArguments(bundle);
        // ((QDActivity)v.getContext()).getFragmentHelper().startFragment(webViewFragment);
        getFragmentHelper().build(mContext, WebViewFragment.class.getName())
                .putExtras(bundle)
                .putExtra("password", 666666)
                .putExtra("name", "小三")
                .navigation();
    }
}