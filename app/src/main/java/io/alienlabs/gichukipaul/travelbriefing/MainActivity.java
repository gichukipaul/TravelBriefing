package io.alienlabs.gichukipaul.travelbriefing;

import android.content.Context;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.button.MaterialButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cn.pedant.SweetAlert.SweetAlertDialog;
import io.alienlabs.gichukipaul.travelbriefing.Utils.Constants;

public class MainActivity extends AppCompatActivity {

    AutoCompleteTextView completeTextView;
    MaterialButton btn;
    static RequestQueue requestQueue;
    TextView tx;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        requestQueue = Volley.newRequestQueue(this);

        tx = findViewById(R.id.result);
        tx.setVisibility(View.GONE);
        btn = findViewById(R.id.search_btn);
        btn.setText("SEARCH");
        completeTextView = findViewById(R.id.autoCompleteTextView);
        completeTextView.setSelected(true);
        completeTextView.setThreshold(1);
        ArrayAdapter<String> stringArrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, Constants.countries);
        completeTextView.setAdapter(stringArrayAdapter);

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String query = completeTextView.getText().toString();
                if (!TextUtils.isEmpty(query) && isNetworkAvailable()) {
                    search(query);

                } else if (!isNetworkAvailable()) {
                    new SweetAlertDialog(MainActivity.this, SweetAlertDialog.ERROR_TYPE)
                            .setTitleText("Oops..." + ("\ud83d\ude1e"))
                            .setContentText("Internet connection not available!" + ("\ud83d\udef0"))
                            .show();
                } else {
                    new SweetAlertDialog(MainActivity.this, SweetAlertDialog.ERROR_TYPE)
                            .setTitleText("Oops...")
                            .setContentText("Something went wrong!" + ("\ud83d\ude13"))
                            .show();
                }
            }
        });
    }

    private void search(String query) {
        SweetAlertDialog pDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
        pDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
        pDialog.setTitleText("Loading" + ("\u2728") + ("\u2728"));
        pDialog.setCancelable(false);
        pDialog.show();
        String url = "https://travelbriefing.org/" + query + "?format=json";
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        String name, fullName, iso2, timezone, policeTelephone, ambulanceTelephone, currencyName;
                        StringBuffer language = new StringBuffer();
                        try {
                            JSONObject names = response.getJSONObject("names");
                            name = names.getString("name");
                            fullName = names.getString("full");
                            iso2 = names.getString("iso2");

                            JSONObject timezones = response.getJSONObject("timezone");
                            timezone = timezones.getString("name");

                            JSONArray languages = response.getJSONArray("language");
                            for (int i = 0; i < languages.length(); i++) {
                                JSONObject lang1 = languages.getJSONObject(i);
                                String temp = lang1.getString("language");
                                language.append(temp).append(",\t");
                            }
                            String totalLanguage = language.toString();

                            JSONObject phone = response.getJSONObject("telephone");
                            policeTelephone = phone.getString("police");
                            ambulanceTelephone = phone.getString("ambulance");

                            JSONObject currency = response.getJSONObject("currency");
                            currencyName = currency.getString("name");
                            pDialog.dismissWithAnimation();
                            tx.setVisibility(View.VISIBLE);

                            String queryResult = "Country name : " + name + "\n" +
                                    "Country full name : " + fullName + "\n" +
                                    "Languages : " + totalLanguage + "\n" +
                                    ("\ud83d\udc6e") + " Police Contact : " + policeTelephone + "\n" +
                                    ("\ud83d\ude91") + " Ambulance : " + ambulanceTelephone + "\n" +
                                    ("\ud83d\udcb8") + " Currency : " + currencyName + "\n" +
                                    "iso2 : " + iso2 + "\n" +
                                    ("\ud83d\udd70") + " Timezone : " + timezone + "\n";
                            tx.setText(queryResult);
                        } catch (JSONException e) {
                            e.printStackTrace();
                            pDialog.setTitleText(e.getLocalizedMessage());
                            pDialog.dismissWithAnimation();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                pDialog.setTitleText("error occurred" + ("\u26a0"));
                pDialog.dismissWithAnimation();
            }
        });

        requestQueue.add(jsonObjectRequest);

    }

    //check network connectivity
    public boolean isNetworkAvailable() {
        ConnectivityManager manager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = null;
        try {
            if (manager != null) networkInfo = manager.getActiveNetworkInfo();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        return networkInfo != null && networkInfo.isConnected();
    }

}