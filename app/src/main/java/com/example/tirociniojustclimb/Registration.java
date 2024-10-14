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
import com.google.firebase.firestore.QuerySnapshot;

import org.jetbrains.annotations.NotNull;

public class Registration extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        FirebaseAuth auth = FirebaseAuth.getInstance();


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        TextView btn_to_login = findViewById(R.id.simpleButtonToLogin);
        EditText email = (EditText) findViewById(R.id.registration_editTextTextEmailAddress);
        EditText password = (EditText) findViewById(R.id.registration_editTextTextPassword);
        EditText confirm_password = (EditText) findViewById(R.id.registration_editTextTextConfirmPassword);

        Button registrationButton = (Button) findViewById((R.id.registration_button));

        registrationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(email.getText().length()>0 && password.getText().length()>0 && password.getText().toString().equals(confirm_password.getText().toString())) {

                    auth.createUserWithEmailAndPassword(email.getText().toString(),password.getText().toString()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull @NotNull Task<AuthResult> task) {
                            if(task.isSuccessful()){
                                Toast.makeText(getApplicationContext(),"Successfully registered!", Toast.LENGTH_SHORT).show();
                                changeView(editor_mode.class);
                            }else{
                                Log.i("Info",task.getException().toString());
                                Toast.makeText(getApplicationContext(), task.getException().toString(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

                }else{
                    Log.i("Info","Something wrong in params");
                    Toast.makeText(getApplicationContext(), "Invalid parameters", Toast.LENGTH_SHORT).show();
                }
            }
        });


        btn_to_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeView(LogIn.class);
            }
        });
    }

    public void changeView(Class<?> cls){
        //System.out.println("Entered changeView function");
        Intent intent = new Intent(this,cls);
        //System.out.println("Intent created");
        startActivity(intent);
    }
}

