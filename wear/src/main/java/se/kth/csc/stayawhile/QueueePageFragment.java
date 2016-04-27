package se.kth.csc.stayawhile;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by viklu on 2016-04-22.
 */
public class QueueePageFragment extends Fragment {

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Bundle bundle = getArguments();
        View view = inflater.inflate(R.layout.queuee_page,container,false);

        TextView name = (TextView) view.findViewById(R.id.u_name);
        name.setText(bundle.getString("name"));

        TextView location = (TextView) view.findViewById(R.id.u_location);
        location.setText(bundle.getString("location"));

        TextView type = (TextView) view.findViewById(R.id.u_type);
        type.setText(bundle.getString("type"));

        TextView comment = (TextView) view.findViewById(R.id.u_comment);
        comment.setText(bundle.getString("comment"));

        return view;
    }
}
