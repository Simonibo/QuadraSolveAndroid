package de.jamesbeans.quadrasolve;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;

/**
 * Created by Simon on 17.01.2017.
 * A dialog fragment which asks a yes/no-question
 */

public class YesNoDialogFragment extends DialogFragment {
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(getArguments().getString("question"))
                .setPositiveButton(getArguments().getString("positive_text"), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        MainActivity.lastQuestionID = getArguments().getInt("actionId");
                        //YES!!
                        MainActivity.lastAction = 1;
                        final MainActivity m = (MainActivity) getActivity();
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                m.evalYesNo();
                            }
                        }).start();
                    }
                })
                .setNegativeButton(getArguments().getString("negative_text"), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        MainActivity.lastQuestionID = getArguments().getInt("actionId");
                        //NO!
                        MainActivity.lastAction = 0;
                        final MainActivity m = (MainActivity) getActivity();
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                m.evalYesNo();
                            }
                        }).start();
                    }
                });
        return builder.create();
    }
}
