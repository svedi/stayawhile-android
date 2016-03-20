package se.kth.csc.stayawhile;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.widget.EditText;

public class BroadcastDialogFragment extends DialogFragment {

    private BroadcastListener mCallback;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final int type = getArguments().getInt("target");
        final EditText input = new EditText(getActivity());
        return new AlertDialog.Builder(getActivity())
                .setTitle("Send broadcast")
                .setView(input)
                .setPositiveButton("Send", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        mCallback.broadcast(input.getText().toString(), type);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                    }
                })
                .create();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mCallback = (BroadcastListener) activity;
    }

    public interface BroadcastListener {
        void broadcast(String message, int target);
    }
}
