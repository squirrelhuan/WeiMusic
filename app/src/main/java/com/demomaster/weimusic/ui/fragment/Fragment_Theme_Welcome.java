package com.demomaster.weimusic.ui.fragment;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;


import com.demomaster.weimusic.constant.Constants;
import com.demomaster.weimusic.R;
import com.demomaster.weimusic.activity.ThemeActivity;
import com.demomaster.weimusic.interfaces.onSelcetPictureResult;
import com.demomaster.weimusic.ui.adapter.GridAdapter_Theme_Welcome;
import com.demomaster.weimusic.util.ImageUtils;
import com.demomaster.weimusic.util.ThemeUtil;
import com.demomaster.weimusic.view.CustomGridView;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import cn.demomaster.huan.quickdeveloplibrary.util.QDFileUtil;
import cn.demomaster.qdlogger_library.QDLogger;

/**
 * Created by huan on 2018/1/21.
 */

public class Fragment_Theme_Welcome extends BaseFragment implements onSelcetPictureResult {

    @Override
    public View setContentUI(LayoutInflater inflater, ViewGroup container) {
        return inflater.inflate(R.layout.fragment_theme_01, container, false);
    }

    @BindView(R.id.grid_01)
    CustomGridView grid_01;
    @BindView(R.id.grid_02)
    CustomGridView grid_02;
    private GridAdapter_Theme_Welcome mAdapter;
    private GridAdapter_Theme_Welcome mAdapter2;

    List<Integer> data = new ArrayList<>();
    List<String> data2 = new ArrayList<String>();


    @Override
    public void initView(View view) {
        int[] data_c = {R.drawable.bg_001};
        data.clear();
        for (int i : data_c) {
            data.add(i);
        }
        mAdapter = new GridAdapter_Theme_Welcome(getActivity(), data, 0);
        grid_01.setAdapter(mAdapter);//recyclerview设置适配器
        grid_01.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                ThemeUtil.changeWelcome(getActivity(), Constants.Theme_Welcome_Type_System, data.get(i));
                mAdapter.notifyDataSetChanged();
                mAdapter2.notifyDataSetChanged();
            }
        });

        mAdapter2 = new GridAdapter_Theme_Welcome(getActivity(), data2, 1);
        grid_02.setAdapter(mAdapter2);
        grid_02.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                ThemeUtil.changeWelcome(getActivity(), Constants.Theme_Welcome_Type_Custom, data2.get(i).toString());
                mAdapter.notifyDataSetChanged();
                mAdapter2.notifyDataSetChanged();
            }
        });

        TextView tv_add = findViewById(R.id.tv_add);
        tv_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SelectPhotoPopupWindow("welcome");
            }
        });
        loadData();
    }

    public void loadData() {
        List<String> files = getThemeActivity().getFilesFromDir(Constants.APP_PATH_PICTURE_WELCOME);
        if (files != null) {
            data2.clear();
            data2.addAll(files);
        }
    }

    public void refreshUI() {
        mAdapter2.notifyDataSetChanged();
    }

    public ThemeActivity getThemeActivity() {
        return ((ThemeActivity) getActivity());
    }

    // 选择图片dialog
    private PopupWindow pop = null;
    private LinearLayout ll_popup;
    // 选择照片的popupWindow
    private void SelectPhotoPopupWindow(final String operationType) {
        getThemeActivity().operationType = operationType;
        getThemeActivity().monSelcetPictureResult = this;
        pop = new PopupWindow(getThemeActivity());
        View view = getLayoutInflater().inflate(R.layout.item_popupwindows_ios,
                null);

        ll_popup = (LinearLayout) view.findViewById(R.id.ll_popup);
        pop.setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
        pop.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        pop.setBackgroundDrawable(new BitmapDrawable());
        pop.setFocusable(true);
        pop.setOutsideTouchable(true);
        pop.setContentView(view);

        RelativeLayout parent =view.findViewById(R.id.parent);
        Button btn_camera = view
                .findViewById(R.id.item_popupwindows_camera);
        Button btn_file = view
                .findViewById(R.id.item_popupwindows_Photo);
        Button btn_default =  view
                .findViewById(R.id.item_popupwindows_Default);
        Button btn_cancle = view
                .findViewById(R.id.item_popupwindows_cancel);
        parent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pop.dismiss();
                ll_popup.startAnimation(AnimationUtils.loadAnimation(
                        getThemeActivity(), R.anim.scaling_big));
            }
        });
        // 拍照
        btn_camera.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                File file = new File(Constants.APP_PATH_PICTURE_WELCOME,System.currentTimeMillis()+".jpg");
                QDFileUtil.createFile(file);
                getThemeActivity().takePicture(QDFileUtil.getUrifromFile(getContext(),file));
            }
        });
        // 文件选取
        btn_file.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // fileUri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE);
                // getPictureByGallery(SettingActivity.this);
                File file = new File(Constants.APP_PATH_PICTURE_WELCOME,System.currentTimeMillis()+".jpg");
                QDFileUtil.createFile(file);
                getThemeActivity().getImageFromPhoto(Uri.fromFile(file));
            }
        });
        // 取消
        btn_cancle.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                pop.dismiss();
            }
        });
        pop.showAtLocation(view, Gravity.BOTTOM, 0, 0);
        ll_popup.startAnimation(AnimationUtils.loadAnimation(getThemeActivity(),
                R.anim.bottom_up));
    }


    @Override
    public void onGetPicResult(Uri fileUri, String code) {
        /*if (fileUri != null) {
            Bitmap photo = ImageUtils.getBitmapFromUri(fileUri,
                    getThemeActivity());
            ImageUtils.savePhoto(photo,
                    Constants.APP_PATH_PICTURE_WELCOME, getThemeActivity().getRandomFileName());
        }*/
        List<String> files = getThemeActivity().getFilesFromDir(Constants.APP_PATH_PICTURE_WELCOME);
        if (files != null) {
            data2.clear();
            data2.addAll(files);
            mAdapter2.notifyDataSetChanged();
        }

        if(pop!=null) {
            pop.dismiss();
        }
    }
}
