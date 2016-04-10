package mlevy94.robiny.gettingwarmer;

import android.location.Location;
import android.text.Html;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by lucun_000 on 4/9/2016.
 */
public class MainAppData {
    private Location currentLocation;
    private Location finalDestination;
    private Location currDestination;
    private JSONObject directionJSONFile;
    private JSONObject locationSearchJSONFile;
    private JSONArray directionStepsArray;
    private int currStep;
    private double direction;
    private boolean locationSearchJSONGood;
    private boolean directionJSONGood;
    private float bearingHistory[];
    double angleDiff;
    private int getBearingHistoryIndex;
    private int bearingHistoryCount;
    private double oldDistance;

    public MainAppData(){
        direction = 0;
        locationSearchJSONGood = false;
        directionJSONGood = false;
        currStep = -1;
        bearingHistory = new float[5];
        bearingHistoryCount = 0;
        getBearingHistoryIndex = 0;
        oldDistance = 0;
        angleDiff = 0;
    }

    public void setLocation(Location newHere){
        if(newHere != null)
            this.currentLocation = new Location(newHere);
    }

    public Location getLocation(){
        return this.currentLocation;
    }

    public void setFinalDestination(Location in){
        if(in != null){
            this.finalDestination = new Location(in);
        }
    }

    public Location getFinalDestination(){
        return this.finalDestination;
    }

    private void setCurrDestination(Location in){
        if(in != null){
            this.currDestination = new Location(in);
        }
    }

    public Location getCurrDestination(){
        return this.currDestination;
    }


    public String getLocationLatiLongiString(){
        if(this.currentLocation != null){
            double longi = this.currentLocation.getLongitude();
            double lati = this.currentLocation.getLatitude();
            return lati + "," + longi;
        }
        return null;
    }


    public void setDirectionJSONFile(String inJSON) {
        if(inJSON != null){
            try {
                this.directionJSONFile = new JSONObject(inJSON);
                if(this.isOKGoogleJSONFile(this.directionJSONFile)){
                    this.directionJSONGood = true;
                    this.directionStepsArray = this.directionJSONFile.getJSONArray("routes").getJSONObject(0).getJSONArray("legs")
                                                    .getJSONObject(0).getJSONArray("steps");
                    this.currStep = -1;
                    this.currDestination = null;
                }
                else{
                    this.directionJSONFile = null;
                }
            }
            catch(JSONException e){
                throw new RuntimeException(e);
            }
        }
    }


    public void setlocationSearchJSONFile(String inJSON){
        if(inJSON != null){
            try {
                this.locationSearchJSONFile = new JSONObject(inJSON);
                if(this.isOKGoogleJSONFile(this.locationSearchJSONFile)){
                    this.locationSearchJSONGood = true;
                }
                else{
                    this.locationSearchJSONFile = null;
                }
            }
            catch(JSONException e){
                throw new RuntimeException(e);
            }
        }
    }

    private boolean isOKGoogleJSONFile(JSONObject inJSONObj){
        try {
            if( inJSONObj != null && inJSONObj.getString("status").equals("OK")) {
                return true;
            }
            return false;
        }
        catch (JSONException e){
            throw new RuntimeException(e);
        }
    }

    public Location getLocationlocationSearchJSONFile(){
        try{
            JSONObject temp = this.locationSearchJSONFile.getJSONArray("results").getJSONObject(0).getJSONObject("geometry").getJSONObject("location");
            String lati = temp.getString("lat");
            String longi = temp.getString("lng");
            Location finaldest = new Location("");
            finaldest.setLatitude(Double.parseDouble(lati));
            finaldest.setLongitude(Double.parseDouble(longi));
            return finaldest;
        }
        catch(JSONException e){
            throw new RuntimeException(e);
        }
    }

    public void setDirection( double in){
        this.direction = in;
    }

    public double getDirection(){
        return this.direction;
    }

    public boolean getLocationSearchJSONGood(){
        return this.locationSearchJSONGood;
    }

    public boolean getDirectionJSONGood(){
        return this.directionJSONGood;
    }

    public JSONObject getCurrStep() {
        try {
            return this.directionStepsArray.getJSONObject(currStep);
        }
        catch(JSONException e) {
            throw new RuntimeException(e);
        }
    }

    public String getCurrStepInstruction(){
        try {
            return Html.fromHtml(this.directionStepsArray.getJSONObject(currStep).getString("html_instructions")).toString();
        }
        catch(JSONException e) {
            throw new RuntimeException(e);
        }
    }

    public void nextStep(){
        if(this.directionStepsArray != null) {
            this.currStep++;
            try {
                double nextCurrDestLat = Double.parseDouble(this.getCurrStep().getJSONObject("end_location").getString("lat"));
                double nextCurrDestLng = Double.parseDouble(this.getCurrStep().getJSONObject("end_location").getString("lng"));
                Location nextCurrDest = new Location("");
                nextCurrDest.setLatitude(nextCurrDestLat);
                nextCurrDest.setLongitude(nextCurrDestLng);
                this.setCurrDestination(nextCurrDest);
            }
            catch (JSONException e){
                throw new RuntimeException(e);
            }
        }
    }

    public int getCurrStepNumberDEBUG(){
        return this.currStep;
    }

    public void addBearingHistory(float newBearing){
        if(this.bearingHistoryCount < 4){
            this.bearingHistory[this.getBearingHistoryIndex] = newBearing;
            this.bearingHistoryCount++;
            this.getBearingHistoryIndex++;
        }
        else if(this.getBearingHistoryIndex < 4){
            this.bearingHistory[this.getBearingHistoryIndex] = newBearing;
            this.getBearingHistoryIndex++;
        }
        else{
            this.bearingHistory[this.getBearingHistoryIndex] = newBearing;
            this.getBearingHistoryIndex = 0;
        }
    }

    public float getAverageBearingHistory(){
        float total = 0;
        for(int i = 0; i < this.bearingHistoryCount; i++)
            total += this.bearingHistory[i];
        return total/this.bearingHistoryCount;
    }

    public String getDirectionalUpdate(){
        return "";
    }

    public double getDistanceChange(double newDistance){
        double difference = this.oldDistance = newDistance;
        this.oldDistance = newDistance;
        return difference;
    }

    public void setAngleDiff(double angleDiff){
        this.angleDiff = angleDiff;
    }

    public double getAngleDiff(){
        return this.angleDiff;
    }
}
