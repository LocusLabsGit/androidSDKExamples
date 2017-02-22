package com.example.locuslabs.examplelibrary;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.locuslabs.sdk.maps.model.Airport;
import com.locuslabs.sdk.maps.model.AirportDatabase;
import com.locuslabs.sdk.maps.model.Floor;
import com.locuslabs.sdk.maps.model.Map;
import com.locuslabs.sdk.maps.model.Marker;
import com.locuslabs.sdk.maps.model.Position;
import com.locuslabs.sdk.maps.view.MapView;

/*
* This activity loads a map and adds starts a timer that has a callback after 10 seconds
* which renders the MapView to the given venue's default radius and position. The venueCenter
* and venueRadius are both static values persisted in the venue's Venue Data JSON file.
* */
public class ExampleRecenterMap extends Activity {
    private static final String TAG = "ExampleFlightStatus";

    private AirportDatabase airportDatabase;
    private MapView mapView;
    private Airport airport;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //This activity takes a venueId parameter. The venueId represents the Airport to be loaded.
        Intent receivedIntent = getIntent();
        String venueId = receivedIntent.getStringExtra("venueId");

        //Create an AirportDatabase which allows airports to be loaded.
        airportDatabase = new AirportDatabase();

        //Load the Airport specified by the venueId passed to the activity.
        loadAirport(venueId);
    }

    @Override
    public void onBackPressed() {
        if ( mapView == null || !mapView.onBackPressed() ) {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        //-----------------------------------
        // Be sure to close the mapView and
        // airportDatabase to release the memory
        // they consume.
        //-----------------------------------

        if ( mapView != null ) {
            mapView.close();
        }

        if ( airportDatabase != null ) {
            airportDatabase.close();
        }

        airportDatabase = null;
        mapView = null;
    }

    private void loadAirport(String venueId) {
        final RelativeLayout rl = new RelativeLayout( this );

        AirportDatabase.OnLoadAirportAndMapListeners listeners = new AirportDatabase.OnLoadAirportAndMapListeners();
        listeners.loadedInitialViewListener = new AirportDatabase.OnLoadedInitialViewListener() {
            @Override public void onLoadedInitialView(View view) {
                ViewGroup parent = (ViewGroup) view.getParent();
                if (parent != null) {
                    parent.removeView(view);
                }

                view.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT));
                rl.addView(view);
                setContentView(rl);
            }
        };
        listeners.loadCompletedListener = new AirportDatabase.OnLoadCompletedListener() {

            @Override public void onLoadCompleted(Airport _airport, Map _map, final MapView _mapView,
                                                  Floor floor, Marker marker) {
                mapView = _mapView;
                airport = _airport;
                resetMapAfter10Seconds();
            }
        };

        airportDatabase.loadAirportAndMap(venueId, "", listeners);
    }

    /* After the Java Utilities Timer counts 10,000 milliseconds, the Venue's center Position and the Venue's radius
    *  are used to set the Map's setRadius and Map's setCenterPosition.
    * */
    private void resetMapAfter10Seconds() {
        new java.util.Timer().schedule( new java.util.TimerTask() {
            public void run() {
                Position currentVenueCenter = airport.getVenueCenter();
                mapView.setCenterPosition(currentVenueCenter);

                double currentVenueRadius = airport.getVenueRadius();
                mapView.setRadius(currentVenueRadius);
            }
        }, 10000 );

    }
}
