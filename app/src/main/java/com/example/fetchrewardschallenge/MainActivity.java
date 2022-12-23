package com.example.fetchrewardschallenge;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    /* Default URL that the JSON is located for fetching */
    final static String URL_JSON = "https://fetch-hiring.s3.amazonaws.com/hiring.json";

    /*
    I declare most variables here to keep track of data easier, and then initialize
    where needed.
     */
    GrabJSON grabJSON = new GrabJSON();
    JSONArray rawArray;
    JSONArray filterArray;

    String rawData;
    String rawSortAndFilter;

    ArrayList sortList;
    ArrayList<HashMap> filterList;

    TextView textViewData;

    Button buttonLoad;
    Button buttonSortAndFilter;
    Button buttonPretty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /*
        Loading buttons and our TextView with correct ids that we declared in our
        activity_main xml.
         */
        textViewData = findViewById(R.id.textView_JSONData);
        textViewData.setMovementMethod(new ScrollingMovementMethod());

        buttonLoad = findViewById(R.id.button_Load);
        buttonSortAndFilter = findViewById(R.id.button_SortAndFilter);
        buttonPretty = findViewById(R.id.button_List);

        /*
        For anything related to grabbing data outside our app, we need to create a
        background thread, as explained below.
         */
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {

                try {

                    rawData = grabJSON.grabData(URL_JSON);
                    /*
                    We grab the JSON in a a separate thread (from the main UI) to prevent
                    tying up the system. I've put this method in a separate class, GrabJSON.java,
                    as it will contain code dealing solely with retrieving and parsing the data.
                     */

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        thread.start();

    }

    public void loadClick(View view) throws JSONException {

        buttonLoad.setClickable(false);
        buttonLoad.setVisibility(View.INVISIBLE);

        textViewData.setText(rawData);
        /*
        We had our GrabJSON class grab the JSON data while the application starts in a separate
        thread so we may avoid lockups (if connection was cut off during transmission, for example).
        Since rawData contains our data, we display it in our TextView to verification.
         */
        buttonSortAndFilter.setVisibility(View.VISIBLE);
    }

    public void sortAndFilterClick(View view) throws JSONException {

        buttonSortAndFilter.setClickable(false);
        buttonSortAndFilter.setVisibility(View.INVISIBLE);

        rawArray = new JSONArray(rawData);
        filterArray = new JSONArray();
        sortList = new ArrayList();
        /*
        Although we have our JSON string (as rawData), it is useless in its current state when
        we need to filter and sort every item. Luckily we can put our rawData as a parameter
        for a new JSONArray to make it more manipulable as we try parsing it with getJSONObject
        as we can see below.
         */

        for (int i = 0; i < rawArray.length(); i++) {

            sortList.add(rawArray.getJSONObject(i));
        }
        Collections.sort(sortList, new Comparator<JSONObject>() {
            private static final String KEY_NAME = "listId";
            private static final String SUBKEY_NAME = "name";
            /*
            We need a subkey to compare since if ListId is equal (which will happen a lot), then
            we can compare by the name when sorting, as per the requirements. AAlthough it would
            be easier sorting by the JSON id, the requirements asked to be sorted by
            listID and then name, if necessary.
             */

            @Override
            public int compare(JSONObject lhs, JSONObject rhs) {

                String lid = null;
                String rid = null;

                try {
                    lid = lhs.getString(KEY_NAME);
                    rid = rhs.getString(KEY_NAME);

                    if (lid.equals(rid)) {
                        /*
                        If key_name, which is our first item that is required to be sorted
                        (listID) is the same, then we sort by its subkey_name which is a string
                        that contains chars of letters and numbers. To properly sort that,
                        we need to replace every letter character with nothing, and then compare
                        the extracted Integer number from that same string, which we do so
                        in our custom compare method and extractInt method.
                         */

                        try {
                            lid = lhs.getString(SUBKEY_NAME);
                            rid = rhs.getString(SUBKEY_NAME);

                            return extractInt(lid) - extractInt(rid);

                        } catch (NumberFormatException e) {
                            System.out.println(e);
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                return lid.compareTo(rid);
            }

            int extractInt(String s) {
                /*
                num contains only numbers in a String. We can then parse and compare it
                as seen below. If we do not have a number, we set it as 0 and then compare.
                 */

                String num = s.replaceAll("\\D", "");
                return num.isEmpty() ? 0 : Integer.parseInt(num);
            }
        });

        /*
        After our sortList was sorted, we put it into an arraylist of hashmap for faster
        checking when we compare our map key (which key[1] equals our JSON 'name' item) to see
        if it contains a null item, or a blank item, as per requirements.

        If it does not contain any invalid 'name', then we can add it directly to our filterList,
        which contains our sorted and now filtered items, ready for display.
         */
        ArrayList<HashMap> sortHash = grabJSON.parseJSON(sortList.toString());
        filterList = new ArrayList<>();

        for (int i = 0; i < sortHash.size(); i++) {

            Map map = sortHash.get(i);
            Object nameKey = "name";    // key 'name' is what we are filtering the invalid items

            if (!map.get(nameKey).equals(null) && !map.get(nameKey).equals("") && !map.get(nameKey).equals("null")) {
                /*
                Checking to see if in the 'name' field, if we have invalid items, if not, then we
                can add them to our filterList which contains our final list for display.
                */
                filterList.add(sortHash.get(i));
            } else {
                // else do not add it, we do not need invalid name items in our list
            }


        }
        Log.d("DEBUG", "total valid items is at " + filterList.size());

        StringBuffer buffer = new StringBuffer();

        for (int i = 0; i < filterList.size(); i++) {

            buffer.append(filterList.get(i));

            if (i != filterList.size() - 1) {

                buffer.append("\n");
            }
        }
        // Just spacing the list to view it a bit better
        rawSortAndFilter = buffer.toString();
        textViewData.setText(rawSortAndFilter);

        buttonPretty.setVisibility(View.VISIBLE);
    }

    public void listClick(View view) throws JSONException {

        buttonPretty.setClickable(false);
        buttonPretty.setVisibility(View.INVISIBLE);
        textViewData.setVisibility(View.INVISIBLE);

        /*
        Lastly, we are just creating a listview for better viewing for the end user.
         */
        ListView listView = findViewById(R.id.listJSON);
        listView.setVisibility(View.VISIBLE);

        ArrayAdapter arrAdp = new ArrayAdapter<>(this, androidx.appcompat.R.layout.support_simple_spinner_dropdown_item, filterList);
        listView.setAdapter(arrAdp);

    }
}
