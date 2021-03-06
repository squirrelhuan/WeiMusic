package com.demomaster.weimusic.ui.fragment;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.demomaster.weimusic.R;
import com.demomaster.weimusic.SystemSetting;
import com.demomaster.weimusic.activity.ThemeActivity;
import com.demomaster.weimusic.constant.Constants;
import com.demomaster.weimusic.constant.ThemeConstants;
import com.demomaster.weimusic.interfaces.onSelcetPictureResult;
import com.demomaster.weimusic.ui.adapter.GridAdapter_Theme_WallPager;
import com.demomaster.weimusic.util.ThemeUtil;
import com.demomaster.weimusic.view.CustomGridView;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.demomaster.huan.quickdeveloplibrary.helper.QDSharedPreferences;
import cn.demomaster.huan.quickdeveloplibrary.util.QDFileUtil;
import cn.demomaster.huan.quickdeveloplibrary.widget.button.ToggleButton;
import cn.demomaster.huan.quickdeveloplibrary.widget.dialog.QDMulSheetDialog;
import cn.demomaster.qdlogger_library.QDLogger;
import cn.demomaster.qdrouter_library.base.fragment.QuickFragment;


/**
 * Created by huan on 2018/1/21.
 */

public class Fragment_Theme_WallPager extends QuickFragment implements onSelcetPictureResult {

    @BindView(R.id.grid_01)
    CustomGridView grid_01;
    @BindView(R.id.grid_02)
    CustomGridView grid_02;
    @BindView(R.id.tg_cover_with_music)
    ToggleButton tg_cover_with_music;
    private GridAdapter_Theme_WallPager mAdapter;
    private GridAdapter_Theme_WallPager mAdapter2;

    List<Integer> data = new ArrayList<>();
    List<String> data2 = new ArrayList<String>();

    @Override
    public boolean isUseActionBarLayout() {
        return false;
    }

    @Override
    public View onGenerateView(@NonNull @NotNull LayoutInflater inflater, @Nullable @org.jetbrains.annotations.Nullable ViewGroup container, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_theme_wall, container, false);
    }

    @Override
    public void initView(View view) {
        ButterKnife.bind(this,view);
        int[] data_c = {R.drawable.background_wall_001,R.drawable.background_wall_002,R.drawable.background_wall_003,R.drawable.background_wall_004};
        data.clear();
        for (int i : data_c) {
            data.add(i);
        }
        mAdapter = new GridAdapter_Theme_WallPager(getActivity(), data, ThemeConstants.WallPagerType.withSystem);
        grid_01.setAdapter(mAdapter);//recyclerview???????????????
        grid_01.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                ThemeUtil.changeWallPaper(ThemeConstants.WallPagerType.withSystem, data.get(i));
                mAdapter.notifyDataSetChanged();
                mAdapter2.notifyDataSetChanged();
                tg_cover_with_music.setChecked(false);
            }
        });
        mAdapter2 = new GridAdapter_Theme_WallPager(getActivity(), data2, ThemeConstants.WallPagerType.customPicture);
        grid_02.setAdapter(mAdapter2);
        grid_02.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                ThemeUtil.changeWallPaper(ThemeConstants.WallPagerType.customPicture, data2.get(i).toString());
                mAdapter.notifyDataSetChanged();
                mAdapter2.notifyDataSetChanged();
                tg_cover_with_music.setChecked(false);
            }
        });
        TextView tv_add = findViewById(R.id.tv_add);
        tv_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SelectPhotoPopupWindow("WallPager");
            }
        });

        int wallPagerType = QDSharedPreferences.getInstance().getInt(Constants.Theme_WallPager_Type, 0);
        tg_cover_with_music.setChecked(wallPagerType== ThemeConstants.WallPagerType.withMusic.value());
        tg_cover_with_music.setOnToggleChanged(new ToggleButton.OnToggleChangeListener() {
            @Override
            public void onToggle(View view, boolean on) {
                ThemeUtil.changeWallPaper(ThemeConstants.WallPagerType.withMusic,on);
                mAdapter.notifyDataSetChanged();
                mAdapter2.notifyDataSetChanged();
            }
        });


        loadData();
    }

    public void loadData() {
        List<String> files = getThemeActivity().getFilesFromDir(Constants.APP_PATH_PICTURE_WALLPAGER);
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

    // ???????????????popupWindow
    private void SelectPhotoPopupWindow(final String operationType) {
        getThemeActivity().operationType = operationType;
        getThemeActivity().monSelcetPictureResult = this;
        String[] menus = {"??????", "??????????????????", "??????????????????"};
        new QDMulSheetDialog.MenuBuilder(getContext()).setData(menus).setOnDialogActionListener(new QDMulSheetDialog.OnDialogActionListener() {
            @Override
            public void onItemClick(QDMulSheetDialog dialog, int position, List<String> data) {
                File file = new File(Constants.APP_PATH_PICTURE_WALLPAGER,System.currentTimeMillis()+".jpg");
                switch (position){
                    case 0://??????
                        QDFileUtil.createFile(file);
                        getThemeActivity().takePicture(QDFileUtil.getUrifromFile(getContext(),mContext.getPackageName()+".fileprovider",file));
                        break;
                    case 1://??????????????????
                        QDFileUtil.createFile(file);
                        getThemeActivity().getImageFromPhoto(Uri.fromFile(file));
                        break;
                    case 2:
                        switch (operationType) {
                            case "cover":
                                QDSharedPreferences.getInstance().putString("cover", "??????");
                                break;
                            case "WallPagerPath":
                                QDSharedPreferences.getInstance().putString("WallPagerPath", "??????");
                                break;
                            case "WelcomePagePath":
                                QDSharedPreferences.getInstance().putString("WelcomePagerPath", "??????");
                                break;
                            default:
                                break;
                        }
                        break;
                }
                dialog.dismiss();
            }
        }).create().show();
    }

    @Override
    public void onGetPicResult(Uri fileUri, String code) {
        /*if (fileUri != null) {
            Bitmap photo = ImageUtils.getBitmapFromUri(fileUri,
                    getThemeActivity());
            ImageUtils.savePhoto(photo,
                    Constants.APP_PATH_PICTURE_WALLPAGER, getThemeActivity().getRandomFileName());
        }*/
        QDLogger.e("????????????1???"+QDFileUtil.getRealPathFromURI(getThemeActivity().mContext, fileUri));
        List<String> files = getThemeActivity().getFilesFromDir(Constants.APP_PATH_PICTURE_WALLPAGER);
        if (files != null) {
            data2.clear();
            data2.addAll(files);
            mAdapter2.notifyDataSetChanged();
        }
    }

}
