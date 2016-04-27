package se.kth.csc.stayawhile;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.support.wearable.view.FragmentGridPagerAdapter;


public class MainGridPagerAdapter extends FragmentGridPagerAdapter {

    public MainGridPagerAdapter(Context ctx, FragmentManager fm) {
        super(fm);
        setCurrentColumnForRow(0,1);
        setCurrentColumnForRow(1,1);
    }

    @Override
    public Fragment getFragment(int row, int col) {
        if (col == 2) return new KickActionFragment();
        if (col == 1) return new AcceptActionFragment();

        return new QueueePageFragment();
    }

    @Override
    public int getRowCount() {
        return 2;
    }

    @Override
    public int getColumnCount(int rowNum) {
        return 3;
    }
}
