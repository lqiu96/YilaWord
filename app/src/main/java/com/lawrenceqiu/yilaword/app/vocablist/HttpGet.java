package com.lawrenceqiu.yilaword.app.vocablist;

import android.os.AsyncTask;
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
    private String mUserID;

    public HttpGet(String mUserID) {
        this.mUserID = mUserID;
    }

    /**
     * Connects to the server and gets a Json message back
     *
     * @param params Empty parameters
     * @return Json response from the server
     */
    @Override
    protected String doInBackground(Void... params) {
        HttpURLConnection connection = null;
        String json = null;
        try {
            URL url = new URL(String.format(Constants.WORD_URL, mUserID));
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.connect();

            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            json = bufferedReader.readLine();
            bufferedReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
        return json;
    }
}

