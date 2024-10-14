package com.example.tirociniojustclimb;

import android.content.Intent;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class add_notes extends AppCompatActivity {          //deve aggiungere la nuova nota alla collezione, nulla di più

    public String id_routeClicked, id_sectorClicked, id_cragClicked, routeName;
    public TextView route_name;
    EditText newNote;
    Button createNote;

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        //grafica pagina
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_notes);
        route_name = findViewById(R.id.route_name_note);
        newNote = findViewById(R.id.note_txt);
        createNote = findViewById(R.id.add_new_note_button);

        id_routeClicked = getIntent().getStringExtra("idRoute");
        id_sectorClicked = getIntent().getStringExtra("idSector");
        id_cragClicked = getIntent().getStringExtra("idCrag");
        routeName = getIntent().getStringExtra("routeName");

        route_name.setText(getIntent().getStringExtra("routeName"));

        createNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                        if (newNote.getText().length() < 1){
                            //ritorna alla pagina precedente;
                            move_to_note_detail(routeName, id_routeClicked, id_sectorClicked, id_cragClicked);
                        }
                        else {
                            if(newNote.getText().length() > 60){
                                Toast.makeText(getApplicationContext(), R.string.toast_addNote_too_long, Toast.LENGTH_LONG).show();
                            }
                            else{
                                //add new note e reindirizza la pagina

                                //date
                                Date c = Calendar.getInstance().getTime();
                                SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault());
                                String formattedDate = df.format(c);

                                //creo l'obj nota per il DB
                                Map<String, Object> newNotaDB = new HashMap<>();
                                newNotaDB.put("date", formattedDate);
                                newNotaDB.put("note", newNote.getText().toString());
                                newNotaDB.put("id_creator", user.getUid());

                                db.collection("crags").document(id_cragClicked).collection("sectors").
                                        document(id_sectorClicked).collection("routes").document(id_routeClicked).collection("notes").add(newNotaDB).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                    @Override
                                    public void onComplete(@NonNull @NotNull Task<DocumentReference> task) {
                                        move_to_note_detail(routeName, id_routeClicked, id_sectorClicked, id_cragClicked);
                                    }
                                });
                            }
                        }
            }
        });

    }

    /**
     * 
     * @param nomeVia
     * @param idRoute
     * @param idSector
     * @param idCrag
     */
    public void move_to_note_detail (String nomeVia, String idRoute, String idSector, String idCrag){
        Intent intent = new Intent(this, note_detail.class);
        intent.putExtra("routeName",nomeVia);
        intent.putExtra("idRoute", idRoute);
        intent.putExtra("idSector", idSector);
        intent.putExtra("idCrag", idCrag);
        intent.putExtra("editable","true");
        startActivity(intent);
    }

}




// funzionante
    /*public void  AggiuntaNotaProva(){

        Date c = Calendar.getInstance().getTime();
        SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault());
        String formattedDate = df.format(c);
        String testoNota = "prova della nuova nota";

        Note newnota = new Note(formattedDate, testoNota);      //ho la data in obj java inutile

        Map<String, Object> newNotaDB = new HashMap<>();        //ho la data in obj DB
        newNotaDB.put("date", formattedDate);
        newNotaDB.put("note", testoNota);

        //salvo la nota nel DB
        db.collection("crags").document(id_cragClicked).collection("sectors").
                document(id_sectorClicked).collection("routes").document(id_routeClicked).collection("notes").add(newNotaDB).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
            @Override
            public void onComplete(@NonNull @NotNull Task<DocumentReference> task) {

            }
        });

    }*/