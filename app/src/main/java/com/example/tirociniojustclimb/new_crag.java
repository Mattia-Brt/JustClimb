package com.example.tirociniojustclimb;


import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class new_crag extends AppCompatActivity {
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    public Button cancel;
    public Button create_crag;
    public EditText name;
    public EditText description;
    public EditText lat;
    public EditText lon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_crag);

        create_crag = findViewById(R.id.create_crag);
        cancel = findViewById(R.id.cancel_new_crag);
        name = (EditText) findViewById(R.id.crag_name);
        description = (EditText) findViewById(R.id.crag_description);
        lat = (EditText) findViewById(R.id.crag_lat);
        lon = (EditText) findViewById(R.id.crag_lon);

        lat.setText(Double.toString(getIntent().getDoubleExtra("lat",0.0)));
        lon.setText(Double.toString(getIntent().getDoubleExtra("lon",0.0)));

        create_crag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Editable name_string = name.getText();
                Editable description_string = description.getText();
                Editable lat_string = lat.getText();
                Editable lon_string = lon.getText();

                if (name_string.length() > 0 && description_string.length() > 0 && lat_string.length() > 0 && lon_string.length() > 0) {
                    addCrag(name_string.toString(),lat_string.toString(),lon_string.toString(),description_string.toString());
                    Intent i = new Intent(getApplicationContext(),editor_mode.class);
                    startActivity(i);
                } else {
                    Log.i("Info", "Some arguments are missing");
                }
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                move_to_Map();
            }
        });
    }

    public void addCrag(String name,String lat,String lon,String descr){
        Map<String, Object> docData = new HashMap<>();
        docData.put("name", name);
        docData.put("lat",lat);
        docData.put("lon",lon);
        docData.put("description",descr);
        docData.put("counterDelete", "0");
        db.collection("crags").add(docData);
    }

    /**
     *
     */
    public void move_to_Map (){
        startActivity(new Intent(this, editor_mode.class));

    }
}
