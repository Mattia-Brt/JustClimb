package com.example.tirociniojustclimb;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import com.example.tirociniojustclimb.databinding.ActivityEditorModeBinding;


import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class editor_mode extends FragmentActivity implements GoogleMap.OnInfoWindowCloseListener, GoogleMap.OnMarkerClickListener,OnMapReadyCallback {

    private GoogleMap mMap;
    private ActivityEditorModeBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityEditorModeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    public Button crag_detail;
    public Button add_crag;
    public Button confirm_position;
    public Button settings_btn;
    public TextView coordinates_viewer;
    public double new_crag_lat;
    public double new_crag_lon;
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

        coordinates_viewer = (TextView) findViewById(R.id.coordinates_viewer);
        coordinates_viewer.setVisibility(View.INVISIBLE);

        add_crag = (Button) findViewById(R.id.add_crag_btn);
        confirm_position = (Button) findViewById(R.id.confirm_position);
        confirm_position.setClickable(false);
        confirm_position.setVisibility(View.INVISIBLE);
        settings_btn = findViewById(R.id.settings_btn);

        add_crag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Marker marker=mMap.addMarker(new MarkerOptions()
                        .position(new LatLng(mMap.getCameraPosition().target.latitude,mMap.getCameraPosition().target.longitude))
                        .draggable(true));
                marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
                marker.setTag(-1);
                add_crag.setVisibility(View.INVISIBLE);
                add_crag.setClickable(false);
                confirm_position.setClickable(true);
                confirm_position.setVisibility(View.VISIBLE);
                coordinates_viewer.setVisibility(View.VISIBLE);
                new_crag_lat=marker.getPosition().latitude;
                new_crag_lon=marker.getPosition().longitude;
                coordinates_viewer.setText("Lat:" + Double.toString(new_crag_lat) + " Lon:" + Double.toString(new_crag_lon));
            }
        });

        confirm_position.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(),new_crag.class);      //new_crag è la pagina di arrivo
                i.putExtra("lat",new_crag_lat);
                i.putExtra("lon",new_crag_lon);
                startActivity(i);
            }
        });

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

        mMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(@NonNull @NotNull Marker marker) {

            }

            @Override
            public void onMarkerDrag(@NonNull @NotNull Marker marker) {
                new_crag_lat=marker.getPosition().latitude;
                new_crag_lon=marker.getPosition().longitude;
                coordinates_viewer.setText("Lat:" + Double.toString(new_crag_lat) + " Lon:" + Double.toString(new_crag_lon));
            }

            @Override
            public void onMarkerDragEnd(@NonNull @NotNull Marker marker) {
                mMap.animateCamera(CameraUpdateFactory.newLatLng(marker.getPosition()));
                new_crag_lat=marker.getPosition().latitude;
                new_crag_lon=marker.getPosition().longitude;
                coordinates_viewer.setText("Lat:" + Double.toString(new_crag_lat) + " Lon:" + Double.toString(new_crag_lon));
            }
        });


        settings_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), Settings.class);
                intent.putExtra("editable", "true");
                startActivity(intent);
            }
        });

    }

    public void getCrags(editor_mode.EditorFirestoreCallback firestoreCallback){
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

    private interface EditorFirestoreCallback{
        void onCallback(List<String> list);
    }

    public void move_to_info_crag(){
        Intent intent = new Intent(this, crag_detail.class);
        intent.putExtra("id",id_crag_clicked);
        intent.putExtra("editable","true");
        startActivity(intent);
    }

    @Override
    public boolean onMarkerClick(final Marker marker) {
        if(!marker.getTag().toString().equals("-1")){
            Log.i("Info",marker.getTag().toString());
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
