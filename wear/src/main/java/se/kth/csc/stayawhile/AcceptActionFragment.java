package se.kth.csc.stayawhile;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

/**
 * Created by viklu on 2016-04-22.
 */
public class AcceptActionFragment extends Fragment {
    private String userid;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        userid = getArguments().getString("uid");
        View view = inflater.inflate(R.layout.accept_action_fragment,container,false);
        ImageView imageView = (ImageView) view.findViewById(R.id.accept);
        imageView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                MainActivity.mMessageListener.sendAttendUser( userid );
                return false; //TODO: Implement
            }
        });
        return view;
    }
}
