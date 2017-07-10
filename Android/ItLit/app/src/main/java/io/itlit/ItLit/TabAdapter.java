package io.itlit.ItLit;

import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.Fragment;

public class TabAdapter extends FragmentStatePagerAdapter {
    public static final int TABS = 3;

    public TabAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public int getCount() {
        return TABS;
    }

    @Override
    public Fragment getItem(int position) {
        return TabFragment.newInstance(position);
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return "Friends";
            case 1:
                return "Light";
            case 2:
                return "Map";
            default:
                return "Tab";
        }
    }
}
