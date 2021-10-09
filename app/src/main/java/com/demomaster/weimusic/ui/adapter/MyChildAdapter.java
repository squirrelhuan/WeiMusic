package com.demomaster.weimusic.ui.adapter;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.demomaster.weimusic.ui.fragment.MainFragment1;
import com.demomaster.weimusic.ui.fragment.MainFragment2;
import com.demomaster.weimusic.ui.fragment.MainFragment3;

public class MyChildAdapter extends FragmentPagerAdapter {

    MainFragment1 mainFragment1;
    MainFragment2 mainFragment2;
    MainFragment3 mainFragment3;
    public MyChildAdapter(FragmentManager fm) {
        super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                if (mainFragment1 == null) {
                    mainFragment1 = new MainFragment1();
                }
                return mainFragment1;
            case 1:
                if (mainFragment2 == null) {
                    mainFragment2 = new MainFragment2();
                }
                return mainFragment2;
            case 2:
                if (mainFragment3 == null) {
                    mainFragment3 = new MainFragment3();
                }
                return mainFragment3;
        }
        return null;
    }

    @Override
    public int getCount() {
        return 3;
    }

    public void destroy() {
        mainFragment1= null;
        mainFragment2 = null;
        mainFragment3 = null;
    }
}
