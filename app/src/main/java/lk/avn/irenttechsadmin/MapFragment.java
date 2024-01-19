package lk.avn.irenttechsadmin;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.SearchView;
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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

import lk.avn.irenttechsadmin.custom.CustomLoading;

public class MapFragment extends Fragment implements OnMapReadyCallback {

    private static final String TAG = MapFragment.class.getName();

    private GoogleMap mMap;
    private Location currentLocation;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 10;
    private static View fragmentView;
    private Marker marker_current, marker_pin;
    private static String warning_name ,email;
    private Polyline polyline;
    private static CustomLoading customLoading;
    private static FirebaseFirestore fireStore;
    private FusedLocationProviderClient fusedLocationProviderClient;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_map, container, false);
    }


    @Override
    public void onViewCreated(@NonNull View fragment, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(fragment, savedInstanceState);
        fragmentView = fragment;
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getActivity());
        SharedPreferences preferences = getActivity().getSharedPreferences("AuthActivity", getContext().MODE_PRIVATE);
        email = preferences.getString("EMAIL", null);
        customLoading = new CustomLoading(getActivity());
        fireStore = FirebaseFirestore.getInstance();

//        if (getActivity() instanceof HomeActivity) {
//            HomeActivity ha = (HomeActivity) getActivity();
//            ha.findViewById(R.id.bottomBar).setVisibility(View.GONE);
//            TextView t_name = ha.findViewById(R.id.toolbar_name);
//            t_name.setText("MAP");
//            MaterialToolbar toolbar = ha.findViewById(R.id.toolbar);
//            toolbar.setNavigationIcon(null);
//            ImageButton backbtn = ha.findViewById(R.id.back_btn);
//            backbtn.setVisibility(View.VISIBLE);
//            backbtn.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    if (getActivity() != null) {
//                        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
//                        transaction.replace(R.id.frame_layouts,new ProfileFragment());
//                        transaction.commit();                    }
//                }
//            });
//        }

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(MapFragment.this);
        }

    }

    private boolean checkPermission() {
        return ActivityCompat.checkSelfPermission(requireActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(requireActivity(), android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestLocationPermissions() {
        ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
    }

    @SuppressLint("MissingPermission")
    private void getLastLocation() {
        if (checkPermission()) {
            Task<Location> task = fusedLocationProviderClient.getLastLocation();
            task.addOnSuccessListener(new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if (location != null) {
                        currentLocation = location;
                        LatLng latLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
                        mMap.addMarker(new MarkerOptions().position(latLng).title("My Location"));
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));

                    }
                }
            });
        } else {
            requestLocationPermissions();
        }

    }

    // ...

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);

        MapStyleOptions styleOptions = MapStyleOptions.loadRawResourceStyle(getContext(), R.raw.map_design);
        mMap.setMapStyle(styleOptions);

        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(@NonNull LatLng end) {
                // Print the latitude and longitude in the log
                Log.d(TAG, "Latitude: " + end.latitude + ", Longitude: " + end.longitude);

                warning_name = "Do you wnt to Continue";
                new MapFragment.WarningDialog(end.latitude, end.longitude,getActivity().getSupportFragmentManager()).show(getActivity().getSupportFragmentManager(), "Error");

                if (marker_pin == null) {
                    MarkerOptions markerOptions = new MarkerOptions();
                    markerOptions.position(end);
                    marker_pin = mMap.addMarker(markerOptions);
                } else {
                    marker_pin.setPosition(end);
                }



//                LatLng start = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
//                getDirection(start, end);
            }
        });

        if (checkPermission()) {
//            getLastLocation();
        } else {
            requestLocationPermissions();
        }
    }

// ...


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLastLocation();
            } else {
                Snackbar.make(fragmentView.findViewById(R.id.fraimId), "Location permission denied", Snackbar.LENGTH_INDEFINITE)
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

//    public void getDirection(LatLng start, LatLng end){
//        Retrofit retrofit = new Retrofit.Builder()
//                .baseUrl("https://maps.googleapis.com/maps/api/directions/")
//                .addConverterFactory(GsonConverterFactory.create())
//                .build();
//
//        DirectionApi directionApi = retrofit.create(DirectionApi.class);
//
//        String origin = start.latitude+","+start.longitude;
//        String destination = end.latitude+","+end.longitude;
//        String key = "AIzaSyAaScpSGikMXnzETHUoLJpyI0QHlUtJvzQ";
//
//        Call<JsonObject> apiJson = directionApi.getJson(origin, destination, true, key);
//        apiJson.enqueue(new Callback<JsonObject>() {
//            @Override
//            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
//                if (response.isSuccessful()) {
//                    JsonObject body = response.body();
//                    JsonArray routes = body.getAsJsonArray("routes");
//
//                    if (routes != null && routes.size() > 0) {
//                        JsonObject route = routes.get(0).getAsJsonObject();
//                        JsonObject overviewPolyline = route.getAsJsonObject("overview_polyline");
//
//                        List<LatLng> points = PolyUtil.decode(overviewPolyline.get("points").getAsString());
//
//                        new Handler(Looper.getMainLooper()).post(new Runnable() {
//                            @Override
//                            public void run() {
//                                if (polyline == null) {
//                                    PolylineOptions polylineOptions = new PolylineOptions();
//                                    polylineOptions.width(20);
//                                    polylineOptions.color(getContext().getColor(android.R.color.holo_blue_dark));
//                                    polylineOptions.addAll(points);
//                                    polyline = mMap.addPolyline(polylineOptions);
//                                } else {
//                                    polyline.setPoints(points);
//                                }
//                            }
//                        });
//                    } else {
//                        Log.e("MapFragment", "No routes found in the response");
//                    }
//                } else {
//                    Log.e("MapFragment", "Error in the API response: " + response.message());
//                }
//            }
//
//
//            @Override
//            public void onFailure(Call<JsonObject> call, Throwable t) {
//
//            }
//        });
//    }

//    public static void addLocationData(Double latitude, Double longitude, FragmentManager fragmentManager){
//        SaveLocation saveLocation = new SaveLocation(email,latitude,longitude);
//        customLoading.show();
//        DocumentReference locationDocument = fireStore.collection("Location").document(email);
//        locationDocument.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
//            @Override
//            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
//                if (task.isSuccessful()) {
//                    DocumentSnapshot document = task.getResult();
//                    fireStore.collection("Location").document(email).set(saveLocation).addOnSuccessListener(new OnSuccessListener<Void>() {
//                        @Override
//                        public void onSuccess(Void unused) {
//                            customLoading.dismiss();
//                            FragmentTransaction transaction = fragmentManager.beginTransaction();
//                            transaction.replace(R.id.frame_layouts,new ProfileFragment());
//                            transaction.commit();
//                        }
//                    }).addOnFailureListener(new OnFailureListener() {
//                        @Override
//                        public void onFailure(@NonNull Exception e) {
//                            customLoading.dismiss();
//                            Log.e(TAG, "Error response");
//
//                        }
//                    });
//                }
//            }
//        }).addOnFailureListener(new OnFailureListener() {
//            @Override
//            public void onFailure(@NonNull Exception e) {
//                customLoading.dismiss();
//                Log.e(TAG, "Error response");
//
//            }
//        });
//    }

    public static class WarningDialog extends DialogFragment {
        private Double longitude;
        private Double latitude;
        private FragmentManager fragmentManager;

        public WarningDialog(Double latitude, Double longitude,FragmentManager fragmentManager) {
            this.latitude = latitude;
            this.longitude = longitude;
            this.fragmentManager = fragmentManager;

        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.RoundedCornersDialog);
            LayoutInflater inflater = requireActivity().getLayoutInflater();

            View customView = inflater.inflate(R.layout.warning_message, null);


            TextView messageTextView = customView.findViewById(R.id.warning_dialog_message);
            messageTextView.setText(warning_name);

            Button ContinueButton = customView.findViewById(R.id.warning_dialog_ok_button);
            ContinueButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
//                    addLocationData(latitude, longitude,fragmentManager);
                    dismiss();
                }
            });

            ContinueButton.setText("Continue");

            Button cancelButton = customView.findViewById(R.id.warning_dialog_cancel_button);
            cancelButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dismiss();
                }
            });
            cancelButton.setText("No");

            builder.setView(customView);
            return builder.create();
        }
    }

}