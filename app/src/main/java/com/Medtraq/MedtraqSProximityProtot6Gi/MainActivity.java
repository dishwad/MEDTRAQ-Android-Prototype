package com.Medtraq.MedtraqSProximityProtot6Gi;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.Medtraq.MedtraqSProximityProtot6Gi.estimote.BeaconID;
import com.Medtraq.MedtraqSProximityProtot6Gi.estimote.EstimoteCloudBeaconDetails;
import com.Medtraq.MedtraqSProximityProtot6Gi.estimote.EstimoteCloudBeaconDetailsFactory;
import com.Medtraq.MedtraqSProximityProtot6Gi.estimote.ProximityContentManager;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.estimote.sdk.SystemRequirementsChecker;
import com.estimote.sdk.cloud.model.Color;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

//
// Running into any issues? Drop us an email to: contact@estimote.com
//

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private static final Map<Color, Integer> BACKGROUND_COLORS = new HashMap<>();

    static {
        BACKGROUND_COLORS.put(Color.ICY_MARSHMALLOW, android.graphics.Color.rgb(109, 170, 199));
        BACKGROUND_COLORS.put(Color.BLUEBERRY_PIE, android.graphics.Color.rgb(98, 84, 158));
        BACKGROUND_COLORS.put(Color.MINT_COCKTAIL, android.graphics.Color.rgb(155, 186, 160));
    }

    public Boolean sink_flag = false;

    private static final int BACKGROUND_COLOR_NEUTRAL = android.graphics.Color.rgb(160, 169, 172);

    private ProximityContentManager proximityContentManager;

    public void sendMyData(String location, String user, Boolean sink_flag){
        final String url = "https://webdevbootcamp-dishwad.c9users.io/beacons/" + location + "/" + user + "/" + sink_flag.toString();

        final JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.POST, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.v("MEDTRAQ sendMyData", "RES: " + response.toString());
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.v("MEDTRAQ err sendMyData", "EXC: " + error.getLocalizedMessage());
            }
        });
        Volley.newRequestQueue(this).add(jsonRequest);
    }

    public void getFlagData(){
        final String url = "https://webdevbootcamp-dishwad.c9users.io/sink_flag/";

        final JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.v("MEDTRAQ getFlagData", "RES: " + response.toString());
                try {
                    if(response.getBoolean("sink_flag")) sink_flag = response.getBoolean("sink_flag");
                } catch (JSONException e){
                    Log.v("JSON getFlagData", "EXC: " + e.getLocalizedMessage());
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.v("MEDTRAQ err getFlagData", "EXC: " + error.getLocalizedMessage());
            }
        });
        Volley.newRequestQueue(this).add(jsonRequest);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        proximityContentManager = new ProximityContentManager(this,
                Arrays.asList(
                        new BeaconID("B9407F30-F5F8-466E-AFF9-25556B57FE6D", 57532, 54960),
                        new BeaconID("B9407F30-F5F8-466E-AFF9-25556B57FE6D", 21161, 62003),
                        new BeaconID("B9407F30-F5F8-466E-AFF9-25556B57FE6D", 28641, 29683),
                        new BeaconID("B9407F30-F5F8-466E-AFF9-25556B57FE6D", 50999, 46282),
                        new BeaconID("B9407F30-F5F8-466E-AFF9-25556B57FE6D", 58449, 63577),
                        new BeaconID("B9407F30-F5F8-466E-AFF9-25556B57FE6D", 57736, 57813)),
                new EstimoteCloudBeaconDetailsFactory());
        proximityContentManager.setListener(new ProximityContentManager.Listener() {
            @Override
            public void onContentChanged(Object content) {
                String text;
                String location;
                Integer backgroundColor;
                if (content != null) {
                    EstimoteCloudBeaconDetails beaconDetails = (EstimoteCloudBeaconDetails) content;
                    if(beaconDetails.getBeaconName().equalsIgnoreCase("candy")){
                        text = "You're near the patient bed!";
                        location = "patient_bed";
                        getFlagData();
                        if (!sink_flag){
                            Toast.makeText(getApplicationContext(), "You need to wash your hands!", Toast.LENGTH_SHORT).show();
                        }
                        sendMyData(location, "alexander", false);
                    } else if (beaconDetails.getBeaconName().equalsIgnoreCase("lemon")){
                        text = "You're near the the sink!";
                        location = "sink";
                        sendMyData(location, "alexander", true);
                    } else {
                        text = "No beacons in range.";
                    }
                    backgroundColor = BACKGROUND_COLORS.get(beaconDetails.getBeaconColor());
                } else {
                    text = "No beacons in range.";
                    backgroundColor = null;
                }
                ((TextView) findViewById(R.id.textView)).setText(text);
                findViewById(R.id.relativeLayout).setBackgroundColor(
                        backgroundColor != null ? backgroundColor : BACKGROUND_COLOR_NEUTRAL);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!SystemRequirementsChecker.checkWithDefaultDialogs(this)) {
            Log.e(TAG, "Can't scan for beacons, some pre-conditions were not met");
            Log.e(TAG, "Read more about what's required at: http://estimote.github.io/Android-SDK/JavaDocs/com/estimote/sdk/SystemRequirementsChecker.html");
            Log.e(TAG, "If this is fixable, you should see a popup on the app's screen right now, asking to enable what's necessary");
        } else {
            Log.d(TAG, "Starting ProximityContentManager content updates");
            proximityContentManager.startContentUpdates();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "Stopping ProximityContentManager content updates");
        proximityContentManager.stopContentUpdates();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        proximityContentManager.destroy();
    }
}
