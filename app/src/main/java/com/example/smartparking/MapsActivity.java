package com.example.smartparking;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.smartparking.api.ApiClient;
import com.example.smartparking.model.ParkingLot;
import org.osmdroid.config.Configuration;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Main activity that shows OpenStreetMap with parking locations
 * 100% FREE - No API key needed!
 */
public class MapsActivity extends AppCompatActivity {

    private MapView mapView;  // This is the OpenStreetMap object
    private Handler refreshHandler;  // For auto-refresh
    private Runnable refreshRunnable;  // Task to run every 5 seconds
    private boolean isFirstLoad = true;  // To track first load

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Configure OSMDroid (OpenStreetMap library)
        Configuration.getInstance().load(this, getSharedPreferences("osmdroid", Context.MODE_PRIVATE));

        setContentView(R.layout.activity_maps);  // Load the XML layout

        // Get the map from XML
        mapView = findViewById(R.id.map);

        // Enable zoom controls
        mapView.setBuiltInZoomControls(true);
        mapView.setMultiTouchControls(true);

        // Set initial position to Paris, France
        GeoPoint startPoint = new GeoPoint(48.8566, 2.3522);
        mapView.getController().setZoom(12.0);  // Zoom level
        mapView.getController().setCenter(startPoint);

        // Setup auto-refresh every 5 seconds
        setupAutoRefresh();

        // Load parking data from backend
        loadParkingData();
    }

    /**
     * Setup automatic refresh every 5 seconds
     */
    private void setupAutoRefresh() {
        refreshHandler = new Handler();
        refreshRunnable = new Runnable() {
            @Override
            public void run() {
                // Reload parking data
                loadParkingData();

                // Schedule next refresh in 5 seconds
                refreshHandler.postDelayed(this, 5000);
            }
        };

        // Start auto-refresh after 5 seconds (first load is immediate)
        refreshHandler.postDelayed(refreshRunnable, 5000);
    }

    /**
     * Simple method to fetch parking data from backend
     * First gets lots, then gets slots and combines them
     */
    private void loadParkingData() {
        // Show loading message only on first load
        if (isFirstLoad) {
            Toast.makeText(this, "Loading parking data...", Toast.LENGTH_SHORT).show();
        }

        // First, get parking lots
        ApiClient.getParkingApi().getParkingLots().enqueue(new Callback<List<ParkingLot>>() {
            @Override
            public void onResponse(Call<List<ParkingLot>> call, Response<List<ParkingLot>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    final List<ParkingLot> parkingLots = response.body();

                    // Now get parking slots
                    ApiClient.getParkingApi().getParkingSlots().enqueue(new Callback<List<com.example.smartparking.model.ParkingSlot>>() {
                        @Override
                        public void onResponse(Call<List<com.example.smartparking.model.ParkingSlot>> call, Response<List<com.example.smartparking.model.ParkingSlot>> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                List<com.example.smartparking.model.ParkingSlot> allSlots = response.body();

                                // Add all slots to the first parking lot
                                if (!parkingLots.isEmpty()) {
                                    parkingLots.get(0).setParkingSlots(allSlots);
                                }

                                // Show success message only on first load
                                if (isFirstLoad) {
                                    Toast.makeText(MapsActivity.this,
                                            "Found " + parkingLots.size() + " parking lot(s) with " + allSlots.size() + " slots",
                                            Toast.LENGTH_SHORT).show();
                                    isFirstLoad = false;
                                }

                                // Add markers to map
                                addMarkersToMap(parkingLots);
                            }
                        }

                        @Override
                        public void onFailure(Call<List<com.example.smartparking.model.ParkingSlot>> call, Throwable t) {
                            Toast.makeText(MapsActivity.this, "Error loading slots", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    Toast.makeText(MapsActivity.this, "Error loading lots", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<ParkingLot>> call, Throwable t) {
                Toast.makeText(MapsActivity.this,
                        "Network error: " + t.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    /**
     * Simple method to add parking markers to the map
     * Clears old markers and adds new ones with updated data
     */
    private void addMarkersToMap(List<ParkingLot> parkingLots) {
        // Clear all existing markers (remove old data)
        mapView.getOverlays().clear();

        // Loop through each parking lot
        for (ParkingLot lot : parkingLots) {
            // Create a position on the map
            GeoPoint position = new GeoPoint(lot.getLatitude(), lot.getLongitude());

            // Create a marker
            Marker marker = new Marker(mapView);
            marker.setPosition(position);
            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);

            // Set marker title with live parking info (always visible!)
            String title = lot.getName() + "\n" + lot.getFreeSlots() + " free / " + lot.getTotalSlots() + " total";
            marker.setTitle(title);

            // Set detailed snippet (shown when clicked)
            String snippet = "Updated: " + new java.text.SimpleDateFormat("HH:mm:ss").format(new java.util.Date());
            marker.setSnippet(snippet);

            // Add marker to map
            mapView.getOverlays().add(marker);

            // Show info bubble automatically (always visible!)
            marker.showInfoWindow();

            // Move to first parking lot (only on first load)
            if (parkingLots.indexOf(lot) == 0 && isFirstLoad) {
                mapView.getController().setCenter(position);
                mapView.getController().setZoom(15.0);
            }
        }

        // Refresh the map to show markers
        mapView.invalidate();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();  // Important for map lifecycle
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();  // Important for map lifecycle

        // Stop auto-refresh when app goes to background
        if (refreshHandler != null && refreshRunnable != null) {
            refreshHandler.removeCallbacks(refreshRunnable);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Clean up when app is closed
        if (refreshHandler != null && refreshRunnable != null) {
            refreshHandler.removeCallbacks(refreshRunnable);
        }
    }
}
