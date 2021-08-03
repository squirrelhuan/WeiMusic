package com.demomaster.weimusic.ui.fragment;

import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.os.Bundle;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import butterknife.ButterKnife;

/**
 * Created by huan on 2018/1/27.
 */

public abstract class BaseFragment extends Fragment {

    public View rootView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (rootView == null) {
            rootView = setContentUI(inflater, container);
        } else {
            if (rootView.getParent() != null) {
                ((ViewGroup) rootView.getParent()).removeView(rootView);
            }
        }
        ButterKnife.bind(this, rootView);
        initView(rootView);
        return rootView;
    }

    public <T>T findViewById(int id) {
        return (T) rootView.findViewById(id);
    }

    /**
     * TODO 加载视图
     */
    public abstract View setContentUI(LayoutInflater inflater, ViewGroup container);
    public abstract void initView(View view);

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(rootView!=null){
            rootView =null;
        }
    }
}
