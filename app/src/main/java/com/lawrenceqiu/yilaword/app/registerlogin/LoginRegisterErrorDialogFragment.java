package com.lawrenceqiu.yilaword.app.registerlogin;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import com.lawrenceqiu.yilaword.app.R;

/**
 * Created by Lawrence on 6/12/2015.
 */
public class LoginRegisterErrorDialogFragment extends DialogFragment {

    /**
     * Display a DialogFragment which gets the title and the error message
     * -TODO: Consider removing this and placing individual alert dialogs in Login and Register Fragments
     *
     * @param savedInstanceState Bundle of data
     * @return Dialog to be displayed
     */
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Bundle bundle = getArguments();
        String errorMessage = bundle.getString("error");
        String title = bundle.getString("title");

        return new AlertDialog.Builder(getActivity())
                .setTitle(title)
                .setMessage(errorMessage)
                .setCancelable(false)
                .setPositiveButton(R.string.Ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).create();
    }
}
