package com.example.tirociniojustclimb;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null) {
            Toast.makeText(getApplicationContext(), "Automatically Logged", Toast.LENGTH_SHORT).show();
            changeView(editor_mode.class);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button viewer_mode_button = (Button) findViewById(R.id.viewer_mode_btn);
        Button login_btn = (Button) findViewById(R.id.login_btn);
        Button register_btn = (Button) findViewById(R.id.register_btn);

        viewer_mode_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //System.out.println("Entered on click function");
                changeView(viewer_mode.class);
            }
        });
        login_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeView(LogIn.class);
            }
        });
        register_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeView(Registration.class);
            }
        });
    }

    public void changeView(Class<?> cls) {
        //System.out.println("Entered changeView function");
        Intent intent = new Intent(this, cls);
        //System.out.println("Intent created");
        startActivity(intent);
    }
}