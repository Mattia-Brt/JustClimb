package com.example.tirociniojustclimb;


import android.Manifest;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Text;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;


public class crag_detail extends AppCompatActivity {

    public String id_clicked;
    public String id_sector_clicked;
    public String name_sector_clicked;
    public TextView crag_name;
    public TextView descr;
    public ImageButton edit;
    public ImageButton download;
    public ImageButton image;
    public ImageButton delete;
    public Button returnMap;

    public TableRow sectors;
    public TableLayout routes;

    public int counter;
    public int index = 0;
    public Crag crag_object = new Crag();
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crag_detail);
        id_clicked = getIntent().getStringExtra("id");
        crag_name = findViewById(R.id.detail_name);
        descr = findViewById(R.id.detail_descr);
        edit = findViewById(R.id.detail_edit);
        image = findViewById(R.id.imageButton2);
        download = findViewById(R.id.detail_download);
        sectors = findViewById(R.id.detail_sector_row);
        routes = findViewById(R.id.detail_routes);
        returnMap = findViewById(R.id.return_to_map);
        delete = findViewById(R.id.detail_trash);


        edit.setClickable(false);
        edit.setVisibility(View.INVISIBLE);
        image.setClickable(false);
        image.setVisibility(View.INVISIBLE);
        delete.setClickable(false);
        delete.setVisibility(View.INVISIBLE);

        if(getIntent().getStringExtra("editable")!=null){
            edit.setClickable(true);
            edit.setVisibility(View.VISIBLE);
            image.setClickable(true);
            image.setVisibility(View.VISIBLE);
            delete.setClickable(true);
            delete.setVisibility(View.VISIBLE);
        }

        edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isNew();
            }
        });

        download.setOnClickListener(new View.OnClickListener() {
            /* Create and download PDF formatted crag detail*/
            @Override
            public void onClick(View v) {
                stringToFile();
            }
        });

        image.setOnClickListener(new View.OnClickListener() {
            /* button image page */
            @Override
            public void onClick(View v) {
                move_to_ImagePage();
            }
        });

        delete.setOnClickListener(new View.OnClickListener() {
            /* button image page */
            @Override
            public void onClick(View v) {
                dialogWhatDelete();
            }
        });

        returnMap.setOnClickListener(new View.OnClickListener() {
            /* button image page */
            @Override
            public void onClick(View v) {
                move_to_Map();
            }
        });

        /* get info and initialize crag_object */
        db.collection("crags").document(id_clicked).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull @NotNull Task<DocumentSnapshot> task) {
                DocumentSnapshot doc = task.getResult();
                crag_object = new Crag(doc.getString("name"), doc.getString("description"), doc.getString("lat"), doc.getString("lon"));
                crag_name.setText(crag_object.name);
                descr.setText(crag_object.description);
                crag_object.setCounterDelete(doc.getString("counterDelete"));

                /* SECTORS */
                CollectionReference sectors = db.collection("crags").document(id_clicked).collection("sectors");
                sectors.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull @NotNull Task<QuerySnapshot> task) {
                        counter = task.getResult().getDocuments().size();
                        if (counter == 0) proceed();
                        for (DocumentSnapshot sector : task.getResult().getDocuments()) {
                            Sector temp_sector = new Sector(sector.getString("name"));
                            Log.i("Info", "sector " + temp_sector.name + " created");
                            temp_sector.setId_Sector(sector.getId());
                            temp_sector.setCounterDeleteSector(sector.getString("counterDelete"));

                            /* ROUTES */ //modificato in 1.2.2
                            sectors.document(sector.getId()).collection("routes").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                @Override
                                public void onComplete(@NonNull @NotNull Task<QuerySnapshot> task) {
                                    for (DocumentSnapshot route : task.getResult().getDocuments()) {
                                        Route temp_route = new Route(
                                                route.getString("name"),
                                                route.getString("grade"),
                                                route.getString("lenght")
                                        );
                                        temp_route.setDeleteN(route.getString("counterDelete"));
                                        temp_route.setId_route(route.getId());

                                        /* NOTES */
                                        sectors.document(sector.getId()).collection("routes").document(route.getId()).collection("notes").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull @NotNull Task<QuerySnapshot> task) {
                                                for (DocumentSnapshot note : task.getResult().getDocuments()) {
                                                    Note temp_note = new Note(
                                                            note.getString("date"),
                                                            note.getString("note")
                                                    );
                                                    temp_route.add_note(temp_note);
                                                    Log.i("Info", "note " + temp_note.data + " nota con questa data è stata aggiunta alla via " + temp_note);

                                                }
                                            }
                                        });

                                        temp_sector.add_route(temp_route);
                                        Log.i("Info", "route " + temp_route.name + " created and added to sector " + temp_sector.name);
                                    }
                                    crag_object.add_sector(temp_sector);
                                    Log.i("Info", "sector " + temp_sector.name + " added to crag");
                                    proceedIfFinished();
                                }
                            });
                        }
                    }
                });
            }
        });
    }
    /**
     * al load se è nuova chiede se si vuole add a new sector
     */
    public void alertNewCrag(){
        AlertDialog.Builder alert = new AlertDialog.Builder(crag_detail.this);
        alert.setTitle("New Crag");
        alert.setMessage("This crag has no sectors, do you want to add any?");
        alert.setCancelable(true);
        alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(getApplicationContext(), add_sector.class);
                intent.putExtra("id",id_clicked);
                startActivity(intent);
            }
        });
        alert.setNeutralButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        AlertDialog alert_dialog = alert.create();
        alert_dialog.show();
    }

    /**
     * se è nuova crea direttamente il settore altrimenti chiede se creare settore o aggiungere una via
     */
    public void isNew(){
        if(crag_object.sectors.isEmpty()){
            Intent intent = new Intent(getApplicationContext(), add_sector.class);
            intent.putExtra("id",id_clicked);
            startActivity(intent);
        } else{
            popUpAdding();
        }
    }
    /**
     * POP UP - adding a new route or sector
     */
    public void popUpAdding(){
        AlertDialog.Builder alert = new AlertDialog.Builder(crag_detail.this);
        alert.setTitle("Modify Crag");
        alert.setMessage("Do you want to add new route to this sector or create a new sector?");
        alert.setCancelable(true);
        alert.setNegativeButton("Route", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(getApplicationContext(), add_sector.class);
                intent.putExtra("operation", "Route");
                intent.putExtra("id", id_clicked);                                              //id crag
                intent.putExtra("id_sector_clicked", id_sector_clicked);                        //id selected sector
                intent.putExtra("name_sector_clicked", name_sector_clicked);                    //name selected sector
                startActivity(intent);
            }
        });
        alert.setPositiveButton("Sector", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(getApplicationContext(), add_sector.class);
                intent.putExtra("id",id_clicked);
                startActivity(intent);
            }
        });
        alert.setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        AlertDialog alert_dialog = alert.create();
        alert_dialog.show();
    }

    /**
     * POP UP - delete this sector or the crag?
     */
    public void dialogWhatDelete(){
        if(crag_object.sectors.isEmpty()){                                                          //elimina solo la crag
            AlertDialog.Builder alert = new AlertDialog.Builder(crag_detail.this);
            alert.setTitle("Delete this crag");
            alert.setMessage("Are you sure you want delte this Crag?");
            alert.setCancelable(true);
            alert.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                @Override
                /**
                 * inizialmente doveva mandare una mail a me dopo 10 "eliminazioni" ma essendo un servizio a pagamento la elimino gia qui authomaticamente dopo 30 elimiazioni
                 */
                public void onClick(DialogInterface dialog, int which) {
                    if(Integer.parseInt(crag_object.getCounterDelete()) < 19) {
                        crag_object.incrementaDelete();
                        db.collection("crags").document(id_clicked).update("counterDelete", crag_object.getCounterDelete());
                    } else {
                        db.collection("crags").document(id_clicked).delete();   //reimposta il counter a zero e invia la mail
                        move_to_Map();

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

        } else {                                                                                    //scelta se eliminare il settore o la crag
            AlertDialog.Builder alert = new AlertDialog.Builder(crag_detail.this);
            alert.setTitle("What delte?");
            alert.setMessage("Do you want to delte this Sector or delte this Crag?");
            alert.setCancelable(true);
            alert.setNegativeButton("Sector", new DialogInterface.OnClickListener() {           //chiede conferma prima di eliminare
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    AlertDialog.Builder alert = new AlertDialog.Builder(crag_detail.this);
                    alert.setTitle("Are you sure you want to delete this Sector?");
                    alert.setMessage("Clicking on 'delete' probably you will not delete the sector definitely. You only will vote to remove this sector by the application.");
                    alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if(Integer.parseInt(crag_object.sectors.get(index).getCounterDeleteSector()) < 14){                 //aggiorno solo il counter
                                crag_object.sectors.get(index).incrementaDelete();
                                db.collection("crags").document(id_clicked).collection("sectors").
                                        document(id_sector_clicked).update("counterDelete", crag_object.sectors.get(index).getCounterDeleteSector());
                            } else {                                                                                            //elimina
                                db.collection("crags").document(id_clicked).collection("sectors").
                                        document(id_sector_clicked).delete();
                            }
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
            alert.setPositiveButton("Crag", new DialogInterface.OnClickListener() {
                @Override
                /**
                 * inizialmente doveva mandare una mail a me dopo 10 "eliminazioni" ma essendo un servizio a pagamento la elimino gia qui authomaticamente dopo 30 elimiazioni
                 */
                public void onClick(DialogInterface dialog, int which) {
                    if(Integer.parseInt(crag_object.getCounterDelete()) < 19) {
                        crag_object.incrementaDelete();
                        db.collection("crags").document(id_clicked).update("counterDelete", crag_object.getCounterDelete());
                    } else {
                        db.collection("crags").document(id_clicked).delete();   //reimposta il counter a zero e invia la mail
                        move_to_Map();

                    }
                }
            });
            alert.setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            AlertDialog alert_dialog = alert.create();
            alert_dialog.show();
        }

    }

    /**
     * necessario per il corretto funzionamento del codice
     * procede alla creazione della tabella una volta ottenuti tutti gli oggetti
     */
    public void proceedIfFinished(){
        /* Check if finished */

        counter-=1;
        if(counter==0){
            Log.i("Info","Finished!");
            proceed();
        }else{
            Log.i("Info","Still not time to finish");
        }
    }

    /**
     * popola la tabella e la mostra
     */
    public void proceed(){
        if (crag_object.sectors.isEmpty()){
            alertNewCrag();
        } else {        /* Now I can show information as the crag_object has finished to get info from the Database */
            for(int i = 0;i < crag_object.sectors.size();i++){
                Button btn = (Button)getLayoutInflater().inflate(R.layout.sector_view,null);
                TextView blank = new TextView(this);
                blank.setText("");
                blank.setWidth(10);

                btn.setText(crag_object.sectors.get(i).name);
                btn.setId(i);

                btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //int i = Integer.parseInt(btn.getText().toString());
                        int i = btn.getId();
                        routes.removeAllViews();
                        //popola la tabella
                        id_sector_clicked = crag_object.sectors.get(i).getId_Sector();
                        name_sector_clicked = crag_object.sectors.get(i).getName();
                        index = i;
                        popolaTabella(i);
                    }
                });
                sectors.addView(btn);
                sectors.addView(blank);
            }
            //serve solo per far apparire subito la tabella atoselect il primo settore
            if (crag_object.sectors.size() > 0){
                id_sector_clicked = crag_object.sectors.get(0).getId_Sector();
                name_sector_clicked = crag_object.sectors.get(0).getName();
                index = 0;
                popolaTabella(0);
            }
        }



    }
    public void popolaTabella(int indice){
        int i=0;

        for( int j=0;j<crag_object.sectors.get(indice).routes.size();j++){
            TableRow new_row = new TableRow(getApplicationContext());

            TextView name = new TextView(getApplicationContext());
            TextView grade = new TextView(getApplicationContext());
            TextView lenght = new TextView(getApplicationContext());
            Button btnNote = new Button(getApplicationContext());
            btnNote.setText("Note");

            name.setWidth(261); name.setTextSize(TypedValue.COMPLEX_UNIT_SP,16.f);
            grade.setWidth(150); grade.setTextSize(TypedValue.COMPLEX_UNIT_SP,16.f);
            lenght.setWidth(150); lenght.setTextSize(TypedValue.COMPLEX_UNIT_SP,16.f);
            btnNote.setWidth(130);

            name.setText(crag_object.sectors.get(indice).routes.get(j).name);
            grade.setText(crag_object.sectors.get(indice).routes.get(j).grade);
            lenght.setText(crag_object.sectors.get(indice).routes.get(j).lenght);

            //ogni btn viene reindirizzato su una pagina specifica
            String name_route_clicked = name.getText().toString();
            String id_route_clicked = crag_object.sectors.get(indice).routes.get(j).getId_route();
            String id_sector_clicked = crag_object.sectors.get(indice).getId_Sector();
            String id_crag_clicked = id_clicked;


            btnNote.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    move_to_note_detail(name_route_clicked, id_route_clicked, id_sector_clicked, id_crag_clicked);
                }
            });

            new_row.addView(name); new_row.addView(grade); new_row.addView(lenght); new_row.addView(btnNote);

            //se è un utente visitor non vede il button trash
            if(getIntent().getStringExtra("editable")!=null){

                ImageButton btnTrash = (ImageButton) getLayoutInflater().inflate(R.layout.button_trashview,null);
                btnTrash.setId(j);

                btnTrash.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        AlertDialog.Builder alert = new AlertDialog.Builder(crag_detail.this);
                        alert.setTitle("Are you sure you want to delete this route?");
                        alert.setMessage("Clicking on 'delete' probably you will not delete the route definitely. You only will vote to remove this route by the application.");
                        alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if(Integer.parseInt(crag_object.sectors.get(indice).routes.get(btnTrash.getId()).getDeleteN()) < 4){   //aggiorno solo il contatore
                                    crag_object.sectors.get(indice).routes.get(btnTrash.getId()).incrementaDelete();
                                    db.collection("crags").document(id_crag_clicked).collection("sectors").
                                            document(id_sector_clicked).collection("routes").document(id_route_clicked).update("counterDelete", crag_object.sectors.get(indice).routes.get(btnTrash.getId()).getDeleteN());
                                } else {                                                                                                //lo elimino
                                    db.collection("crags").document(id_crag_clicked).collection("sectors").
                                            document(id_sector_clicked).collection("routes").document(id_route_clicked).delete();
                                /*finish();
                                startActivity(getIntent()); //funziona ma ricarica tutta la pagina */
                                }
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
            routes.addView(new_row);
        }

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
        intent.putExtra("editable",getIntent().getStringExtra("editable"));
        startActivity(intent);
    }

    /**
     *
     */
    public void move_to_ImagePage (){
        Intent intent = new Intent(this, image_page.class);
        intent.putExtra("id",id_clicked);
        intent.putExtra("name", crag_object.name);
        startActivity(intent);
    }

    /**
     *
     */
    public void move_to_Map (){
        if(getIntent().getStringExtra("editable")!=null){
            startActivity(new Intent(this, editor_mode.class));
        } else {
            startActivity(new Intent(this, viewer_mode.class));
        }
    }

    /**
     * only for download
     */
    public void stringToFile(){
        try {
            String separator = System.getProperty("line.separator");
            String doubleSeparator = "" + separator + separator;
            File gpxfile = new File(getFilePath());
            FileWriter writer = new FileWriter(gpxfile);

            /* create text sequence */
            String text = "JUST CLIMB" + separator + "All right reserved" + doubleSeparator + "_____Crag: " + crag_object.name + "_____" + doubleSeparator;
            for(Sector sect:crag_object.getSectors()){
                text += "Sector: " + sect.name + separator;
                for(Route route:sect.getRoutes()){
                    text+=String.format("%-50s%-5s%-5s",route.name,route.grade,route.lenght)+ separator;
                }
                text+=doubleSeparator;
            }

            writer.append(text);
            writer.flush();
            writer.close();
            Toast.makeText(getApplicationContext(), "File saved " + getFilePath(), Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Toast.makeText(getApplicationContext(),"Something went wrong", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    };
    private String getFilePath(){
        ContextWrapper cw = new ContextWrapper(getApplicationContext());
        File docDir = cw.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
        /*File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "prova.txt");
        File docDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
        File docDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),"CragDetail.txt");

        File docsFolder = new File(Environment.getExternalStorageDirectory() + "/Documents");
        boolean isPresent = true;
        if (!docsFolder.exists()) {
            isPresent = docsFolder.mkdir();
        }
        if (isPresent) {
            //File file = new File(docsFolder.getAbsolutePath(),"test.txt");
            File file = new File(docsFolder.getAbsolutePath(),"CragDetail.txt");
            return file.getPath();
        }
        return "";
         */

        File file = new File(docDir,"CragDetail" + ".txt");
        return file.getPath();
    }


}