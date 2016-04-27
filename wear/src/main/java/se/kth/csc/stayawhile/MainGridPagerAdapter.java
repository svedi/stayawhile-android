package se.kth.csc.stayawhile;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.os.Bundle;
import android.support.wearable.view.FragmentGridPagerAdapter;

import java.util.ArrayList;
import java.util.List;


public class MainGridPagerAdapter extends FragmentGridPagerAdapter {

    private List<Bundle> queuees = new ArrayList<>();

    public MainGridPagerAdapter(Context ctx, FragmentManager fm) {
        super(fm);
        //TODO: Remove hard coding
        Bundle b1 = new Bundle();
        b1.putString("uid", "u1dfkfdskfHUMBUG");
        b1.putString("name", "Alexander Viklund");
        b1.putString("location", "Grey 06");
        b1.putString("type", "Help");
        b1.putString("comment", "What am i doing?");
        queuees.add(b1);
        Bundle b2 = new Bundle();
        b2.putString("uid", "u1dfkfdskfHUMBUG");
        b2.putString("name", "Wille");
        b2.putString("location", "Grey 07");
        b2.putString("type", "Present");
        b2.putString("comment", "Long message is a long message");
        queuees.add(b2);
        setCurrentColumnForRow(0, 1);
        setCurrentColumnForRow(1,1);
    }

    @Override
    public Fragment getFragment(int row, int col) {
        Fragment fragment;
        if (col == 1) fragment = new AcceptActionFragment();
        else if (col == 2) fragment =  new KickActionFragment();
        else fragment = new QueueePageFragment();
        fragment.setArguments(queuees.get(row));
        return fragment;
    }

    @Override
    public int getRowCount() {
        return queuees.size();
    }

    @Override
    public int getColumnCount(int rowNum) {
        return 3;
    }
}
