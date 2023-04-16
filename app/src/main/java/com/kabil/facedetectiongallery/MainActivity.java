package com.kabil.facedetectiongallery;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.SparseArray;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    File pathToImageDirectory;
    private static final int REQUEST_STORAGE_PERMISSION = 1;

    TextView imageCount,faceCount;
    Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
       // getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_STORAGE_PERMISSION);
        }
        createDCIMDir();

        button = findViewById(R.id.button);
        imageCount = findViewById(R.id.imageCount);
        faceCount=findViewById(R.id.faceCount);



        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                getAllFacesFromGalleryImages(MainActivity.this);

            }
        });



    }

    private void createDCIMDir() {
        // Define the name of the new directory
        String newDirName = "FaceDetectionGallery";

        // Get the path to the DCIM directory
        String dcimPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getAbsolutePath();

        // Create a File object for the new directory inside the DCIM directory
        pathToImageDirectory = new File(dcimPath + File.separator + newDirName);

        // Create the directory if it does not already exist
        if (!pathToImageDirectory.exists()) {
            boolean success = pathToImageDirectory.mkdirs();
            if (!success) {
                // Handle the error if the directory could not be created
                Toast.makeText(getApplicationContext(), "Directory Not created: ", Toast.LENGTH_SHORT).show();
            }
        }
    }

    protected void getAllFacesFromGalleryImages(Context context) {
        int image_count=0;
        int face_count=0;
        int positionx;
        int positiony;
        int width;
        int height;
        int columnIndex;
        long id;
        Uri uri;
        Bitmap bitmap;
        FaceDetector faceDetector;
        Frame frame;
        SparseArray<Face> faces;
        Face face;
        Bitmap faceBitmap;
        String baseFilename;
        String timestamp;
        String filename;
        File file;
        FileOutputStream fos;
        Cursor cursor;
        String[] projection = {MediaStore.Images.Media._ID};
        String fc,ic;


        cursor = context.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, null, null, null);


        if (cursor != null && cursor.moveToFirst()) {
            columnIndex = cursor.getColumnIndex(MediaStore.Images.Media._ID);
            do {
                id = cursor.getLong(columnIndex);
                uri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);
                try {
                    bitmap = BitmapFactory.decodeStream(context.getContentResolver().openInputStream(uri));
                    // Create a FaceDetector instance
                    faceDetector = new FaceDetector.Builder(context).setTrackingEnabled(false).setLandmarkType(FaceDetector.ALL_LANDMARKS).build();

                    // Create a Frame instance from the bitmap
                    frame = new Frame.Builder().setBitmap(bitmap).build();

                    // Detect faces in the frame
                    faces = faceDetector.detect(frame);

                    // Loop through the detected faces
                    for (int i = 0; i < faces.size(); i++) {
                        face = faces.valueAt(i);
                        try {
                            positionx=(int) face.getPosition().x;
                            positiony=(int) face.getPosition().y;

                            width=(int) face.getWidth();
                            height=(int) face.getHeight();
                            if(positionx>0 && positiony>0 && width>0 && height >0) {
                                if((positionx+width)<=bitmap.getWidth() &&(positiony+height)<=bitmap.getHeight()) {

                                    // Crop the face from the original bitmap
                                    faceBitmap = Bitmap.createBitmap(bitmap,
                                            positionx,
                                            positiony,
                                            width,
                                            height);

                                    // Save the cropped face to a file

                                    baseFilename = "face";
                                    timestamp = Long.toString(System.currentTimeMillis());
                                    filename = baseFilename + timestamp + ".jpg";


                                    file = new File(pathToImageDirectory, filename);
                                    fos = new FileOutputStream(file);
                                    faceBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                                    fos.flush();
                                    fos.close();
                                    faceBitmap.recycle();
                                }
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        face_count=face_count+1;
                        fc="Face Count is: "+face_count;
                        faceCount.setText(fc);
                    }

                    // Release resources
                    faceDetector.release();
                    bitmap.recycle();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                image_count=image_count+1;
                ic="Image Count is: "+image_count;
                imageCount.setText(ic);

            } while (cursor.moveToNext());
            cursor.close();
        }


    }
    @Override
    public void onBackPressed() {
        closeApp();
    }

    public void closeApp() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure you want to Exit ?").setCancelable(false).setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                finish();
            }
        }).setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }
}