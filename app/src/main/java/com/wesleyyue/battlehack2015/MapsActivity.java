package com.wesleyyue.battlehack2015;

import android.content.Intent;
import android.graphics.Point;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.animation.BounceInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.TextView;

import com.braintreepayments.api.dropin.BraintreePaymentActivity;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.TextHttpResponseHandler;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import org.apache.http.Header;

import java.util.HashMap;
import java.util.List;


public class MapsActivity extends FragmentActivity implements GoogleMap.OnMarkerClickListener, GoogleMap.OnMapClickListener{

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private String braintree_token;
    private static String server_domain = "http://mighty-retreat-5059.herokuapp.com";
    private static final int BRAINTREE_REQUEST_CODE = 100;
    private View BottomSlideOut;
    private static boolean peeking_info_pane = false;
    private static boolean expanded_info_pane = false;
    private static final int info_pane_height = 1100;
    private static final int peeking_height = 300;
    private static final int expanded_height = info_pane_height - peeking_height;
    private boolean in_rent_mode = false;

    HashMap<String, ParseObject> marker_info;

    TextView hourlyRate;
    TextView review1;
    TextView review2;
    TextView review3;
    TextView review1author;
    TextView review2author;
    TextView review3author;

    Button rentbtn;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);




        hourlyRate = (TextView)findViewById(R.id.hourlyRate);
        review1 = (TextView)findViewById(R.id.review1);
        review2 = (TextView)findViewById(R.id.review2);
        review3 = (TextView)findViewById(R.id.review3);
        review1author = (TextView)findViewById(R.id.review1author);
        review2author = (TextView)findViewById(R.id.review2author);
        review3author = (TextView)findViewById(R.id.review3author);
        rentbtn = (Button)findViewById(R.id.rentBtn);



        // Enable Local Datastore.
        Parse.enableLocalDatastore(this);
        Parse.initialize(this, "m62ASk25Hb1HaaMBWUQ6XeI7VKcbTn1A0g1KDtZp", "DaAlxgusMVeMn6fo05UMVF9lTwlZy2VCXa59BMgB");

        marker_info = new HashMap<String, ParseObject>();


        setUpMapIfNeeded();

        getToken();
        mMap.setOnMarkerClickListener(this);
        mMap.setOnMapClickListener(this);


        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int window_width = size.x;
        int window_height = size.y;


        BottomSlideOut = findViewById(R.id.BottomSlideOut);
        BottomSlideOut.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        Log.d("bottomHeight", "height = " + window_height);
        BottomSlideOut.setTranslationY(window_height);
        BottomSlideOut.animate().setInterpolator(new DecelerateInterpolator()).setDuration(200);

    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpMap()} once when {@link #mMap} is not null.
     * <p/>
     * If it isn't installed {@link SupportMapFragment} (and
     * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
     * install/update the Google Play services APK on their device.
     * <p/>
     * A user can return to this FragmentActivity after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the FragmentActivity may not
     * have been completely destroyed during this process (it is likely that it would only be
     * stopped or paused), {@link #onCreate(Bundle)} may not be called again so we should call this
     * method in {@link #onResume()} to guarantee that it will be called.
     */
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }
    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p/>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap() {
//        mMap.addMarker(new MarkerOptions().position(new LatLng(0, 0)).title("Marker"));



        ParseQuery<ParseObject> query = ParseQuery.getQuery("Bikes");
//        query.whereEqualTo("playerName", "Dan Stemkoski");
        query.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> scoreList, ParseException e) {
                if (e == null) {
                    for (ParseObject object : scoreList) {
                        Log.d("score", "here: " + object.getDouble("lon"));
                        double lon = object.getDouble("lon");
                        double lat = object.getDouble("lat");

//                        Log.d("hmtesting", object.getObjectId());

                        mMap.addMarker(new MarkerOptions().position(new LatLng(lon, lat)).title(object.getObjectId()));
                        marker_info.put(object.getObjectId(), object);
                    }
                } else {
                    Log.d("score", "Error: " + e.getMessage());
                }
            }
        });
    }

    // Braintree
    private void getToken() {
        AsyncHttpClient client = new AsyncHttpClient();
        client.get(server_domain + "/client_token", new TextHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                // TODO: failure handling
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                braintree_token = responseString;
            }
        });
    }

    public void onBraintreeSubmit(View v) {
        Intent intent = new Intent(this, BraintreePaymentActivity.class);
        intent.putExtra(BraintreePaymentActivity.EXTRA_CLIENT_TOKEN, "eyJ2ZXJzaW9uIjoyLCJhdXRob3JpemF0aW9uRmluZ2VycHJpbnQiOiIxN2IzYmU3MjI2NzI2ODRkZTBiZDk4Yjk4NzgyMGYyYWE1NjhkMGUwNGJiNmIwNGJiYmMzY2M2NjBiZjY5MjZlfGNyZWF0ZWRfYXQ9MjAxNS0wNy0xOFQyMzoxMjowMy41MTc0ODk5NjErMDAwMFx1MDAyNm1lcmNoYW50X2lkPWRjcHNweTJicndkanIzcW5cdTAwMjZwdWJsaWNfa2V5PTl3d3J6cWszdnIzdDRuYzgiLCJjb25maWdVcmwiOiJodHRwczovL2FwaS5zYW5kYm94LmJyYWludHJlZWdhdGV3YXkuY29tOjQ0My9tZXJjaGFudHMvZGNwc3B5MmJyd2RqcjNxbi9jbGllbnRfYXBpL3YxL2NvbmZpZ3VyYXRpb24iLCJjaGFsbGVuZ2VzIjpbXSwiZW52aXJvbm1lbnQiOiJzYW5kYm94IiwiY2xpZW50QXBpVXJsIjoiaHR0cHM6Ly9hcGkuc2FuZGJveC5icmFpbnRyZWVnYXRld2F5LmNvbTo0NDMvbWVyY2hhbnRzL2RjcHNweTJicndkanIzcW4vY2xpZW50X2FwaSIsImFzc2V0c1VybCI6Imh0dHBzOi8vYXNzZXRzLmJyYWludHJlZWdhdGV3YXkuY29tIiwiYXV0aFVybCI6Imh0dHBzOi8vYXV0aC52ZW5tby5zYW5kYm94LmJyYWludHJlZWdhdGV3YXkuY29tIiwiYW5hbHl0aWNzIjp7InVybCI6Imh0dHBzOi8vY2xpZW50LWFuYWx5dGljcy5zYW5kYm94LmJyYWludHJlZWdhdGV3YXkuY29tIn0sInRocmVlRFNlY3VyZUVuYWJsZWQiOnRydWUsInRocmVlRFNlY3VyZSI6eyJsb29rdXBVcmwiOiJodHRwczovL2FwaS5zYW5kYm94LmJyYWludHJlZWdhdGV3YXkuY29tOjQ0My9tZXJjaGFudHMvZGNwc3B5MmJyd2RqcjNxbi90aHJlZV9kX3NlY3VyZS9sb29rdXAifSwicGF5cGFsRW5hYmxlZCI6dHJ1ZSwicGF5cGFsIjp7ImRpc3BsYXlOYW1lIjoiQWNtZSBXaWRnZXRzLCBMdGQuIChTYW5kYm94KSIsImNsaWVudElkIjpudWxsLCJwcml2YWN5VXJsIjoiaHR0cDovL2V4YW1wbGUuY29tL3BwIiwidXNlckFncmVlbWVudFVybCI6Imh0dHA6Ly9leGFtcGxlLmNvbS90b3MiLCJiYXNlVXJsIjoiaHR0cHM6Ly9hc3NldHMuYnJhaW50cmVlZ2F0ZXdheS5jb20iLCJhc3NldHNVcmwiOiJodHRwczovL2NoZWNrb3V0LnBheXBhbC5jb20iLCJkaXJlY3RCYXNlVXJsIjpudWxsLCJhbGxvd0h0dHAiOnRydWUsImVudmlyb25tZW50Tm9OZXR3b3JrIjp0cnVlLCJlbnZpcm9ubWVudCI6Im9mZmxpbmUiLCJ1bnZldHRlZE1lcmNoYW50IjpmYWxzZSwiYnJhaW50cmVlQ2xpZW50SWQiOiJtYXN0ZXJjbGllbnQzIiwibWVyY2hhbnRBY2NvdW50SWQiOiJzdGNoMm5mZGZ3c3p5dHc1IiwiY3VycmVuY3lJc29Db2RlIjoiVVNEIn0sImNvaW5iYXNlRW5hYmxlZCI6dHJ1ZSwiY29pbmJhc2UiOnsiY2xpZW50SWQiOiIxMWQyNzIyOWJhNThiNTZkN2UzYzAxYTA1MjdmNGQ1YjQ0NmQ0ZjY4NDgxN2NiNjIzZDI1NWI1NzNhZGRjNTliIiwibWVyY2hhbnRBY2NvdW50IjoiY29pbmJhc2UtZGV2ZWxvcG1lbnQtbWVyY2hhbnRAZ2V0YnJhaW50cmVlLmNvbSIsInNjb3BlcyI6ImF1dGhvcml6YXRpb25zOmJyYWludHJlZSB1c2VyIiwicmVkaXJlY3RVcmwiOiJodHRwczovL2Fzc2V0cy5icmFpbnRyZWVnYXRld2F5LmNvbS9jb2luYmFzZS9vYXV0aC9yZWRpcmVjdC1sYW5kaW5nLmh0bWwiLCJlbnZpcm9ubWVudCI6Im1vY2sifSwibWVyY2hhbnRJZCI6ImRjcHNweTJicndkanIzcW4iLCJ2ZW5tbyI6Im9mZmxpbmUiLCJhcHBsZVBheSI6eyJzdGF0dXMiOiJtb2NrIiwiY291bnRyeUNvZGUiOiJVUyIsImN1cnJlbmN5Q29kZSI6IlVTRCIsIm1lcmNoYW50SWRlbnRpZmllciI6Im1lcmNoYW50LmNvbS5icmFpbnRyZWVwYXltZW50cy5zYW5kYm94LkJyYWludHJlZS1EZW1vIiwic3VwcG9ydGVkTmV0d29ya3MiOlsidmlzYSIsIm1hc3RlcmNhcmQiLCJhbWV4Il19fQ==");
        startActivityForResult(intent, BRAINTREE_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == BRAINTREE_REQUEST_CODE) {
            if (resultCode == BraintreePaymentActivity.RESULT_OK) {
                String paymentMethodNonce = data.getStringExtra(BraintreePaymentActivity.EXTRA_PAYMENT_METHOD_NONCE);
                postNonceToServer(paymentMethodNonce);
            }
        }
    }

    private void postNonceToServer(String paymentMethodNonce) {
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        params.put("payment_method_nonce", paymentMethodNonce);
        client.post(server_domain + "/payment-methods", params,
                new AsyncHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                        // TODO: start renting view
                        othernfc();
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                        try {
                            throw error;
                        } catch (Throwable throwable) {
                            throwable.printStackTrace();
                        }
                        Log.d("postNonce", "failed");
                    }
                }
        );
    }
    // End of braintree code

    public void nfcActivity (View view) {
        if (in_rent_mode){
            in_rent_mode = false;
            Intent intent = new Intent(this, BraintreePaymentActivity.class);
            intent.putExtra(BraintreePaymentActivity.EXTRA_CLIENT_TOKEN, braintree_token);
            startActivityForResult(intent, BRAINTREE_REQUEST_CODE);

        } else {
            ParseObject bike_rides = new ParseObject("bike_rides");
            bike_rides.put("user_id", 1);
            bike_rides.saveInBackground();
            in_rent_mode = true;
            rentbtn.setText("End rental");
            Intent intent = new Intent(this, NFCActivity.class);
            Bundle b = new Bundle();
            b.putInt("lock", 0); //Your id
            intent.putExtras(b); //Put your id to your next Intent
            startActivity(intent);
        }


    }
    public void othernfc() {
        Intent intent = new Intent(this, NFCActivity.class);
        Bundle b = new Bundle();
        b.putInt("lock", 1); //Your id
        intent.putExtras(b); //Put your id to your next Intent
        startActivity(intent);
        rentbtn.setText("Rent");
    }




    @Override
    public boolean onMarkerClick(Marker marker) {
        ParseObject current_marker_info = marker_info.get(marker.getTitle());
        Log.d("hmtesting", current_marker_info.getObjectId());

        hourlyRate.setText("$" + current_marker_info.getNumber("rate") + "/hr");
        review1.setText(current_marker_info.getString("review1"));
        review2.setText(current_marker_info.getString("review2"));
        review3.setText(current_marker_info.getString("review3"));
        review1author.setText(current_marker_info.getString("author1"));
        review2author.setText(current_marker_info.getString("author2"));
        review3author.setText(current_marker_info.getString("author3"));


        if (!peeking_info_pane){
            BottomSlideOut.animate().translationYBy(-peeking_height);
            peeking_info_pane = true;
        }

        return true;
    }

    @Override
    public void onMapClick(LatLng latLng) {
        if (peeking_info_pane && expanded_info_pane) {
            BottomSlideOut.animate().translationYBy(info_pane_height);
            peeking_info_pane = false;
            expanded_info_pane = false;
        } else if (peeking_info_pane) {
            BottomSlideOut.animate().translationYBy(peeking_height);
            peeking_info_pane = false;
        }

    }

    public void toggleExpandInfoPane(View view) {
        if(expanded_info_pane){
            BottomSlideOut.animate().translationYBy(expanded_height);
        }else{
            BottomSlideOut.animate().translationYBy(-expanded_height);
        }

        expanded_info_pane = !expanded_info_pane;
    }

    private void addBike (View view){
        Intent intent = new Intent(this, AddBikeActivity.class);
        startActivity(intent);
    }
}
