package com.example.tirociniojustclimb;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;

import android.provider.MediaStore;
import android.util.Log;
import android.util.MonthDisplayHelper;
import android.view.Display;
import android.view.View;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;


public class image_page extends AppCompatActivity {
    public TextView titlePage;
    public ImageButton newImg;
    public TableLayout table_img;
    public TableRow new_row;
    public Button return_to_cragDetail;

    public String id_crag_clicked;
    public String name_crag_clicked;

    public int nPhoto = 0;
    // DB reference
    FirebaseStorage storage = FirebaseStorage.getInstance();
    StorageReference gsReference = storage.getReferenceFromUrl("gs://justclimbtirocinio.appspot.com");

    //public ImageView provaImgView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_page);

        id_crag_clicked = getIntent().getStringExtra("id");
        name_crag_clicked = getIntent().getStringExtra("name");

        titlePage = findViewById(R.id.CragImageTitle);
        titlePage.setText(name_crag_clicked);
        table_img = findViewById(R.id.table_img);
        newImg = findViewById(R.id.NewImageButton);
        return_to_cragDetail = findViewById(R.id.return_to_crag_detail3);

        //provaImgView = findViewById(R.id.imageViewProva23);

        return_to_cragDetail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                return_to_crag_detail();
            }
        });

        newImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadImage();
            }
        });

        popola_gridImage();
    }

    /**
     * take picture from DB
     * folder's name is crag ID
     */
    public void popola_gridImage(){     // https://firebase.google.com/docs/storage/android/start
        //elenca tutti i file contenuti nella cartella denominata con l'ID della crag
        StorageReference listRef = gsReference.child(id_crag_clicked + "/");
        listRef.listAll()
                .addOnSuccessListener(new OnSuccessListener<ListResult>() {
                    @Override
                    public void onSuccess(ListResult listResult) {
                        if(listResult.getItems().isEmpty()){
                            popUpisEmpty();
                        } else{
                            for (StorageReference item : listResult.getItems()) {
                                // All the items under listRef.
                                StorageReference imgRef = gsReference.child(id_crag_clicked + "/" + item.getName());
                                visual_img(imgRef);
                                nPhoto = nPhoto +1;     //conto le foto per poi usarlo per assegnare il nome
                            }
                        }
                    }
                });
    }

    /**
     *
     * @param imgRef
     */
    public void visual_img(StorageReference imgRef){
        android.widget.TableRow.LayoutParams p = new android.widget.TableRow.LayoutParams();
        p.bottomMargin = 10;

        //ImageView imgView = (ImageView)getLayoutInflater().inflate(R.layout.img_imageview, null);
        ImageView imgView = new ImageView(getApplicationContext());
        TableRow new_row = new TableRow(getApplicationContext());

        //questa parte poi va copiata sopra
        final long ONE_MEGABYTE = 1024 * 1024;
        imgRef.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                Bitmap bmp = BitmapFactory.decodeByteArray(bytes,0,bytes.length);
                imgView.setImageBitmap(bmp);
                imgView.setLayoutParams(p);

                new_row.addView(imgView);
                table_img.addView(new_row);

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle any errors
            }
        });
    }

    /**
     * PopUp "is Empty" new Img?
     */
    public void popUpisEmpty(){
        AlertDialog.Builder alert = new AlertDialog.Builder(image_page.this);
        alert.setTitle("No images");
        alert.setMessage("For this Crag there are no images. Do you want to add first?");
        alert.setCancelable(true);
        alert.setPositiveButton("Add Image", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                uploadImage();
                dialog.cancel();
            }
        });
        alert.setNegativeButton("Esc", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                return_to_crag_detail();
            }
        });
        AlertDialog alert_dialog = alert.create();
        alert_dialog.show();
    }

    /**
     *get photo from gallery
     * upload photo in cloud
     */
    public void uploadImage(){//prende le foto dal telefono https://medium.com/@hasangi/capture-image-or-choose-from-gallery-photos-implementation-for-android-a5ca59bc6883 e poi le carica sul db https://firebase.google.com/docs/storage/android/start
        Intent pickPhoto = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(pickPhoto, 1);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Bitmap selectedImage = null;

        //prende la foto dalla galleria
        if (resultCode == RESULT_OK) {
            try {
                final Uri imageUri = data.getData();
                final InputStream imageStream = getContentResolver().openInputStream(imageUri);
                selectedImage = BitmapFactory.decodeStream(imageStream);
                //provaImgView.setImageBitmap(selectedImage);
            } catch (FileNotFoundException e) {
            }
        }

        //carica la foto sul cloud
        nPhoto++;
        StorageReference newImgRef = gsReference.child(id_crag_clicked + "/"+nPhoto+".jpg");   //crea la cartella se non esiste e ne codifica il nome
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        selectedImage.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] dataImg = baos.toByteArray();

        UploadTask uploadTask = newImgRef.putBytes(dataImg);
        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                reloadPage();
            }
        });

    }

    public void reloadPage (){
        Intent intent = new Intent(this, image_page.class);
        intent.putExtra("id",id_crag_clicked);
        intent.putExtra("name", name_crag_clicked);
        startActivity(intent);
    }


    /**
     * method
     */
    public void return_to_crag_detail(){
        Intent intent2 = new Intent(getApplicationContext(), crag_detail.class);
        intent2.putExtra("id", id_crag_clicked);
        intent2.putExtra("editable", "true");
        startActivity(intent2);
    }


    /* public void popUpProva(Boolean a){
        AlertDialog.Builder alert = new AlertDialog.Builder(image_page.this);
        alert.setTitle(" ");
        alert.setMessage(" " +a.toString() );
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

    /*
    private void uploadImage() {
        final CharSequence[] options = {"Take Photo", "Choose from Gallery", "Cancel"};

        AlertDialog.Builder builder = new AlertDialog.Builder(image_page.this);
        builder.setTitle("Choose your profile picture");

        builder.setItems(options, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int item) {

                if (options[item].equals("Take Photo")) {
                    Intent takePicture = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(takePicture, 0);

                } else if (options[item].equals("Choose from Gallery")) {
                    Intent pickPhoto = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(pickPhoto, 1);

                } else if (options[item].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }*/

}

