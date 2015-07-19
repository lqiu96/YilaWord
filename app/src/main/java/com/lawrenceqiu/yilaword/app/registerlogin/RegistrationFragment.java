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
import com.lawrenceqiu.yilaword.app.vocablist.MainActivity;
import com.lawrenceqiu.yilaword.app.vocablist.VocabWordDisplayFragment;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import org.apache.http.Header;

/**
 * Created by Lawrence on 6/10/2015.
 */
public class RegistrationFragment extends Fragment {
    private Button register;
    private Button cancel;

    private EditText enterFirstName;
    private EditText enterLastName;
    private EditText enterUserID;
    private EditText enterPassword;
    private EditText enterCellPhoneNumber;
    private EditText enterEmailAddress;

    private boolean isUniqueId;

    /**
     * Checks to make sure valid input has been put into the registration form
     * If not, it displays dialogFragment detailing the errors of the input
     * If successful, changes the SharedPreference to show that the user has no logged in
     */
    private View.OnClickListener handleRegisterListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String firstName = enterFirstName.getText().toString();
            String lastName = enterLastName.getText().toString();
            final String userId = enterUserID.getText().toString();
            String password = enterPassword.getText().toString();
            String cellPhoneNumber = enterCellPhoneNumber.getText().toString();
            String emailAddress = enterEmailAddress.getText().toString();
            String error = "Please enter your:\n";
            if (firstName.length() == 0 || lastName.length() == 0 || userId.length() == 0
                    || password.length() == 0 || cellPhoneNumber.length() == 0 || emailAddress.length() == 0) {
                if (firstName.length() == 0) {
                    error += "First name\n";
                }
                if (lastName.length() == 0) {
                    error += "Last name\n";
                }
                if (userId.length() == 0) {
                    error += "userID\n";
                }
                if (password.length() == 0) {
                    error += "Password\n";
                }
                if (cellPhoneNumber.length() == 0) {
                    error += "Phone Number\n";
                }
                if (emailAddress.length() == 0) {
                    error += "Email Address\n";
                }
                AlertDialog dialog = new AlertDialog.Builder(getActivity())
                        .setTitle(getString(R.string.registrationError))
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
            client.get(String.format(Constants.REGISTER_URL, "true", firstName, lastName, userId,
                    password, cellPhoneNumber, emailAddress),
                    new AsyncHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                            JsonParser jsonParser = new JsonParser();
                            JsonElement element = jsonParser.parse(new String(responseBody));
                            JsonObject responseObject = element.getAsJsonObject();
                            RegistrationFragment.this.isUniqueId = responseObject.get("status").getAsBoolean();
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                            error.printStackTrace();
                        }
                    });
            if (isUniqueId) {
                client.get(String.format(Constants.REGISTER_URL, "true", firstName, lastName, userId,
                                password, cellPhoneNumber, emailAddress),
                        new AsyncHttpResponseHandler() {
                            @Override
                            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                                SharedPreferences sharedPreferences = getActivity().getSharedPreferences(Constants.PREFERENCE_FILE,
                                        Context.MODE_PRIVATE);
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.putBoolean(Constants.PREFERENCE_REGISTER, true);
                                editor.putBoolean(Constants.PREFERENCE_LOGIN, true);
                                editor.apply();
                                cancel.performClick();
                            }

                            @Override
                            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                                error.printStackTrace();
                            }
                        });
            } else {
                AlertDialog dialog = new AlertDialog.Builder(getActivity())
                        .setTitle(getString(R.string.registrationError))
                        .setMessage(userId + " has already been taken by someone else")
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
    };

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_register, container, false);
        register = (Button) view.findViewById(R.id.registerButton);
        cancel = (Button) view.findViewById(R.id.cancelRegistration);
        enterFirstName = (EditText) view.findViewById(R.id.enterFirstName);
        enterLastName = (EditText) view.findViewById(R.id.enterLastName);
        enterUserID = (EditText) view.findViewById(R.id.enterUserID);
        enterPassword = (EditText) view.findViewById(R.id.enterPassword);
        enterCellPhoneNumber = (EditText) view.findViewById(R.id.enterCellPhoneNumber);
        enterEmailAddress = (EditText) view.findViewById(R.id.enterEmailAddress);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        register.setOnClickListener(handleRegisterListener);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().finish();
            }
        });
    }
}
