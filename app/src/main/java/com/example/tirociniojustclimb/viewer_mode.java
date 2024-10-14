package com.example.tirociniojustclimb;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.example.tirociniojustclimb.databinding.ActivityViewerModeBinding;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


public class viewer_mode extends FragmentActivity implements GoogleMap.OnInfoWindowCloseListener, GoogleMap.OnMarkerClickListener,OnMapReadyCallback {

    private GoogleMap mMap;
    private ActivityViewerModeBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityViewerModeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    public Button crag_detail;
    public String id_crag_clicked;
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMarkerClickListener((GoogleMap.OnMarkerClickListener) this);
        mMap.setOnInfoWindowCloseListener((GoogleMap.OnInfoWindowCloseListener)this);

        LatLng startPosition = new LatLng(46.07497304869112, 11.127758033677374);

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(startPosition,10));

        crag_detail = (Button) findViewById(R.id.crag_detail_btn);
        crag_detail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                move_to_info_crag();
            }
        });
        crag_detail.setClickable(false); crag_detail.setVisibility(View.INVISIBLE);

        getCrags(cragsIds -> {
            Iterator iter = cragsIds.iterator();
            while(iter.hasNext()){
                String id = iter.next().toString();
                db.collection("crags").document(id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull @NotNull Task<DocumentSnapshot> task) {
                        mMap.addMarker(new MarkerOptions().position(
                                new LatLng(Double.parseDouble(task.getResult().getString("lat")),
                                        Double.parseDouble(task.getResult().getString("lon"))))
                                .title(task.getResult().getString("name"))).setTag(task.getResult().getId());
                    }
                });
            }
        });

        Button settings_btn = findViewById(R.id.settings_btn);

        settings_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(),Settings.class);
                startActivity(i);
            }
        });

    }

    public void getCrags(FirestoreCallback firestoreCallback){
        List<String> ids = new ArrayList<String>();
        db.collection("crags").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull @NotNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    for(QueryDocumentSnapshot document: task.getResult()){
                        ids.add(document.getId());
                        firestoreCallback.onCallback(ids);
                    }
                }else{
                    Log.i("Info","Task not successful");
                }
            }
        });
    }

    private interface FirestoreCallback{
        void onCallback(List<String> list);
    }

    public void move_to_info_crag(){
        Intent intent = new Intent(this, crag_detail.class);
        intent.putExtra("id",id_crag_clicked);
        startActivity(intent);
    }

    @Override
    public boolean onMarkerClick(final Marker marker) {
        if(!marker.getTag().toString().equals("-1")){
            //Log.i("Info",marker.getTag().toString());
            crag_detail.setClickable(true);
            crag_detail.setVisibility(View.VISIBLE);
            marker.showInfoWindow();
            id_crag_clicked = marker.getTag().toString();
        }
        return true;
    }

    @Override
    public void onInfoWindowClose(Marker marker) {
        crag_detail.setClickable(false); crag_detail.setVisibility(View.INVISIBLE);
        marker.hideInfoWindow();
    }
}

