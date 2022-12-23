package com.example.fetchrewardschallenge;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import javax.net.ssl.HttpsURLConnection;

public class GrabJSON {

    protected ArrayList<HashMap> testData = new ArrayList<>();
    protected JSONArray jsonArray;

    protected String grabData(String jsonURL) {
        /*
        Standard way to connect to an address and read data such as the JSON data
        that we are using this method for. It will return a string.
         */

        HttpURLConnection connection = null;
        BufferedReader reader = null;

        try {
            URL url = new URL(jsonURL);
            connection = (HttpsURLConnection) url.openConnection();
            connection.connect();
            InputStream stream = connection.getInputStream();
            reader = new BufferedReader(new InputStreamReader(stream));

            StringBuffer buffer = new StringBuffer();
            String line = "";


            while ((line = reader.readLine()) != null) {

                buffer.append(line+"\n");
            }

            return buffer.toString();

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public ArrayList<HashMap> parseJSON(String data) throws JSONException {
        /*
        As name suggest, parses a String of raw JSON data into something that can be used when
        using JSONArray for filtering/sorting
        */

        jsonArray = new JSONArray(data);

        for (int i = 0; i < jsonArray.length(); i++) {

            JSONObject jsonObject = jsonArray.getJSONObject(i);

            String jo_id = jsonObject.getString("id");
            String jo_listId = jsonObject.getString("listId");
            String jo_name = jsonObject.getString("name");

            HashMap jsonHash = new HashMap<>();
            jsonHash.put("id", jo_id);
            jsonHash.put("listId", jo_listId);
            jsonHash.put("name", jo_name);

            testData.add(jsonHash);
        }
        /*
        The data being returned will be used to compare with our key map for filtered invalid names
         */
        return testData;
    }

}