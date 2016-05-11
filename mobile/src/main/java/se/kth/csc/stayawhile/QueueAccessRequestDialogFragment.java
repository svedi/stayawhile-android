package se.kth.csc.stayawhile;

import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import se.kth.csc.stayawhile.api.Queue;

public class QueueAccessRequestDialogFragment extends DialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Bundle args = getArguments();
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Assistant access")
                .setNeutralButton("OK", null);

        try {
            JSONObject result = new JSONObject(args.getString("queue"));
            Queue queue = Queue.fromJSON(result);
            StringBuilder sb = new StringBuilder();
            sb.append("You do not have assistant access to this queue. To get access, " +
                    "you need to contact one of the teachers in the course:\n");
            JSONArray teachers = queue.getJSON().getJSONArray("teacher");
            for (int i = 0; i < teachers.length(); i++) {
                sb.append("- " + teachers.getJSONObject(i).getString("realname") + "\n");
            }
            builder.setMessage(sb.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        AlertDialog dialog = builder.create();
        return dialog;
    }

}
