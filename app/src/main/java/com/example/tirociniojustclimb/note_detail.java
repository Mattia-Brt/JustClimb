package com.example.tirociniojustclimb;

import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
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
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class note_detail extends AppCompatActivity{

    public String id_routeClicked, id_sectorClicked, id_cragClicked, routeName;
    public TextView route_name;
    public Button edit;
    public TableLayout notes;
    public Button returnButton, grade;

    public Route route_object;
    public ArrayList<Note> noteList;
    public ArrayList<Grade> gradeList = new ArrayList<>();

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //grafica pagina
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_detail);
        route_name = findViewById(R.id.route_name);
        edit = findViewById(R.id.detail_edit2);
        notes = findViewById(R.id.list_of_notes);
        returnButton = findViewById(R.id.return_to_crag_detail);
        grade = findViewById(R.id.edit_grade_button);

        //salvo i danti passati dall'intent
        id_routeClicked = getIntent().getStringExtra("idRoute");
        id_sectorClicked = getIntent().getStringExtra("idSector");
        id_cragClicked = getIntent().getStringExtra("idCrag");
        routeName = getIntent().getStringExtra("routeName");


        route_name.setText(getIntent().getStringExtra("routeName"));

        /**
         * modo per bloccare l'utente non loggato
         */
        edit.setClickable(false);
        edit.setVisibility(View.INVISIBLE);
        grade.setClickable(false);
        grade.setVisibility(View.INVISIBLE);
        if(getIntent().getStringExtra("editable")!=null){
            edit.setClickable(true);
            edit.setVisibility(View.VISIBLE);
            grade.setClickable(true);
            grade.setVisibility(View.VISIBLE);
        }

        edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDialog();
            }
        });

        grade.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                voteGrade();
            }
        });

        returnButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                move_to_info_crag();
            }
        });

        route_object = new Route();
        /**
         * scarico i dati della route selezionata
         */
        db.collection("crags").document(id_cragClicked).collection("sectors").
                document(id_sectorClicked).collection("routes").document(id_routeClicked).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull @NotNull Task<DocumentSnapshot> task) {
                DocumentSnapshot doc = task.getResult();
                route_object.setPar(doc.getString("name"), doc.getString("grade"), doc.getString("lenght"), doc.getString("counterDelete"));
            }
        });


        /**
         * scarico tutte le note
         * popolo la pagina con le note in proceed()
         */
        db.collection("crags").document(id_cragClicked).collection("sectors").
                document(id_sectorClicked).collection("routes").document(id_routeClicked).collection("notes").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull @NotNull Task<QuerySnapshot> task) {
                noteList = new ArrayList<>();
                for (DocumentSnapshot note : task.getResult().getDocuments()) {
                    Note temp_note = new Note(
                            note.getString("date"),
                            note.getString("note")

                    );
                    temp_note.setId_Note(note.getId());
                    temp_note.setId_user(note.getString("id_creator"));
                    noteList.add(temp_note);
                }

                //ordino la lista per data (sarebbe da migliorare)
                Collections.sort(noteList);
                for (int i = 0; i<noteList.size(); i++){
                    route_object.add_note(noteList.get(i));
                    Log.i("Info", "note " + noteList.get(i).data + " nota con questa data è stata aggiunta alla via " + route_object.name);
                }
                proceed();
            }
        });

    }


    public void proceed(){
        /* Now I can show information as the crag_object has finished to get info from the Database */
        notes.removeAllViews();
        for(int i = 0;i < route_object.notes.size();i++){
            TableRow new_row = new TableRow(getApplicationContext());

            TextView date = new TextView(getApplicationContext());
            TextView txt_note = new TextView(getApplicationContext());

            date.setWidth(300); date.setTextSize(TypedValue.COMPLEX_UNIT_SP,16.f);
            txt_note.setWidth(400); txt_note.setTextSize(TypedValue.COMPLEX_UNIT_SP,16.f);

            date.setText(route_object.notes.get(i).data);
            txt_note.setText(route_object.notes.get(i).txtNota);

            new_row.addView(date); new_row.addView(txt_note);

            String creator = route_object.notes.get(i).getId_user();
            
            if(creator != null && creator.equals(user.getUid())){
                ImageButton btnTrash = (ImageButton) getLayoutInflater().inflate(R.layout.button_trashview,null);

                String id = route_object.notes.get(i).id;

                btnTrash.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        AlertDialog.Builder alert = new AlertDialog.Builder(note_detail.this);
                        alert.setTitle("Delete note");
                        alert.setMessage("Are you sure you want to delete your note?.");
                        alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                db.collection("crags").document(id_cragClicked).collection("sectors").
                                        document(id_sectorClicked).collection("routes").document(id_routeClicked).collection("notes").document(id).delete();
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
                });

                new_row.addView(btnTrash);
            }

            android.widget.TableRow.LayoutParams p = new android.widget.TableRow.LayoutParams();
            p.height = 30;
            new_row.setLayoutParams(p);

            notes.addView(new_row);
        }

    }
    /**
     * verifica se l'utente ha gia votato il grado
     * True : manda a popUpHasVoted(Grade)
     * False : manda a voteGradePopUp(String)
     */
    public Boolean hasVoted = false;
    public void voteGrade(){
        String UId = user.getUid();
        hasVoted = false;

        //scarico tutti i gradi salvati fino ad ora
        db.collection("crags").document(id_cragClicked).collection("sectors").
                document(id_sectorClicked).collection("routes").document(id_routeClicked).collection("grade").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull @NotNull Task<QuerySnapshot> task) {

                for (DocumentSnapshot grade : task.getResult().getDocuments()) {
                    Grade temp_grade = new Grade(
                            grade.getString("grade"),
                            grade.getString("id_user")

                    );
                    temp_grade.setId(grade.getId());
                    gradeList.add(temp_grade);

                    String grad = null;
                    String gradid = null;
                    for (Grade g : gradeList){
                        if (UId.equals(g.getId_user())){
                            //ha gia votato
                            hasVoted = true;
                            grad = g.getGrado();
                            gradid = g.getId();
                        }
                    }

                    if(hasVoted){
                        popUpHasVoted(grad, gradid);
                    } else {
                        voteGradePopUp(null, null);
                    }
                }
            }
        });



    }

    /**
     * PopUp per dire all'utente che ha gia votato e che se vuole può confermare o cmabirare il voto
     */
    public void popUpHasVoted(String g, String id){
        AlertDialog.Builder alert = new AlertDialog.Builder(note_detail.this);

        alert.setTitle("You have already voted");
        alert.setMessage("The rank was: "+ g + ". \n Do you want to change it? ");

        alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                voteGradePopUp(g, id);

            }
        }).setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                dialog.cancel();
            }
        });

        alert.show();
    }

    /**
     * PopUp per la modifica del grado della route
     */
    public void voteGradePopUp(String g, String id){

        AlertDialog.Builder alert = new AlertDialog.Builder(note_detail.this);

        alert.setTitle("Vote Grade");
        alert.setMessage("Type here your grade for the route");

        // Set an EditText view to get user input
        final EditText input = new EditText(this);
        alert.setView(input);

        alert.setPositiveButton("Confirm Grade", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                int prev = 0;

                if(g != null){  //gia votato quindi elimino il voto vecchio
                    db.collection("crags").document(id_cragClicked).collection("sectors").document(id_sectorClicked).collection("routes").
                            document(id_routeClicked).collection("grade").document(id).delete();
                    prev = gradoToInt(g);
                }

                //lo carico sul DB e poi faccio la media
                Map<String, Object> newGrade = new HashMap<>();
                newGrade.put("grade", input.getText().toString());
                newGrade.put("id_user", user.getUid());

                int finalPrev = prev;
                db.collection("crags").document(id_cragClicked).collection("sectors").document(id_sectorClicked).collection("routes").
                        document(id_routeClicked).collection("grade").add(newGrade).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                    @Override
                    public void onComplete(@NonNull @NotNull Task<DocumentReference> task) {
                        calculateMedia(finalPrev, gradoToInt(input.getText().toString()));
                    }
                });
            }

        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                dialog.cancel();
            }
        });

        alert.show();
    }

    public void calculateMedia(int Prev ,int New){
        if (Prev != 0) {    //quindi aveva votato e devo sottrarlo
            int i = 0;
            int sum = 0;
            for (Grade g : gradeList){
                sum =  sum + gradoToInt(g.getGrado());
                i++;
            }
            sum = sum - Prev + New;
            sum = sum / i;

            //aggiorno la media nella route
            db.collection("crags").document(id_cragClicked).collection("sectors").document(id_sectorClicked).
                    collection("routes").document(id_routeClicked).update("grade", gradoToString(sum));

        } else{ //non ha mai votato quindi faccio la media tra i vecchi e quello di adesso
            int i = 1;
            int sum = 0;
            for (Grade g : gradeList){
                sum =  sum + gradoToInt(g.getGrado());
                i++;
            }
            sum = sum + New;
            sum = sum / i;

            //aggiorno la media nella route
            db.collection("crags").document(id_cragClicked).collection("sectors").document(id_sectorClicked).
                    collection("routes").document(id_routeClicked).update("grade", gradoToString(sum));
        }


    }

    //popUp conferma aggiunta nota
    //reindirizza a add_note.java
    public void openDialog(){
        AlertDialog.Builder alert = new AlertDialog.Builder(note_detail.this);
        alert.setTitle("Add new Note");
        alert.setMessage("Do you want to add a new note to the route?");
        alert.setCancelable(true);
        alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(getApplicationContext(), add_notes.class);
                intent.putExtra("routeName",routeName);
                intent.putExtra("idRoute", id_routeClicked);
                intent.putExtra("idSector", id_sectorClicked);
                intent.putExtra("idCrag", id_cragClicked);
                intent.putExtra("editable","true");
                startActivity(intent);
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

    public void move_to_info_crag(){
        Intent intent = new Intent(this, crag_detail.class);
        intent.putExtra("id",id_cragClicked);
        intent.putExtra("editable",getIntent().getStringExtra("editable"));
        startActivity(intent);
    }

    public int gradoToInt(String g){
        switch (g) {
            case "4a": return 40;
            case "4a+": return 42;
            case "4b": return 43;
            case "4b+": return 45;
            case "4c": return 47;
            case "4c+": return 48;

            case "5a": return 50;
            case "5a+": return 52;
            case "5b": return 53;
            case "5b+": return 55;
            case "5c": return 57;
            case "5c+": return 58;

            case "6a": return 60;
            case "6a+": return 62;
            case "6b": return 63;
            case "6b+": return 65;
            case "6c": return 67;
            case "6c+": return 68;

            case "7a": return 70;
            case "7a+": return 72;
            case "7b": return 73;
            case "7b+": return 75;
            case "7c": return 77;
            case "7c+": return 78;

            case "8a": return 80;
            case "8a+": return 82;
            case "8b": return 83;
            case "8b+": return 85;
            case "8c": return 87;
            case "8c+": return 88;

            default: return 1;
        }
    }

    public String gradoToString(int g){
        switch (g) {
            case 40: return "4a";
            case 41: return "4a";
            case 42: return "4a+";
            case 43: return "4b";
            case 44: return "4b";
            case 45: return "4b+";
            case 46: return "4b+";
            case 47: return "4c";
            case 48: return "4c+";
            case 49: return "4c+";

            case 50: return "5a";
            case 51: return "5a";
            case 52: return "5a+";
            case 53: return "5b";
            case 54: return "5b";
            case 55: return "5b+";
            case 56: return "5b+";
            case 57: return "5c";
            case 58: return "5c+";
            case 59: return "5c+";

            case 60: return "6a";
            case 61: return "6a";
            case 62: return "6a+";
            case 63: return "6b";
            case 64: return "6b";
            case 65: return "6b+";
            case 66: return "6b+";
            case 67: return "6c";
            case 68: return "6c+";
            case 69: return "6c+";

            case 70: return "7a";
            case 71: return "7a";
            case 72: return "7a+";
            case 73: return "7b";
            case 74: return "7b";
            case 75: return "7b+";
            case 76: return "7b+";
            case 77: return "7c";
            case 78: return "7c+";
            case 79: return "7c+";

            case 80: return "8a";
            case 81: return "8a";
            case 82: return "8a+";
            case 83: return "8b";
            case 84: return "8b";
            case 85: return "8b+";
            case 86: return "8b+";
            case 87: return "8c";
            case 88: return "8c+";
            case 89: return "8c+";

            default: return "";
        }
    }

    public String MediaGrado (String Gproposto, String Gprecedente, int ngrade){
        int precedente = gradoToInt(Gprecedente);
        int proposto = gradoToInt(Gproposto);
        int g3 = (proposto + (precedente * ngrade) ) / (ngrade + 1);


        return gradoToString(g3);
    }

    //prova per vedere i valori
    /*public void popUpProva(String a, String b){
        AlertDialog.Builder alert = new AlertDialog.Builder(note_detail.this);
        alert.setTitle(" ");
        alert.setMessage("precedente " + a + " proposto "+ b );
        alert.setCancelable(true);

        alert.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        AlertDialog alert_dialog = alert.create();
        alert_dialog.show();
    }*/
}
