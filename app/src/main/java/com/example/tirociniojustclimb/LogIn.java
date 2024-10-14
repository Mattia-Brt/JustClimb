package com.example.tirociniojustclimb;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.jetbrains.annotations.NotNull;

public class LogIn extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        FirebaseAuth auth = FirebaseAuth.getInstance();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);

        EditText email = (EditText) findViewById(R.id.email_editTextTextEmailAddress);
        EditText password = (EditText) findViewById(R.id.login_editTextTextPassword);
        Button loginButton = (Button) findViewById((R.id.login_button));

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(email.getText().length()>0 && password.getText().length() > 0) {
                    auth.signInWithEmailAndPassword(email.getText().toString(), password.getText().toString()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull @NotNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                changeView(editor_mode.class);
                            } else {
                                Log.i("Info", task.getException().toString());
                                Toast.makeText(getApplicationContext(), task.getException().toString(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }else{
                    Log.i("Info","invalid parameters");
                    Toast.makeText(getApplicationContext(), "Invalid parameters", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void changeView(Class<?> cls){
        //System.out.println("Entered changeView function");
        Intent intent = new Intent(this,cls);
        Toast.makeText(getApplicationContext(), "Successfully Logged!", Toast.LENGTH_SHORT).show();
        //System.out.println("Intent created");
        startActivity(intent);
    }
}

