package com.example.locuslabs.examplelibrary;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.locuslabs.sdk.configuration.Logger;
import com.locuslabs.sdk.maps.model.Airport;
import com.locuslabs.sdk.maps.model.AirportDatabase;
import com.locuslabs.sdk.maps.model.Circle;
import com.locuslabs.sdk.maps.model.Floor;
import com.locuslabs.sdk.maps.model.Map;
import com.locuslabs.sdk.maps.model.Marker;
import com.locuslabs.sdk.maps.model.Position;
import com.locuslabs.sdk.maps.model.Search;
import com.locuslabs.sdk.maps.model.SearchResult;
import com.locuslabs.sdk.maps.model.SearchResults;
import com.locuslabs.sdk.maps.view.MapView;

import java.util.Arrays;
import java.util.List;

/*
* This Activity loads a Map of the given Venue then
* search for all the POI closer to a fixed position
* and display on the screen
* */

public class ExampleSearchByProximity extends Activity {
    private static final String TAG = "ExampleSearchByProximity";
    private AirportDatabase airportDatabase;
    private MapView mapView;

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

            @Override public void onLoadCompleted(final Airport _airport, final Map _map, final MapView _mapView,
                                                  Floor floor, Marker marker) {
                mapView = _mapView;
                mapView.setOnReadyListener(new MapView.OnReadyListener() {
                    @Override
                    public void onReady() {
                        searchByProximity(_map,_airport);
                    }
                });
            }
        };
        airportDatabase.loadAirportAndMap(venueId, "", listeners);
    }

    private void searchByProximity(final Map map, Airport _airport){
        Logger.info("Ready");
        Search search = _airport.search();
        Search.OnProximitySearchWithTermsResultsListener searchListener = new Search.OnProximitySearchWithTermsResultsListener() {
            @Override
            public void onProximitySearchWithTermsResults(SearchResults searchResults, String s) {
                List<SearchResult> resultList = searchResults.getResults();
                Position firstPosition = resultList.get(0).getPosition();
                Position position = null;
                for (int i = 0 ; i< resultList.size();i++){
                    position = resultList.get(i).getPosition();
                    map.addCircle(new Circle.Options()
                            .position(position)
                            .radius(10.0)
                            .fillOpacity(0.3)
                            .fillColor(Color.RED));
                }
                mapView.setCenterPosition(firstPosition);
                mapView.setRadius(150);
            }
        };

        search.proximitySearchWithTerms(Arrays.asList("starbucks"), "sea-mainterminal-departures", 47.441316, -122.3041542, searchListener);
    }
}
