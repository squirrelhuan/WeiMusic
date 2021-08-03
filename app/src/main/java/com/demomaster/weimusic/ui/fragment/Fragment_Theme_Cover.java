package com.demomaster.weimusic.ui.fragment;

import android.content.Intent;
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
import android.widget.RelativeLayout;
import android.widget.TextView;


import com.demomaster.weimusic.constant.Constants;
import com.demomaster.weimusic.R;
import com.demomaster.weimusic.activity.ThemeActivity;
import com.demomaster.weimusic.constant.ThemeConstants;
import com.demomaster.weimusic.interfaces.onSelcetPictureResult;
import com.demomaster.weimusic.ui.adapter.GridAdapter_Theme_Cover;
import com.demomaster.weimusic.util.ImageUtils;
import com.demomaster.weimusic.util.ThemeUtil;
import com.demomaster.weimusic.view.CustomGridView;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import cn.demomaster.huan.quickdeveloplibrary.helper.QDSharedPreferences;
import cn.demomaster.huan.quickdeveloplibrary.helper.toast.PopToastUtil;
import cn.demomaster.huan.quickdeveloplibrary.util.QDFileUtil;
import cn.demomaster.huan.quickdeveloplibrary.widget.button.ToggleButton;
import cn.demomaster.huan.quickdeveloplibrary.widget.dialog.QDMulSheetDialog;
import cn.demomaster.huan.quickdeveloplibrary.widget.popup.QDPopup;
import cn.demomaster.qdlogger_library.QDLogger;

import static com.demomaster.weimusic.constant.Constants.Action_WallBackGround;
import static com.demomaster.weimusic.constant.Constants.Action_WellComeBackGround;

/**
 * Created by huan on 2018/1/21.
 */

public class Fragment_Theme_Cover extends BaseFragment implements onSelcetPictureResult {

    @BindView(R.id.grid_01)
    CustomGridView grid_01;
    @BindView(R.id.grid_02)
    CustomGridView grid_02;
    @BindView(R.id.tv_add)
    TextView tv_add;

    @BindView(R.id.tg_cover_with_music)
    ToggleButton tg_cover_with_music;
    GridAdapter_Theme_Cover mAdapter;
    GridAdapter_Theme_Cover mAdapter2;

    List<Integer> data = new ArrayList<>();
    List<String> data2 = new ArrayList<String>();

    @Override
    public View setContentUI(LayoutInflater inflater, ViewGroup container) {
        return inflater.inflate(R.layout.fragment_theme_cover, container, false);
    }

    @Override
    public void initView(View view) {
        int[] data_c = {R.color.red, R.color.orange, R.color.yellow, R.color.green, R.color.cyan, R.color.blue, R.color.purple, R.color.black};
        for (int i : data_c) {
            data.add(i);
        }
        mAdapter = new GridAdapter_Theme_Cover(getActivity(), data, ThemeConstants.CoverType.withSystem);
        grid_01.setAdapter(mAdapter);//recyclerview设置适配器
        grid_01.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                ThemeUtil.setCover(ThemeConstants.CoverType.withSystem, getActivity().getResources().getColor(data.get(i)));
                mAdapter.notifyDataSetChanged();
                mAdapter2.notifyDataSetChanged();
            }
        });

        mAdapter2 = new GridAdapter_Theme_Cover(getActivity(), data2, ThemeConstants.CoverType.customPicture);
        grid_02.setAdapter(mAdapter2);
        grid_02.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                ThemeUtil.setCover(ThemeConstants.CoverType.customPicture, data2.get(i));
                mAdapter.notifyDataSetChanged();
                mAdapter2.notifyDataSetChanged();
            }
        });

        tv_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SelectPhotoPopupWindow("cover");
            }
        });

        tg_cover_with_music.setChecked(ThemeUtil.getCoverType() == ThemeConstants.CoverType.withMusic);
        tg_cover_with_music.setOnToggleChanged(new ToggleButton.OnToggleChangeListener() {
            @Override
            public void onToggle(View view, boolean on) {
                ThemeUtil.setCover(ThemeConstants.CoverType.withMusic, on);
            }
        });

        loadData();
    }

    public void loadData() {
        List<String> files = getThemeActivity().getFilesFromDir(Constants.APP_PATH_PICTURE_CAVER);
        if (files != null) {
            data2.clear();
            data2.addAll(files);
        }
        refreshUI();
    }

    public void refreshUI() {
        mAdapter2.notifyDataSetChanged();
    }

    public ThemeActivity getThemeActivity() {
        return ((ThemeActivity) getActivity());
    }

    // 选择照片的popupWindow
    private void SelectPhotoPopupWindow(final String operationType) {
        String[] menus = {"拍照", "从相册中选取", "恢复默认壁纸"};
        new QDMulSheetDialog.MenuBuilder(getContext()).setData(menus).setOnDialogActionListener(new QDMulSheetDialog.OnDialogActionListener() {
            @Override
            public void onItemClick(QDMulSheetDialog dialog, int position, List<String> data) {
                File file = new File(Constants.APP_PATH_PICTURE_CAVER,System.currentTimeMillis()+".jpg");
                switch (position){
                    case 0://拍照
                        QDFileUtil.createFile(file);
                        getThemeActivity().takePicture(QDFileUtil.getUrifromFile(getContext(),file));
                        break;
                    case 1://从相册中选取
                       QDFileUtil.createFile(file);
                        getThemeActivity().getImageFromPhoto(Uri.fromFile(file));
                        break;
                    case 2:
                        switch (operationType) {
                            case "cover":
                                QDSharedPreferences.getInstance().putString("cover", "默认");
                                break;
                            case "WallPagerPath":
                                QDSharedPreferences.getInstance().putString("WallPagerPath", "默认");
                                break;
                            case "WelcomePagePath":
                                QDSharedPreferences.getInstance().putString("WelcomePagerPath", "默认");
                                break;
                            default:
                                break;
                        }
                        break;
                }
                dialog.dismiss();
            }
        }).create().show();

        getThemeActivity().operationType = operationType;
        getThemeActivity().monSelcetPictureResult = this;
    }

    @Override
    public void onGetPicResult(Uri fileUri, String code) {
        /*if (fileUri != null) {
            Bitmap photo = ImageUtils.getBitmapFromUri(fileUri,
                    getThemeActivity());
            ImageUtils.savePhoto(photo,
                    Constants.APP_PATH_PICTURE_CAVER, getThemeActivity().getRandomFileName());
        }*/
        List<String> files = getThemeActivity().getFilesFromDir(Constants.APP_PATH_PICTURE_CAVER);
        if (files != null) {
            data2.clear();
            data2.addAll(files);
            mAdapter2.notifyDataSetChanged();
        }
    }
}
