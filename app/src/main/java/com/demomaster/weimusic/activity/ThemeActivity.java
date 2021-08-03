package com.demomaster.weimusic.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.text.TextUtils;
import android.util.Log;


import com.alibaba.fastjson.JSON;
import com.demomaster.weimusic.R;
import com.demomaster.weimusic.interfaces.onSelcetPictureResult;
import com.demomaster.weimusic.ui.adapter.FragmentAdapter;
import com.demomaster.weimusic.ui.adapter.ScrollingTabsAdapter;
import com.demomaster.weimusic.ui.fragment.Fragment_Theme_Cover;
import com.demomaster.weimusic.ui.fragment.Fragment_Theme_Font;
import com.demomaster.weimusic.ui.fragment.Fragment_Theme_WallPager;
import com.demomaster.weimusic.ui.fragment.Fragment_Theme_Welcome;
import com.demomaster.weimusic.util.ImageUtils;
import com.demomaster.weimusic.view.ScrollableTabView;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import cn.demomaster.huan.quickdeveloplibrary.helper.PhotoHelper;
import cn.demomaster.huan.quickdeveloplibrary.helper.simplepicture.model.Image;
import cn.demomaster.huan.quickdeveloplibrary.util.QDFileUtil;
import cn.demomaster.qdlogger_library.QDLogger;

public class ThemeActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_theme);
        setTitle("个性化");
        initView();
    }

    void initView() {
        initPager();
    }

    public void initPager() {
        // Initiate PagerAdapter
        // PagerAdapter mPagerAdapter = new PagerAdapter(getSupportFragmentManager());

      /*  Bundle bundle = new Bundle();
        bundle.putString(MIME_TYPE, Audio.Playlists.CONTENT_TYPE);
        bundle.putLong(BaseColumns._ID, PLAYLIST_RECENTLY_ADDED);
*/
        //Get tab visibility preferences
       /* SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        Set<String> defaults = new HashSet<String>(Arrays.asList(
        		getResources().getStringArray(R.array.tab_titles)
        	));
        Set<String> tabs_set = sp.getStringSet(TABS_ENABLED,defaults);
        //if its empty fill reset it to full defaults
        	//stops app from crashing when no tabs are shown
        if(tabs_set.size()==0){
        	tabs_set = defaults;
        }*/

        //Only show tabs that were set in preferences
        // Recently added tracks
        // if(tabs_set.contains(getResources().getString(R.string.tab_recent)))

        /*List<Fragment> fragments = new ArrayList<Fragment>();
        fragments.add(new Fragment_Theme_Welcome());
        fragments.add(new Fragment_Theme_WallPager());
        fragments.add(new Fragment_Theme_Cover());
        fragments.add(new Fragment_Theme_Font());*/
        /*FragmentAdapter mPagerAdapter = new FragmentAdapter(getSupportFragmentManager(),
                fragments);*/
        MyFragmentPagerAdapter mPagerAdapter = new MyFragmentPagerAdapter(getSupportFragmentManager());
        // Initiate ViewPager
        ViewPager mViewPager = (ViewPager) findViewById(R.id.viewPager);
        //mViewPager.setPageMargin(getResources().getInteger(R.integer.viewpager_margin_width));
        //mViewPager.setPageMarginDrawable(R.drawable.viewpager_margin);
        mViewPager.setOffscreenPageLimit(mPagerAdapter.getCount());
        mViewPager.setAdapter(mPagerAdapter);
        //mViewPager.setCurrentItem(0);

        // Tabs
        initScrollableTabs(mViewPager);

        // Theme chooser
        //ThemeUtils.initThemeChooser(this, mViewPager, "viewpager", THEME_ITEM_BACKGROUND);
        // ThemeUtils.setMarginDrawable(this, mViewPager, "viewpager_margin");
    }

    public static class MyFragmentPagerAdapter extends androidx.fragment.app.FragmentPagerAdapter{

        public MyFragmentPagerAdapter(@NonNull @NotNull FragmentManager fm) {
            super(fm);
        }

        @NonNull
        @NotNull
        @Override
        public Fragment getItem(int position) {
            switch (position){
                case 0:
                    return new Fragment_Theme_Welcome();
                case 1:
                    return new Fragment_Theme_WallPager();
                case 2:
                    return new Fragment_Theme_Cover();
                case 3:
                    return new Fragment_Theme_Font();
            }
            return null;
        }

        @Override
        public int getCount() {
            return 4;
        }
    }

    /**
     * Initiate the tabs
     */
    public void initScrollableTabs(ViewPager mViewPager) {
        ScrollableTabView mScrollingTabs = (ScrollableTabView) findViewById(R.id.scrollingTabs);
        ScrollingTabsAdapter mScrollingTabsAdapter = new ScrollingTabsAdapter(this);
        mScrollingTabs.setAdapter(mScrollingTabsAdapter);
        mScrollingTabs.setViewPager(mViewPager);
        //ThemeUtils.initThemeChooser(this, mScrollingTabs, "scrollable_tab_background",
        //        THEME_ITEM_BACKGROUND);
    }

    //private Uri fileUri;
    public static final int MEDIA_TYPE_IMAGE = 1;
    private static final String LOG_TAG = "CGQ";
    public String operationType = "WallPagerPath";
    public onSelcetPictureResult monSelcetPictureResult;

    /**
     * 调用系统拍照
     */
    public void takePicture(Uri uri) {
        getPhotoHelper().takePhotoCrop(uri, new PhotoHelper.OnTakePhotoResult() {
            @Override
            public void onSuccess(Intent data, String path) {
                Uri uri = data.getData();
                Log.d(LOG_TAG, "CROP_SMALL_PICTURE data=" +data+",uri="+uri+",getExtras="+data.getAction());
                if (data != null) {
                    if(data.getData()!=null){
                        monSelcetPictureResult.onGetPicResult(data.getData(), operationType);
                    }else {
                        monSelcetPictureResult.onGetPicResult(Uri.parse(data.getAction().toString()), operationType);
                    }
                }
            }

            @Override
            public void onFailure(String error) {

            }
        });
    }

    private void cutImage(Uri srcUri,Uri outUri) {
        PhotoHelper.Builder builder = new PhotoHelper.Builder(mContext);
        builder.setOutUri(outUri);
        builder.setSrcUri(srcUri);
        builder.setOnTakePhotoResult(new PhotoHelper.OnTakePhotoResult() {
            @Override
            public void onSuccess(Intent data, String path) {
                Uri uri = data.getData();
                Log.d(LOG_TAG, "CROP_SMALL_PICTURE data=" +data+",uri="+uri+",getExtras="+data.getAction());
                if (data != null) {
                    if(data.getData()!=null){
                        monSelcetPictureResult.onGetPicResult(data.getData(), operationType);
                    }else {
                        monSelcetPictureResult.onGetPicResult(Uri.parse(data.getAction().toString()), operationType);
                    }
                }
            }

            @Override
            public void onFailure(String error) {

            }
        });
        switch (operationType) {
            case "WallPagerPath":
               // ImageUtils.startPhotoZoom(ThemeActivity.this, fileUri,
               //         CROP_SMALL_PICTURE);
                break;
            case "WelcomePagePath":
               // ImageUtils.startPhotoZoomScale(ThemeActivity.this, fileUri,
               //         CROP_SMALL_PICTURE, 1, 1.78f);
               // builder.setAspectX(1);
                //builder.setAspectY(1.78f);
                break;
            default:
                break;
        }
        getPhotoHelper().photoCrop(builder);
    }

    /**
     * 图库选择图片并保存
     * @param uri
     */
    public void getImageFromPhoto(Uri uri) {
        getPhotoHelper().selectPhotoFromGalleryAndCrop(uri,new PhotoHelper.OnTakePhotoResult() {
            @Override
            public void onSuccess(Intent data, String path) {
                //fileUri = data.getData();
                /*fileUri = QDFileUtil.getUrifromFile(mContext,new File(path));*/
                //cutImage(data.getData(),uri);
                Uri uri = data.getData();
                Log.d(LOG_TAG, "CROP_SMALL_PICTURE data=" +data+",uri="+uri+",getExtras="+data.getAction());
                if (data != null) {
                    if(data.getData()!=null){
                        monSelcetPictureResult.onGetPicResult(data.getData(), operationType);
                    }else {
                        monSelcetPictureResult.onGetPicResult(Uri.parse(data.getAction().toString()), operationType);
                    }
                }
            }

            @Override
            public void onFailure(String error) {

            }
        });
        /*Intent intent = new Intent(Intent.ACTION_PICK, null);
        intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                "image/*");
        ((Activity) context).startActivityForResult(intent, REQUE_CODE_PHOTO);*/
    }

    /**
     * Create a File for saving an image or video
     */
    private static File getOutputMediaFile(int type) {
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.
        File mediaStorageDir = null;
        try {
            // This location works best if you want the created images to be
            // shared
            // between applications and persist after your app has been
            // uninstalled.
            mediaStorageDir = new File(
                    Environment
                            .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                    "MyCameraApp");
            Log.d(LOG_TAG, "Successfully created mediaStorageDir: "
                    + mediaStorageDir);
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(LOG_TAG, "Error in Creating mediaStorageDir: "
                    + mediaStorageDir);
        }
        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                // 在SD卡上创建文件夹需要权限：
                QDLogger.d(LOG_TAG, "权限获取失败");
                return null;
            }
        }
        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss")
                .format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator
                    + "IMG_" + timeStamp + ".jpg");
        } else {
            return null;
        }
        return mediaFile;
    }

    public List<String> getFilesFromDir(String filePath) {
        //QDLogger.e("getFilesFromDir=" + filePath);
        List<String> filespath = new ArrayList<>();
        File f = new File(filePath);
        if (f.exists()) {
            try {
                File[] files = f.listFiles();// 列出所有文件
                for (File file : files) {
                    if(file.length()>0) {
                        filespath.add(file.getPath());
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return filespath;
    }

}
