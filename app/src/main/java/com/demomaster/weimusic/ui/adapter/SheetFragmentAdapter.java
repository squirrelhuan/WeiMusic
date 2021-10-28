package com.demomaster.weimusic.ui.adapter;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.demomaster.weimusic.WeiApplication;
import com.demomaster.weimusic.model.AudioSheet;
import com.demomaster.weimusic.player.service.MusicDataManager;
import com.demomaster.weimusic.ui.fragment.MainFragment1;
import com.demomaster.weimusic.ui.fragment.MainFragment2;
import com.demomaster.weimusic.ui.fragment.MainFragment3;
import com.demomaster.weimusic.ui.fragment.SheetFragment3;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class SheetFragmentAdapter extends FragmentPagerAdapter {
    List<AudioSheet> audioSheets = new ArrayList<>();
    public SheetFragmentAdapter(FragmentManager fm) {
        super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        audioSheets.addAll(MusicDataManager.getInstance(WeiApplication.getInstance()).getSongSheet());
    }

    @Override
    public Fragment getItem(int position) {
        Fragment fragment = new SheetFragment3();
        Bundle bundle = new Bundle();
        bundle.putSerializable("audioSheets",audioSheets.get(position));
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public int getCount() {
        return audioSheets.size();
    }

    public void destroy() {
    }
}
