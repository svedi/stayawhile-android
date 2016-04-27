/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package se.kth.csc.stayawhile;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.support.wearable.view.FragmentGridPagerAdapter;
/**
 * Constructs fragments as requested by the GridViewPager. For each row a different background is
 * provided.
 * <p>
 * Always avoid loading resources from the main thread. In this sample, the background images are
 * loaded from an background task and then updated using {@link #notifyRowBackgroundChanged(int)}
 * and {@link #notifyPageBackgroundChanged(int, int)}.
 */
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
