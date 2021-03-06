package com.lawrenceqiu.yilaword.app.registerlogin;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.lawrenceqiu.yilaword.app.Constants;
import com.lawrenceqiu.yilaword.app.R;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import org.apache.http.Header;

/**
 * Created by Lawrence on 6/10/2015.
 */
public class LoginFragment extends Fragment {
    private EditText enterUserID;
    private EditText enterPassword;
    private Button login;
    private Button cancel;

    /**
     * Submit button listener to check when user wants to login
     * If there is an issue with login (no input, wrong password), displays a DialogFragment
     * detailing the error
     * Otherwise, changes the sharedPreference to true if sucessfully logged in.
     */
    private View.OnClickListener handleLoginListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            final String userID = enterUserID.getText().toString();
            String password = enterPassword.getText().toString();
            String error = "";
            if (userID.length() == 0 || userID.length() < 6 || userID.length() > 20
                    || password.length() == 0 || password.length() < 6 || password.length() > 22) {
                if (userID.length() == 0) {
                    error += "Please enter a userID\n";
                } else if (userID.length() < 6) {
                    error += "userId must be at least 6 characters\n";
                } else {
                    error += "userId can't be more than 20 characters\n";
                }
                if (password.length() == 0) {
                    error += "Please enter a password\n";
                } else if (password.length() < 6) {
                    error += "Password must be at least 6 characters\n";
                } else {
                    error += "Password can't be more than 22 characters\n";
                }
                AlertDialog dialog = new AlertDialog.Builder(getActivity())
                        .setTitle(getString(R.string.loginError))
                        .setMessage(error)
                        .setCancelable(false)
                        .setPositiveButton(R.string.Ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }).create();
                dialog.show();
                return;
            }
            AsyncHttpClient client = new AsyncHttpClient();
            client.get(String.format(Constants.LOGIN_URL, userID, password), new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                    JsonParser jsonParser = new JsonParser();
                    JsonElement element = jsonParser.parse(new String(responseBody));
                    JsonObject responseObject = element.getAsJsonObject();
                    if (responseObject.get("status").getAsBoolean()) {
                        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(
                                Constants.PREFERENCE_FILE, Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putBoolean(Constants.PREFERENCE_LOGIN, true);
                        editor.apply();
                        getActivity().finish();     //Exit the activity
                    } else {
                        String error = responseObject.get("error_msg").getAsString();
                        AlertDialog dialog = new AlertDialog.Builder(getActivity())
                                .setTitle(getString(R.string.loginError))
                                .setMessage(error)
                                .setCancelable(false)
                                .setPositiveButton(R.string.Ok, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                }).create();
                        dialog.show();
                    }
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                    error.printStackTrace();
                }
            });
        }
    };

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);
        login = (Button) view.findViewById(R.id.submitUserId);
        cancel = (Button) view.findViewById(R.id.cancelLogin);
        enterUserID = (EditText) view.findViewById(R.id.enterUserID);
        enterPassword = (EditText) view.findViewById(R.id.enterPassword);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        login.setOnClickListener(handleLoginListener);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().finish();
            }
        });
    }
}
