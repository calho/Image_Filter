package com.example.calvin.lab2;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.widget.NumberPicker;

/**
 * Created by Calvi on 2018-01-31.
 */

public class SettingsDialog extends DialogFragment {

    private SharedPreferences pref;


    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setMessage("Select undo limit");

        final NumberPicker numberPicker = new NumberPicker(getActivity());
        numberPicker.setMaxValue(10);
        numberPicker.setMinValue(0);

        SharedPreferences pref = getActivity().getSharedPreferences("N",  0);
        final SharedPreferences.Editor editor = pref.edit();
        int N = pref.getInt("N", 5);

        numberPicker.setValue(N);

        builder.setView(numberPicker);

        builder.setPositiveButton("Set", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                int newN = numberPicker.getValue();
                editor.putInt("N", newN);
                editor.commit();
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });

        return builder.create();
    }
}
