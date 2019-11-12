package com.FinalProject.TomAlon.YardenShaish.Fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.FinalProject.TomAlon.YardenShaish.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Objects;

import javax.annotation.Nullable;

public class InstallersMapFragment extends Fragment implements OnMapReadyCallback {
    private static final String TAG = InstallersMapFragment.class.getSimpleName();
    private static final String USERS_TAG = "users";
    private static final String IS_ADMIN_TAG = "isAdmin";

    // firebase
    private FirebaseFirestore mFirestoreDB = FirebaseFirestore.getInstance();

    // maps and markers
    private HashMap<String, Marker> mMarkers = new HashMap<>();
    private MapView mMapView;
    private GoogleMap mMap;

    public InstallersMapFragment() {
        // Required empty public constructor
    }

    public static InstallersMapFragment newInstance() {
        return new InstallersMapFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this shiftFragments
        View rootView = inflater.inflate(R.layout.fragment_map, container, false);
        mMapView = rootView.findViewById(R.id.map);
        mMapView.getMapAsync(this);
        mMapView.onCreate(savedInstanceState);
        mMapView.onResume();
        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        mMapView.onSaveInstanceState(outState);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMaxZoomPreference(16);
        subscribeToUpdates();
    }

    private void subscribeToUpdates() {
        mFirestoreDB.collection(USERS_TAG).whereEqualTo(IS_ADMIN_TAG, false)
                .addSnapshotListener(Objects.requireNonNull(getActivity()), new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.d(TAG, e.getMessage());
                            return;
                        }
                        if (queryDocumentSnapshots != null) {
                            setMarker(queryDocumentSnapshots);
                        }
                    }
                });
    }


    private void setMarker(QuerySnapshot queryDocumentSnapshots) {
        // When a location update is received, put or update
        // its value in mMarkers, which contains all the markers
        // for locations received, so that we can build the
        // boundaries required to show them all on the map at once
        for (DocumentChange documentChange : queryDocumentSnapshots.getDocumentChanges()) {
            String name = Objects.requireNonNull(documentChange.getDocument()
                    .getData().get("name")).toString();
            GeoPoint geoPoint = (GeoPoint) Objects.requireNonNull(documentChange.getDocument()
                    .getData().get("location"));
            LatLng location = new LatLng(geoPoint.getLatitude(), geoPoint.getLongitude());
            switch (documentChange.getType()) {
                case ADDED:
                    mMarkers.put(name, mMap.addMarker(new MarkerOptions().title(name).position(location)));
                    break;
                case MODIFIED:
                    Objects.requireNonNull(mMarkers.get(name)).setPosition(location);
                    break;
                case REMOVED:
                    Log.d(TAG, "installer removed");
                    mMarkers.remove(name);
                    break;
            }
        }
        markersModified();
    }

    private void markersModified() {
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (Marker marker : mMarkers.values()) {
            builder.include(marker.getPosition());
            marker.showInfoWindow();
        }
        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 300));
    }

}
