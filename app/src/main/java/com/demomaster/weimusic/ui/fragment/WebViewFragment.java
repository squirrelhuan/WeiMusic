package com.demomaster.weimusic.ui.fragment;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.demomaster.weimusic.R;
import com.demomaster.weimusic.player.service.MC;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.demomaster.huan.quickdeveloplibrary.annotation.ActivityPager;
import cn.demomaster.huan.quickdeveloplibrary.annotation.ResType;
import cn.demomaster.huan.quickdeveloplibrary.base.tool.actionbar.OptionsMenu;
import cn.demomaster.huan.quickdeveloplibrary.helper.PhotoHelper;
import cn.demomaster.huan.quickdeveloplibrary.helper.toast.QdToast;
import cn.demomaster.huan.quickdeveloplibrary.operatguid.GuiderView;
import cn.demomaster.huan.quickdeveloplibrary.util.ClipboardUtil;
import cn.demomaster.huan.quickdeveloplibrary.util.ScreenShotUitl;
import cn.demomaster.huan.quickdeveloplibrary.view.loading.StateView;
import cn.demomaster.huan.quickdeveloplibrary.view.webview.QDWebView;
import cn.demomaster.huan.quickdeveloplibrary.view.webview.QuickWebChromeClient;
import cn.demomaster.huan.quickdeveloplibrary.view.webview.QuickWebViewClient;
import cn.demomaster.huan.quickdeveloplibrary.widget.dialog.OnClickActionListener;
import cn.demomaster.huan.quickdeveloplibrary.widget.dialog.QDDialog;
import cn.demomaster.qdlogger_library.QDLogger;
import cn.demomaster.qdrouter_library.base.fragment.QuickFragment;
import cn.demomaster.qdrouter_library.view.ImageTextView;


/**
 * Squirrel桓
 * 2018/8/25
 */
@ActivityPager(name = "Web", preViewClass = StateView.class, resType = ResType.Custome)
public class WebViewFragment extends QuickFragment {

    @BindView(R.id.webView)
    QDWebView webView;
    @BindView(R.id.framelayout)
    FrameLayout framelayout;

    @BindView(R.id.et_input)
    EditText et_input;
    @BindView(R.id.btn_search)
    Button btn_search;

    @Override
    public View getHeaderlayout() {
        return inflateView(R.layout.activity_actionbar_webview);
    }

    @Nullable
    @Override
    public View onGenerateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //return super.onCreateView(inflater, container, savedInstanceState);
        View mView = inflater.inflate(R.layout.fragment_layout_webview, null);
        return mView;
    }
    String url;
    Map<String,String> resourceMap = new LinkedHashMap<>();
    QDWebView.WindowViewInflate windowViewInflate;
    QuickWebViewClient quickWebViewClient;
    QuickWebChromeClient.OnStateChangedListener onStateChangedListener;
    public void initView(View rootView) {
        ButterKnife.bind(this,rootView);
        initMenu();
        et_input.setText(url);
        btn_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!TextUtils.isEmpty(et_input.getText())) {
                    webView.loadUrl(et_input.getText().toString());
                }
            }
        });
        ImageTextView rightView = findViewById(R.id.it_actionbar_menu);
        if(rightView!=null){
            rightView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    webView.loadUrl(url);
                }
            });
        }
        Bundle bundle = getArguments();
        if (bundle!=null&&bundle.containsKey("URL")) {
            // android 5.0부터 API수준 21이상을 타겟킹하는 경우 아래를 추가해 주시길 바랍니다  android 5.0至API水平21以上的目标，请添加以下内容
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                webView.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
                CookieManager cookieManager = CookieManager.getInstance();
                cookieManager.setAcceptCookie(true);
                cookieManager.setAcceptThirdPartyCookies(webView, true);
            }
            windowViewInflate = new QDWebView.WindowViewInflate() {
                @Override
                public WebView onWindowOpen() {
                    QDWebView qdWebView = new QDWebView(mContext);
                    // android 5.0부터 API수준 21이상을 타겟킹하는 경우 아래를 추가해 주시길 바랍니다  android 5.0至API水平21以上的目标，请添加以下内容
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        qdWebView.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
                        CookieManager cookieManager = CookieManager.getInstance();
                        cookieManager.setAcceptCookie(true);
                        cookieManager.setAcceptThirdPartyCookies(qdWebView, true);
                    }
                    qdWebView.setOnWindowViewInflate(windowViewInflate);
                    qdWebView.setMyWebViewClient(quickWebViewClient);
                    qdWebView.setOnStateChangedListener(onStateChangedListener);
                    framelayout.addView(qdWebView,new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                    return qdWebView;
                }

                @Override
                public void onCloseWindow(WebView window) {
                    QDLogger.e("移除tab页 window:" + window);
                    framelayout.removeView(window);
                }
            };
            webView.setOnWindowViewInflate(windowViewInflate);
            quickWebViewClient =new QuickWebViewClient(webView){
                @Override
                public void onLoadResource(WebView view, String url) {
                    if(!TextUtils.isEmpty(url)) {
                        if(url.endsWith(".m4a")
                                ||url.endsWith(".mp3")
                                ||url.endsWith(".aac")){
                            QDLogger.e("onLoadResource1 url=" + url);
                            resourceMap.put(url,null);
                            showResourceDialog(url);
                            return;
                        }
                    }
                    super.onLoadResource(view, url);
                }
            };
            webView.setMyWebViewClient(quickWebViewClient);
            onStateChangedListener = new QuickWebChromeClient.OnStateChangedListener() {
                @Override
                public void onProgress(int progress) {

                }

                @Override
                public void onFinish() {

                }

                @Override
                public void onReceivedTitle(WebView view, String title) {
                    setTitle(title);
                }

            };
            webView.setOnStateChangedListener(onStateChangedListener);
            url = (String) bundle.get("URL");
            webView.loadUrl(url);
        }
    }
    private OptionsMenu optionsMenu;
    private OptionsMenu.Builder optionsMenubuilder;
    private void initMenu() {
            List<OptionsMenu.Menu> menus = new ArrayList<>();
            String[] menuNames = {"刷新", "浏览器标识(PC)", "浏览器标识(mobile)"};
            for (int i = 0; i < menuNames.length; i++) {
                OptionsMenu.Menu menu = new OptionsMenu.Menu();
                menu.setTitle(menuNames[i]);
                menu.setPosition(i);
                //menu.setIconId(R.mipmap.quickdevelop_ic_launcher);
                menu.setIconPadding(30);
                menu.setIconWidth(80);
                menus.add(menu);
            }
        optionsMenubuilder = new OptionsMenu.Builder(mContext);
        optionsMenubuilder.setMenus(menus)
                    .setAlpha(.6f)
                    .setUsePadding(true)
                    .setBackgroundColor(getResources().getColor(R.color.blueviolet))
                    .setBackgroundRadius(20)
                    .setTextColor(Color.WHITE)
                    .setTextSize(16)
                    .setPadding(0)
                    .setWithArrow(true)
                    .setArrowHeight(30)
                    .setArrowWidth(30)
                    .setGravity(GuiderView.Gravity.BOTTOM)
                    .setDividerColor(getResources().getColor(R.color.transparent))
                    .setAnchor(getActionBarTool().getRightView());
        optionsMenubuilder.setOnMenuItemClicked(new OptionsMenu.OnMenuItemClicked() {
                @Override
                public void onItemClick(int position, View view) {
                    optionsMenu.dismiss();
                    switch (position) {
                        case 0:
                            webView.reload();
                            break;
                        case 1:
                            String chrome_ua = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/92.0.4515.107 Safari/537.36";
                            webView.getSettings().setUserAgentString(chrome_ua);
                            webView.reload();
                            break;
                        case 2:
                            String chrome_ua2 = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/92.0.4515.107 Safari/537.36";
                            webView.getSettings().setUserAgentString(chrome_ua2);
                            webView.reload();
                            break;
                    }
                }
            });
        optionsMenu = optionsMenubuilder.creat();
        optionsMenu.setMenus(menus);
        optionsMenu.setAlpha(.6f);
        optionsMenu.setMargin(80);
        optionsMenu.setUsePadding(true);
        optionsMenu.setBackgroundRadiu(20);
        optionsMenu.setBackgroundColor(Color.GRAY);
        optionsMenu.setAnchor(getActionBarTool().getRightView());
        getActionBarTool().setRightOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                optionsMenu.show(v);
            }
        });
    }

    QDDialog qdDialog;
    private void showResourceDialog(String url) {
        if(qdDialog!=null&&qdDialog.isShowing()){
            qdDialog.dismiss();
        }
         QDDialog.Builder builder = new QDDialog.Builder(getContext())
                 .setTitle("检测到音频资源")
                 .setMessage(url)
                 .addAction("复制链接", new OnClickActionListener() {
                     @Override
                     public void onClick(Dialog dialog, View view, Object tag) {
                         ClipboardUtil.setClip(getContext(), url);
                         QdToast.show(getContext(), "copy success", 1000);
                     }
                 })
                 .addAction("播放并下载", new OnClickActionListener() {
                     @Override
                     public void onClick(Dialog dialog, View view, Object tag) {
                         dialog.dismiss();
                         MC.getInstance(getContext()).playOnline(url);
                     }
                 });
        qdDialog = builder.create();
        qdDialog.show();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {//默认只处理回退事件
            if (webView!=null&&webView.canGoBack()){
                webView.goBack();
                return true;
            }
            finish();
            return true;
            //当返回true时，表示已经完整地处理了这个事件，并不希望其他的回调方法再次进行处理，而当返回false时，表示并没有完全处理完该事件，更希望其他回调方法继续对其进行处理
        }
        return false;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        webView.destroy();
    }
}