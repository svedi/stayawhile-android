package se.kth.csc.stayawhile;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import se.kth.csc.stayawhile.api.Queue;
import se.kth.csc.stayawhile.api.Queuee;

public class KickStudentDialogFragment extends DialogFragment {
    public interface KickStudentDialogListener {
        public void onKickStudentDialogPositiveClick(KickStudentDialogFragment dialog, String ugKthid, int position);
    }

    KickStudentDialogListener mListener;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mListener = (KickStudentDialogListener) activity;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Bundle args = getArguments();
        final Queuee queuee = Queuee.fromJSON(args.getString("user"));
        final int position = args.getInt("position");
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Remove User from Queue")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("OK",  new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        mListener.onKickStudentDialogPositiveClick(KickStudentDialogFragment.this, queuee.getUgKthid(), position);
                    }
                });
        builder.setMessage("You are about to remove the following user from the queue:\n\n"
                          + queuee.getRealname()
                          + "\n\nThis operation can not be undone!");

        AlertDialog dialog = builder.create();
        return dialog;
    }

}