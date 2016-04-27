package se.kth.csc.stayawhile;

import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.support.wearable.view.GridViewPager;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.Wearable;


public class MainActivity extends WearableActivity {

    private GoogleApiClient mGoogleApiClient;
    private WearMessageListener mMessageListener;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final GridViewPager pager = (GridViewPager) findViewById(R.id.pager);

        pager.setAdapter(new MainGridPagerAdapter(this, getFragmentManager()));

        initGoogleApi();
        Wearable.MessageApi.addListener(mGoogleApiClient, mMessageListener);
    }

    private void initGoogleApi(){
        mGoogleApiClient = new GoogleApiClient.Builder( this )
                .addApi( Wearable.API )
                .build();

        mGoogleApiClient.connect();
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        mGoogleApiClient.disconnect();
    }
}
