package com.example.tirociniojustclimb;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Date;
import java.util.Calendar;

public class add_sector extends AppCompatActivity {

    EditText sectorName;
    Button createSector;
    EditText routeName;
    EditText routeGrade;
    EditText routeLenght;
    TextView titlePage;

    Button addRoute;
    Button Annulla;
    TableLayout newRoutesTable;
    String id_crag_clicked;

    String id_sector_clicked ;
    String name_sector_clicked ;

    /*counter to complete db requests*/
    int remainingRoutesToBeAdded;

    Sector supportSector = new Sector();

    FirebaseFirestore db;
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_sector);

        db = FirebaseFirestore.getInstance();

        id_crag_clicked = getIntent().getStringExtra("id");

        sectorName = findViewById(R.id.new_sector_name);
        createSector = findViewById(R.id.create_sector_btn);
        routeName = findViewById(R.id.new_route_name);
        routeGrade = findViewById(R.id.new_route_grade);
        routeLenght = findViewById(R.id.new_route_lenght);
        newRoutesTable = findViewById(R.id.new_routes_table);
        addRoute = findViewById(R.id.add_route_btn);
        Annulla = findViewById(R.id.return_to_crag_detail2);


        if(getIntent().getStringExtra("operation") != null){                                                                  //add only route to an existing sector

            id_sector_clicked = getIntent().getStringExtra("id_sector_clicked");
            name_sector_clicked = getIntent().getStringExtra("name_sector_clicked");

            titlePage = findViewById(R.id.new_sector_title5);
            titlePage.setText(""+name_sector_clicked);
            sectorName.setVisibility(View.INVISIBLE);
            createSector.setText("Confirm");

            createSector.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    /* Check for basic condition for new sector */
                    if(newRoutesTable.getChildCount()<1){
                        Toast.makeText(getApplicationContext(),R.string.toast_addSector_emptyTable,Toast.LENGTH_SHORT).show();
                    }else{
                        /* ready to send new sector to firebase DB*/
                        Toast.makeText(getApplicationContext(),R.string.toast_addSector_adding,Toast.LENGTH_SHORT).show();

                        supportSector.name = name_sector_clicked;


                        remainingRoutesToBeAdded = supportSector.routes.size();
                        for(Route route : supportSector.getRoutes()){
                            Map<String, Object> newRoute = new HashMap<>();
                            newRoute.put("name",route.name);
                            newRoute.put("grade",route.grade);
                            newRoute.put("lenght",route.lenght);
                            newRoute.put("counterDelete", "0");

                            db.collection("crags").document(id_crag_clicked).collection("sectors").document(id_sector_clicked).collection("routes").add(newRoute).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                @Override
                                public void onComplete(@NonNull @NotNull Task<DocumentReference> task) {
                                    String newRouteId = task.getResult().getId(); //verificato lo prende giusto l'ID della via

                                    //creo la sottocollection con la prima nota vuota
                                    Map<String, Object> newnota = new HashMap<>();

                                    //ottengo la data
                                    Date c = Calendar.getInstance().getTime();
                                    SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault());
                                    String formattedDate = df.format(c);

                                    newnota.put("date", formattedDate);
                                    newnota.put("note", "Creazione via");

                                    db.collection("crags").document(id_crag_clicked).collection("sectors").document(id_sector_clicked).collection("routes").document(newRouteId).collection("notes").add(newnota).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                        @Override
                                        public void onComplete(@NonNull @NotNull Task<DocumentReference> task) {
                                            move_to_info_crag();
                                        }
                                    });
                                }
                            });
                        }
                    }

                }
            });

        } else {                                                                                    //creating a new sector and add new routes
            createSector.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    /* Check for basic condition for new sector */
                    if(sectorName.getText().length()<1) {
                        Toast.makeText(getApplicationContext(), R.string.toast_addSector_noSectorName, Toast.LENGTH_SHORT).show();
                    }else if(newRoutesTable.getChildCount()<1){
                        Toast.makeText(getApplicationContext(),R.string.toast_addSector_emptyTable,Toast.LENGTH_SHORT).show();
                    }else{
                        /* ready to send new sector to firebase DB*/
                        Toast.makeText(getApplicationContext(),R.string.toast_addSector_adding,Toast.LENGTH_SHORT).show();
                        supportSector.name = sectorName.getText().toString();
                        Map<String, Object> data = new HashMap<>();
                        data.put("name", supportSector.name);
                        data.put("counterDelete", "0");
                        db.collection("crags").document(id_crag_clicked).collection("sectors").add(data).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                            @Override
                            public void onComplete(@NonNull @NotNull Task<DocumentReference> task) {
                                String newSectorId = task.getResult().getId();
                                remainingRoutesToBeAdded = supportSector.routes.size();
                                for(Route route : supportSector.getRoutes()){
                                    Map<String, Object> newRoute = new HashMap<>();
                                    newRoute.put("name",route.name);
                                    newRoute.put("grade",route.grade);
                                    newRoute.put("lenght",route.lenght);
                                    newRoute.put("counterDelete", "0");

                                    db.collection("crags").document(id_crag_clicked).collection("sectors").document(newSectorId).collection("routes").add(newRoute).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                        @Override
                                        public void onComplete(@NonNull @NotNull Task<DocumentReference> task) {
                                            String newRouteId = task.getResult().getId(); //verificato lo prende giusto l'ID della via

                                            //creo la sottocollection con la prima nota vuota
                                            Map<String, Object> newnota = new HashMap<>();

                                            //ottengo la data
                                            Date c = Calendar.getInstance().getTime();
                                            SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault());
                                            String formattedDate = df.format(c);

                                            newnota.put("date", formattedDate);
                                            newnota.put("note", "Creazione via");

                                            db.collection("crags").document(id_crag_clicked).collection("sectors").document(newSectorId).collection("routes").document(newRouteId).collection("notes").add(newnota).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                                @Override
                                                public void onComplete(@NonNull @NotNull Task<DocumentReference> task) {

                                                    Map<String, Object> newGrade = new HashMap<>();
                                                    newGrade.put("grade", route.grade);
                                                    newGrade.put("id_user", user.getUid());

                                                    //aggiungo anche la nuova collection "grade" con il grado e l'id utente
                                                    db.collection("crags").document(id_crag_clicked).collection("sectors").document(newSectorId).collection("routes").document(newRouteId).collection("grade").add(newGrade).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                                        @Override
                                                        public void onComplete(@NonNull @NotNull Task<DocumentReference> task) {
                                                            move_to_info_crag();
                                                        }
                                                    });

                                                }
                                            });

                                        }
                                    });
                                }
                            }
                        });
                    }
                }
            });
        }

        addRoute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(routeName.getText().length()<1 | routeGrade.getText().length()<1 | routeLenght.getText().length() <1){
                    /* missing informations */
                    Toast.makeText(getApplicationContext(),R.string.toast_addSector_routeParamsUnsufficient,Toast.LENGTH_SHORT).show();
                }else{
                    /* Route ok to be added */
                    Route supportRoute = new Route(routeName.getText().toString(),routeGrade.getText().toString(),routeLenght.getText().toString());

                    supportSector.add_route(supportRoute);
                    /* empty form */
                    routeName.setText("");
                    routeGrade.setText("");
                    routeLenght.setText("");


                    refreshTable();
                }
            }
        });
        Annulla.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDialogAnnulla();
            }
        });
    }

    /**
     * popUp conferma annulla operazione - reindirizza a crag_detail.java
     */
    public void openDialogAnnulla(){
        AlertDialog.Builder alert = new AlertDialog.Builder(add_sector.this);
        alert.setTitle("Return to Crag Detail");
        alert.setMessage("Are you sure you want to return to Crag Detail ?");
        alert.setCancelable(true);
        alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent2 = new Intent(getApplicationContext(), crag_detail.class);
                intent2.putExtra("id", id_crag_clicked);
                intent2.putExtra("editable", "true");
                startActivity(intent2);
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

    /**
     * method to move to info crag, simple intent
     */
    public void move_to_info_crag(){
        remainingRoutesToBeAdded--;
        if(remainingRoutesToBeAdded == 0) {
            Intent intent = new Intent(this, crag_detail.class);
            intent.putExtra("id", id_crag_clicked);
            intent.putExtra("editable", "true");
            startActivity(intent);
        }
    }

    /**
     * method
     */
    public void refreshTable(){
        /* show all routes in the table , prefer to remove them all and add again in order to
         * check if the Java Object is been created correctly */
        newRoutesTable.removeAllViews();
        for(Route route : supportSector.getRoutes()){
            TableRow new_row = new TableRow(getApplicationContext());

            TextView name = new TextView(getApplicationContext());
            TextView grade = new TextView(getApplicationContext());
            TextView lenght = new TextView(getApplicationContext());

            name.setWidth(280); name.setTextSize(TypedValue.COMPLEX_UNIT_SP,16.f);
            grade.setWidth(280); grade.setTextSize(TypedValue.COMPLEX_UNIT_SP,16.f);
            lenght.setWidth(280); lenght.setTextSize(TypedValue.COMPLEX_UNIT_SP,16.f);

            name.setText(route.name);
            grade.setText(route.grade);
            lenght.setText(route.lenght);

            new_row.addView(name); new_row.addView(grade); new_row.addView(lenght);

            newRoutesTable.addView(new_row);
        }
    }

}
