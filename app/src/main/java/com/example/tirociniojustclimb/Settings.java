package com.example.tirociniojustclimb;


import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.nfc.Tag;
import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Settings extends AppCompatActivity {

    public Button logout;
    public Button delteAccount;
    public Button changePsw;

    FirebaseAuth auth = FirebaseAuth.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {                    //TODO utente non loggato non vede questi button, ne ha uno "crea account" che limanda alla main

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        logout = findViewById(R.id.logout_btn);
        delteAccount = findViewById(R.id.DeleteAccount);
        changePsw = findViewById(R.id.EditPsw);

        delteAccount.setClickable(false);
        delteAccount.setVisibility(View.INVISIBLE);
        changePsw.setClickable(false);
        changePsw.setVisibility(View.INVISIBLE);

        if(getIntent().getStringExtra("editable")!=null){
            delteAccount.setClickable(true);
            delteAccount.setVisibility(View.VISIBLE);
            changePsw.setClickable(true);
            changePsw.setVisibility(View.VISIBLE);
        }


        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                auth.signOut();
                startActivity(new Intent(getApplicationContext(),MainActivity.class));
            }
        });

        delteAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogConfirmDelete();
            }
        });

        changePsw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {dialogChiangePsw();}
        });
    }

    /**
     *Eliminazione account
     */
    public void dialogConfirmDelete(){
        AlertDialog.Builder alert = new AlertDialog.Builder(Settings.this);
        alert.setTitle("Are you sure you want to delete your account?");
        alert.setMessage("Your account can not be recovered.");
        alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ConfirmDelete();
            }
        });
        alert.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        AlertDialog alert_dialog = alert.create();
        alert_dialog.show();
    }
    public void ConfirmDelete(){
        AlertDialog.Builder alert = new AlertDialog.Builder(Settings.this);
        alert.setTitle("Enter your password to confirm ");

        final Context context = alert.getContext();
        final LayoutInflater inflater = LayoutInflater.from(context);
        final View dialog = inflater.inflate(R.layout.dialog_confirm_password, null);

        alert.setView(dialog);

        EditText psw =  dialog.findViewById(R.id.your_password);

        alert.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), psw.getText().toString());
                user.reauthenticate(credential)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    user.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            auth.signOut();
                                            startActivity(new Intent(getApplicationContext(), MainActivity.class));
                                            Toast.makeText(getApplicationContext(), "Account Delete", Toast.LENGTH_SHORT).show();
                                            Log.d(TAG, "User delete");
                                        }
                                    });

                                } else {
                                    Toast.makeText(getApplicationContext(), "Password is not correct", Toast.LENGTH_SHORT).show();
                                    Log.d(TAG, "osw not correct");
                                }
                            }
                        });
            }
        });
        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        AlertDialog alert_dialog = alert.create();
        alert_dialog.show();
    }

    /**
     *cambiare la psw con verifica (uguale per la mail)
     */
    public void dialogChiangePsw(){
        AlertDialog.Builder alert = new AlertDialog.Builder(Settings.this);
        alert.setTitle("Are you sure you want to change you password?");
        alert.setMessage("Type here your credential.");

        final Context context = alert.getContext();
        final LayoutInflater inflater = LayoutInflater.from(context);
        final View dialog = inflater.inflate(R.layout.dialog_confirm_credential, null);

        alert.setView(dialog);

        EditText current_psw =  dialog.findViewById(R.id.Current_password);
        EditText psw = dialog.findViewById(R.id.New_password);
        EditText confirm_psw= dialog.findViewById(R.id.Confirm_password);

        alert.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(current_psw.getText().length() > 0  && psw.getText().length() > 0 && confirm_psw.getText().length() > 0){          //se almeno i campi sono completi
                    FirebaseUser user = auth.getCurrentUser();
                    //controlla la vecchia psw
                    AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), current_psw.getText().toString());

                    user.reauthenticate(credential)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        // se la vecchia psw coincide allora faccio il match della nuova
                                        if(psw.getText().toString().equals(confirm_psw.getText().toString())){                     //match psw ok
                                            user.updatePassword(psw.getText().toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        Log.d(TAG, "Password updated");
                                                        Toast.makeText(getApplicationContext(), "Password changed succesfully", Toast.LENGTH_SHORT).show();
                                                    } else {
                                                        Log.d(TAG, "Error password not updated");
                                                        Toast.makeText(getApplicationContext(), "Almost 6 character", Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                            });
                                        }else {
                                            Toast.makeText(getApplicationContext(), "The two Password doesn't match", Toast.LENGTH_SHORT).show();
                                            Log.d(TAG, "psw not match ");
                                        }

                                    } else {
                                        Toast.makeText(getApplicationContext(), "Current password is not correct", Toast.LENGTH_SHORT).show();
                                        Log.d(TAG, "Current psw not correct");
                                    }
                                }
                            });
                } else {
                    Toast.makeText(getApplicationContext(), "Fill in all", Toast.LENGTH_SHORT).show();
                }

            }
        });
        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        AlertDialog alert_dialog = alert.create();
        alert_dialog.show();
    }

}
