package mlevy94.robiny.gettingwarmer;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.media.AudioManager;
import android.os.AsyncTask;
import android.os.Vibrator;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatCallback;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
//import com.google.android.gms.common.ConnectionResult;
//import com.google.android.gms.common.api.GoogleApiClient;
//import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
//import com.google.android.gms.location.places.Places;


public class MainActivity extends AppCompatActivity implements Button.OnClickListener
        ,ActivityCompat.OnRequestPermissionsResultCallback,
        TextToSpeech.OnInitListener
//        ,GoogleApiClient.OnConnectionFailedListener
{
    //GUI ELEMENTS
    TextView JSONText;
    TextView gotoPlaceText;
    Button enterButt;
    Button stopButt;
    Button repeatButt;

    //DATA STRUCTS
    RequestQueue HTMLReqQueue;
    MainAppData mainAppData;
    GPSManager mGPSManager;
    DirectionManager mDirectionManager;
    RumbleAsync mRumbleAsync;
    Vibrator vibrator;
    private TextToSpeech textToSpeech;
    private int speachID;
    private int misdirectCount;
    private boolean isRun;
    boolean instructgiven;
    boolean firstmessagecheck;
    boolean asyncRunning;
    String directional;
    String prevDirectional;
//    private GoogleApiClient mGoogleApiClient;


    //CONSTANTS
    private static final String FORWARD = "go forward", BACKWARD = "turn around", LEFT = "turn left", RIGHT = "turn right";
    private static final int REQUEST_LOCATION = 0;
    final String GMapsReqURL1 = "https://maps.googleapis.com/maps/api/directions/json?"
            +"origin=";
    final String GMapsReqURL2 = "&destination=";
    final String GMapsReqURL3 = "&mode=walking"
            +"&key=AIzaSyCeCjVKRDSW76UHXePbe9hMOPQEaNsvkMY";

    final String GLocSearchReqURL1 = "https://maps.googleapis.com/maps/api/place/textsearch/json?query=";
    final String GLocSearchReqURL2 = "&key=AIzaSyCeCjVKRDSW76UHXePbe9hMOPQEaNsvkMY";

    final String blinkyURL1 = "http://192.168.43.62:8080/bd641b2cf52c40399eab4697b10e605a/pin/d1";
    private final int REQ_CODE_SPEECH_INPUT = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //find elements views
        JSONText = (TextView)findViewById(R.id.JSONText);
        gotoPlaceText = (TextView)findViewById(R.id.gotoPlaceText);
        enterButt = (Button)findViewById(R.id.enterButt);
        repeatButt = (Button)findViewById(R.id.repeatButt);
        stopButt = (Button)findViewById(R.id.stopButt);
        vibrator =  (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);

        //init data classes
        HTMLReqQueue = Volley.newRequestQueue(this);
        mGPSManager = new GPSManager(this);
        mainAppData = new MainAppData();
        mDirectionManager = new DirectionManager(this);
        isRun = false;
        instructgiven = false;
        firstmessagecheck = false;
        asyncRunning = false;
        speachID = 0;
        misdirectCount = 0;
/*
        mGoogleApiClient = new GoogleApiClient
                .Builder(this)
                .addApi(Places.GEO_DATA_API)
                .enableAutoManage(this, this)
                .build();
*/
        //assign listeners
        enterButt.setOnClickListener(this);
        repeatButt.setOnClickListener(this);
        stopButt.setOnClickListener(this);
/*
        if(textToSpeech == null){
            textToSpeech = new TextToSpeech(this, this);
            textToSpeech.setLanguage(Locale.US);
            textToSpeech.setSpeechRate(0.8f);
            textToSpeech.setOnUtteranceProgressListener(new ttsUtteranceListener());
        }*/
    }

    private void requestDirections(String DestinationLatLongiString){
        if(mainAppData.getLocationLatiLongiString() != null && DestinationLatLongiString != null) {
            String GMapsReqURL = GMapsReqURL1
                    + mainAppData.getLocationLatiLongiString()
                    + GMapsReqURL2
                    + DestinationLatLongiString
                    + GMapsReqURL3;
            StringRequest directionsRequest = new StringRequest(Request.Method.GET, GMapsReqURL,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            textToSpeech.speak("navigation directions found", TextToSpeech.QUEUE_ADD, null, "normalMessage");
                            mainAppData.setDirectionJSONFile(response);
                            JSONText.setText("Getting Directions");
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            JSONText.setText("That didn't work!");
                        }
                    });
            HTMLReqQueue.add(directionsRequest);
        }
        else {
            Toast.makeText(this.getApplicationContext(), "Cannot find directions", Toast.LENGTH_LONG).show();
            textToSpeech.speak("cannot get navigation directions, check connection", TextToSpeech.QUEUE_ADD, null, "normalMessage");

        }
    }

    void requestDestinationlocationSearch(final String InputLocationString){
        if(InputLocationString != null && !InputLocationString.equals(""))
        {
            String GLocSearchReqURL = GLocSearchReqURL1 + InputLocationString.replaceAll(" ", "+") + GLocSearchReqURL2;
            StringRequest locationSearchRequest = new StringRequest(Request.Method.GET, GLocSearchReqURL,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            mainAppData.setlocationSearchJSONFile(response);
                            JSONText.setText("Looking up Location: " + InputLocationString);
                            if(mainAppData.getLocationSearchJSONGood()) {
                                mainAppData.setFinalDestination(mainAppData.getLocationlocationSearchJSONFile());
                                textToSpeech.speak("Looking up Location: " + InputLocationString +".", TextToSpeech.QUEUE_ADD, null, "normalMessage");
                                requestDirections(String.valueOf(mainAppData.getFinalDestination().getLatitude())
                                        + ","
                                        + String.valueOf(mainAppData.getFinalDestination().getLongitude()));
                            }
                            else{
                                //TODO: ERROR HTML REPLY IS NOT OK
                            }

                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            JSONText.setText("That didn't work!");
                        }
                    });
            HTMLReqQueue.add(locationSearchRequest);
        }
        else {
            Toast.makeText(this.getApplicationContext(), "Cannot get locationSearch information", Toast.LENGTH_LONG).show();
            textToSpeech.speak("cannot search location, check network", TextToSpeech.QUEUE_ADD, null, "normalMessage");
        }

    }

    void updateGPSLocation(Location newLoc){
        mainAppData.setLocation(newLoc);
        if(isRun) {
            if (mainAppData.getDirectionJSONGood() && this.mainAppData.getLocation() != null) {
                //check if we have a curr dest
                if (mainAppData.getCurrDestination() != null) {
                    //check distance to currDest
                    double ftDistance = this.mainAppData.getLocation().distanceTo(mainAppData.getCurrDestination());
                    double destBearing = this.mainAppData.getLocation().bearingTo(this.mainAppData.getCurrDestination());
                    destBearing += 180;
                    double angle = this.mainAppData.getDirection() - destBearing;
                    if(angle < -180)
                        angle += 360;
                    if(angle > 180)
                        angle -= 360;
                    if((angle <= 30 && angle >= 0) || (angle >= -30 && angle <= 0))
                        directional = FORWARD;
                    else if((angle <= -30) && (angle >= -135))
                        directional = RIGHT;
                    else if((angle >= 30) && (angle <=135))
                        directional = LEFT;
                    else if((angle <= -135) || (angle >= 135 ))
                        directional = BACKWARD;
                    this.mainAppData.setAngleDiff(angle);
                    String debugout = "Curr Step #" + this.mainAppData.getCurrStepNumberDEBUG() + ":\n"
                            + "Distance: " + ftDistance + " ft\n"
                            + "Instruction: " + this.mainAppData.getCurrStepInstruction() + "\n"
                            + "Needed Bearing: " + destBearing + "\n"
                            //                        + "Current Bearing: " + this.mainAppData.getAverageBearingHistory() +"\n";
                            + "Current Bearing: " + this.mainAppData.getDirection() + "\n"
                            + "angle: " + angle +"\n"
                            + "Directional: " + directional;
                    JSONText.setText(debugout);
                    if(!instructgiven) {
                        String instructionMess = "step " + (this.mainAppData.getCurrStepNumberDEBUG()+1) + ". Instruction: "
                                + this.mainAppData.getCurrStepInstruction()
                                + ". . Distance: " + (int)ftDistance +" feet";
                        textToSpeech.speak(instructionMess, TextToSpeech.QUEUE_ADD, null, "instructionMessage");
                        instructgiven = true;
                    }
                    if(prevDirectional == null || ( prevDirectional != null && !prevDirectional.equals(directional))) {
                        misdirectCount++;
                        if (misdirectCount > 0) {
                            prevDirectional = directional;
                            if (directional.equals(FORWARD)) {
                                textToSpeech.speak("Go forward", TextToSpeech.QUEUE_ADD, null, "instructionMessage");
                            }
                            else if (directional.equals(RIGHT)) {
                                textToSpeech.speak("turn right", TextToSpeech.QUEUE_ADD, null, "instructionMessage");
                            }
                            else if (directional.equals(LEFT)) {
                                textToSpeech.speak("turn left", TextToSpeech.QUEUE_ADD, null, "instructionMessage");
                            }
                            else if (directional.equals(BACKWARD)) {
                                textToSpeech.speak("turn around", TextToSpeech.QUEUE_ADD, null, "instructionMessage");
                            }
                            misdirectCount = 0;
                        }
                    }
                    if (ftDistance < 5) {
                        textToSpeech.speak("Next Step.", TextToSpeech.QUEUE_ADD, null, "instructionMessage");
                        this.mainAppData.nextStep();
                        instructgiven = false;
                    }
                    if(mRumbleAsync == null){
                        mRumbleAsync = new RumbleAsync();
                        mRumbleAsync.execute();
                        asyncRunning = true;
                    }
                } else {
                    //get first currDest
                    this.mainAppData.nextStep();
                }
            }
        }
    }

    void updateDirection(double inangle){
        mainAppData.setDirection(inangle);
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == enterButt.getId()) {
            textToSpeech.speak("please tell me your destination", TextToSpeech.QUEUE_ADD, null, "promptspeachtotext");
        }
        else if(v.getId() == repeatButt.getId()){
            instructgiven = false;
        }
        else if(v.getId() == stopButt.getId()){
            enterButt.setVisibility(Button.VISIBLE);
            repeatButt.setVisibility(Button.GONE);
            stopButt.setVisibility(Button.GONE);
            isRun = false;
            textToSpeech.speak("navigation stopped", TextToSpeech.QUEUE_ADD, null, "normalMessage");
            if(mRumbleAsync != null && mRumbleAsync.getStatus() == AsyncTask.Status.RUNNING){
                mRumbleAsync.cancel(true);
                mRumbleAsync = null;
                asyncRunning = false;
            }

        }
    }

    private void promptSpeachInput(){
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
                "Say something");
        try {
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
        } catch (ActivityNotFoundException a) {
            Toast.makeText(getApplicationContext(),
                    "speech to text not supported",
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQ_CODE_SPEECH_INPUT: {
                if (resultCode == RESULT_OK && null != data) {

                    ArrayList<String> result = data
                            .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    gotoPlaceText.setText(result.get(0));
                    this.requestDestinationlocationSearch(gotoPlaceText.getText().toString());
                    enterButt.setVisibility(Button.GONE);
                    repeatButt.setVisibility(Button.VISIBLE);
                    stopButt.setVisibility(Button.VISIBLE);
                    isRun = true;
                }
                break;
            }

        }
        this.onStart();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(asyncRunning){
            mRumbleAsync = new RumbleAsync();
            mRumbleAsync.execute();
        }
        mGPSManager.register();
        mDirectionManager.register();
        textToSpeech = new TextToSpeech(this, this);
        textToSpeech.setLanguage(Locale.US);
        textToSpeech.setSpeechRate(0.8f);
        textToSpeech.setOnUtteranceProgressListener(new ttsUtteranceListener());
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(mRumbleAsync != null && mRumbleAsync.getStatus()==AsyncTask.Status.RUNNING){
            mRumbleAsync.cancel(true);
            mRumbleAsync = null;
        }
        mGPSManager.unregister();
        mDirectionManager.unregister();
        if(textToSpeech != null){
            textToSpeech.stop();
            textToSpeech.shutdown();
            textToSpeech = null;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 1
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED
                        && grantResults[1] == PackageManager.PERMISSION_GRANTED){
                    mGPSManager = new GPSManager(this);
                    mGPSManager.register();
                }
                else{
                    Toast.makeText(this.getApplicationContext(), "Permission rejected, exiting...", Toast.LENGTH_LONG).show();
                    textToSpeech.speak("location permission rejected, exiting", TextToSpeech.QUEUE_ADD, null, "normalMessage");
                    this.finish();
                }
                return;
            }
            default:{
                Toast.makeText(this.getApplicationContext(), "Permission rejected, exiting...", Toast.LENGTH_LONG).show();
                textToSpeech.speak("location permission rejected, exiting", TextToSpeech.QUEUE_ADD, null, "normalMessage");
                this.finish();
                return;
            }
            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    @Override
    public void onInit(int status) {
        if(status == TextToSpeech.SUCCESS && !firstmessagecheck){
            textToSpeech.speak("Application opened", TextToSpeech.QUEUE_FLUSH, null, "normalMessage");
            firstmessagecheck = true;
        }
    }

    class ttsUtteranceListener extends UtteranceProgressListener {

        @Override
        public void onDone(String utteranceId) {
            if (utteranceId.equals("promptspeachtotext")) {
                promptSpeachInput();
            }
        }

        @Override
        public void onError(String utteranceId) {
        }

        @Override
        public void onStart(String utteranceId) {
        }
    }


    private class RumbleAsync extends AsyncTask<Double, StringRequest, Void>{
        @Override
        protected Void doInBackground(Double... doubles){
            boolean state = false;
            while(!isCancelled()){
                try{
                    if(Math.abs((int) mainAppData.getAngleDiff()) < 30) {
                        Thread.sleep(360);
                        blinkyCom(false);
                    }
                    else{
                        int sleeptime = (180 - Math.abs((int) mainAppData.getAngleDiff())) * 2;
                        Thread.sleep(sleeptime);
                        state = !state;
                        blinkyCom(state);
                    }

                }
                catch (InterruptedException e){
                    e.printStackTrace();
                }
            }
            blinkyCom(false);
            vibrator.cancel();
            return null;
        }

        private void blinkyCom(boolean state){
            String booleanstate = "[\"1\"]";
            String blinkyURL = blinkyURL1;
            JSONArray payload;
            if(state) {
                booleanstate = "[\"0\"]";
                vibrator.vibrate(1000);
            }
            else{
                vibrator.cancel();
            }
            try {
                payload = new JSONArray(booleanstate);
            }
            catch(JSONException e){
                throw new RuntimeException(e);
            }
            JsonArrayRequest blinkyRequest= new JsonArrayRequest(Request.Method.PUT, blinkyURL, payload,
                    new Response.Listener<JSONArray>() {
                        @Override
                        public void onResponse(JSONArray response){
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                        }
                    });
            HTMLReqQueue.add(blinkyRequest);
        }
    }
    /*
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Toast.makeText(this.getApplicationContext(), "Cannot connect to Google Play ERROR CODE: " + connectionResult.getErrorCode(), Toast.LENGTH_LONG).show();
        this.finish();
    }

*/
}
