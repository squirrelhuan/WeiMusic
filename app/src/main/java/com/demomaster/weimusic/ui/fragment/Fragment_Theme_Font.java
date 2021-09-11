package com.demomaster.weimusic.ui.fragment;

import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
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


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.demomaster.weimusic.R;
import com.demomaster.weimusic.activity.ThemeActivity;
import com.demomaster.weimusic.constant.Constants;
import com.demomaster.weimusic.interfaces.onSelcetPictureResult;
import com.demomaster.weimusic.ui.adapter.GridAdapter_Theme_Font;
import com.demomaster.weimusic.util.ThemeUtil;
import com.demomaster.weimusic.view.CustomGridView;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.demomaster.huan.quickdeveloplibrary.util.QDFileUtil;
import cn.demomaster.qdrouter_library.base.fragment.QuickFragment;

import static com.demomaster.weimusic.constant.Constants.Action_Theme_Change;

/**
 * Created by huan on 2018/1/21.
 */

public class Fragment_Theme_Font extends QuickFragment implements onSelcetPictureResult {


    @BindView(R.id.grid_01)
    CustomGridView grid_01;
    @BindView(R.id.grid_02)
    CustomGridView grid_02;
    private GridAdapter_Theme_Font mAdapter;
    List<Integer> data = new ArrayList<>();

    @Override
    public boolean isUseActionBarLayout() {
        return false;
    }

    @Override
    public View onGenerateView(@NonNull @NotNull LayoutInflater inflater, @Nullable @org.jetbrains.annotations.Nullable ViewGroup container, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_theme_01, container, false);
    }

    @Override
    public void initView(View view) {
        ButterKnife.bind(this,view);
        int[] data_c = {R.style.StartAppTheme_Light,R.style.StartAppTheme_Night};
        data.clear();
        for (int i : data_c) {
            data.add(i);
        }
        mAdapter = new GridAdapter_Theme_Font(getActivity(), data, 0);
        grid_01.setAdapter(mAdapter);//recyclerview设置适配器
        grid_01.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                ThemeUtil.changeFont(data.get(i));
                mAdapter.notifyDataSetChanged();
                Intent intent2 = new Intent();
                intent2.setAction(Action_Theme_Change);
                getActivity().sendBroadcast(intent2);
                //mAdapter2.notifyDataSetChanged();
            }
        });

       /* mAdapter2 = new GridAdapter_Theme_Font(getActivity(), data2, 1);
        grid_02.setAdapter(mAdapter2);
        grid_02.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                mAdapter.notifyDataSetChanged();
                //mAdapter2.notifyDataSetChanged();
            }
        });*/

        TextView tv_add = (TextView) findViewById(R.id.tv_add);
        tv_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
    }

    public void loadData() {
        /*List<String> files = getThemeActivity().getFilesFromDir(Constants.APP_PATH_PICTURE_Font);
        if (files != null) {
            data2.clear();
            data2.addAll(files);
        }*/
    }

    public void refreshUI() {
        //mAdapter2.notifyDataSetChanged();
    }

    public ThemeActivity getThemeActivity() {
        return ((ThemeActivity) getActivity());
    }

    @Override
    public void onGetPicResult(Uri fileUri, String code) {
    }

}
