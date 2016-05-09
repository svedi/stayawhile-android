package se.kth.csc.stayawhile;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.widget.EditText;

public class MessageDialogFragment extends DialogFragment {

    private MessageListener mCallback;
    private String title = "";

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final EditText input = new EditText(getActivity());
        return new AlertDialog.Builder(getActivity())
                .setTitle(title)
                .setView(input)
                .setPositiveButton("Send", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        mCallback.message(input.getText().toString(), getArguments());
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
        mCallback = (MessageListener) activity;
    }

    public void setTitle(String newTitle){
        title = newTitle;
    }

    public interface MessageListener {

        int BROADCAST_ALL = 0;
        int BROADCAST_FACULTY = 1;
        int PRIVATE_MESSAGE = 2;
        int COMMENT = 3;
        int COMPLETION = 4;

        void message(String message, Bundle arguments);
    }
}
