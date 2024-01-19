package lk.avn.irenttechsadmin;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.maps.android.PolyUtil;

import java.util.List;

import lk.avn.irenttechsadmin.custom.CustomLoading;
import lk.avn.irenttechsadmin.service.DirectionApi;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String TAG = MapActivity.class.getName();

    private GoogleMap mMap;
    private Location currentLocation;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 10;
    private Marker marker_current, marker_pin;
    private static String  email;
    private static Double longitude;
    private static Double latitude;
    private Polyline polyline;
    private static CustomLoading customLoading;
    private static FirebaseFirestore fireStore;
    private FusedLocationProviderClient fusedLocationProviderClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        customLoading = new CustomLoading(this);
        fireStore = FirebaseFirestore.getInstance();

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        Intent receivedIntent = getIntent();
        if (receivedIntent != null) {
            email = receivedIntent.getStringExtra("EMAIL_KEY");
            System.out.println(email);
        }

        fireStore.collection("User").document(email).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                if (value.exists()) {
                    latitude = value.getDouble("latitude");
                    longitude = value.getDouble("longitude");
                }
            }
        });

        }


    private boolean checkPermission() {
        return ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestLocationPermissions() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
    }

    @SuppressLint("MissingPermission")
    private void getLastLocation() {
        Task<Location> task = fusedLocationProviderClient.getLastLocation();
        task.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    currentLocation = location;

                    LatLng start = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());

                    LatLng end = new LatLng(latitude, longitude);

                    mMap.addMarker(new MarkerOptions().position(start).title("Start"));

                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(start, 15));

                    getDirection(start, end);
                }
            }
        });
    }


    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);

        MapStyleOptions styleOptions = MapStyleOptions.loadRawResourceStyle(this, R.raw.map_design);
        mMap.setMapStyle(styleOptions);

        if (checkPermission()) {
            getLastLocation();
        } else {
            requestLocationPermissions();
        }
    }



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLastLocation();
            } else {
                Snackbar.make(findViewById(R.id.fraimId), "Location permission denied", Snackbar.LENGTH_INDEFINITE)
                        .setAction("Settings", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                                        .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                            }
                        }).show();
            }
        }
    }

    public void getDirection(LatLng start, LatLng end) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://maps.googleapis.com/maps/api/directions/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        DirectionApi directionApi = retrofit.create(DirectionApi.class);

        double destinationLatitude = latitude != null ? latitude : 0.0;
        double destinationLongitude = longitude != null ? longitude : 0.0;
        LatLng destination = new LatLng(destinationLatitude, destinationLongitude);

        String origin = start.latitude + "," + start.longitude;
        String destinationStr = destination.latitude + "," + destination.longitude;
        String key = "AIzaSyAaScpSGikMXnzETHUoLJpyI0QHlUtJvzQ";

        Log.d(TAG, "Origin: " + origin);
        Log.d(TAG, "Destination: " + destinationStr);
        Log.d(TAG, "API Key: " + key);

        Call<JsonObject> apiJson = directionApi.getJson(origin, destinationStr, true, key);
        apiJson.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful()) {
                    JsonObject body = response.body();
                    Log.d(TAG, "Direction API Response: " + body.toString());

                    JsonArray routes = body.getAsJsonArray("routes");

                    if (routes != null && routes.size() > 0) {
                        JsonObject route = routes.get(0).getAsJsonObject();
                        JsonObject overviewPolyline = route.getAsJsonObject("overview_polyline");
                        String points = overviewPolyline.get("points").getAsString();

                        List<LatLng> decodedPolyline = PolyUtil.decode(points);

                        // Draw polyline on the map
                        if (decodedPolyline != null && decodedPolyline.size() > 0) {
                            if (polyline != null) {
                                polyline.remove(); // Remove existing polyline
                            }

                            PolylineOptions polylineOptions = new PolylineOptions()
                                    .addAll(decodedPolyline)
                                    .color(ContextCompat.getColor(MapActivity.this, R.color.green))
                                    .width(10);
                            polyline = mMap.addPolyline(polylineOptions);
                        } else {
                            Log.e(TAG, "No points in the polyline");
                        }
                    } else {
                        Log.e(TAG, "No routes found in the response");
                    }
                } else {
                    Log.e(TAG, "Error in the API response: " + response.message());
                }
            }



            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Log.e(TAG, "Error in API call: " + t.getMessage());
            }
        });
    }


}