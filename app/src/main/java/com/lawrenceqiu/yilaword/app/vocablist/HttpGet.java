package com.lawrenceqiu.yilaword.app.vocablist;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;
import com.lawrenceqiu.yilaword.app.Constants;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Lawrence on 6/23/2015.
 */
public class HttpGet extends AsyncTask<Void, Void, String> {
    private String userID;

    public HttpGet(String userID) {
        this.userID = userID;
    }

    /**
     * Connects to the server and gets a Json message back
     *
     * @param params Empty parameters
     * @return Json response from the server
     */
    @Override
    protected String doInBackground(Void... params) {
        HttpURLConnection connection;
        String json = null;
        try {
            URL url = new URL(String.format(Constants.WORD_URL, userID));
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.connect();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            json = bufferedReader.readLine();
            bufferedReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return json;
    }
}

