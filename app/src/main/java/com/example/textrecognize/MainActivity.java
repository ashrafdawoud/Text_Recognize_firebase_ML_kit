package com.example.textrecognize;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.text.FirebaseVisionCloudTextRecognizerOptions;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

public class MainActivity extends AppCompatActivity {
private  final int REQUIST_TAKE_PHOTO=1;
ImageView imageView;
TextView textView;
    String currentPhotoPath;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imageView=findViewById (R.id.image);
        textView=findViewById (R.id.text);
    }


    public void takePhoto(View view) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        Toast.makeText (MainActivity.this,"text",Toast.LENGTH_LONG).show ();

        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File

            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.example.android.fileprov",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUIST_TAKE_PHOTO);


            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult (requestCode, resultCode, data);

        if(requestCode==REQUIST_TAKE_PHOTO){
            if(resultCode==RESULT_OK){
                //Bitmap photo=(Bitmap) data.getExtras ().get ("data");
                //RecognizePhoto(photo);
                //imageView.setImageBitmap (photo);
                galleryAddPic();


            } else if (requestCode==RESULT_CANCELED){
                Toast.makeText (MainActivity.this,"canceld",Toast.LENGTH_LONG).show ();
            }


            else {                Toast.makeText (MainActivity.this,"fail",Toast.LENGTH_LONG).show ();
            }

        }
    }

    private void RecognizePhoto(Bitmap photo) {
        FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(photo);

        FirebaseVisionTextRecognizer detector = FirebaseVision.getInstance()
                .getOnDeviceTextRecognizer();

        FirebaseVisionCloudTextRecognizerOptions options = new FirebaseVisionCloudTextRecognizerOptions.Builder()
                .setLanguageHints(Arrays.asList("en", "ar"))
                .build();

        Task<FirebaseVisionText> result= detector.processImage(image)
                .addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
                    @Override
                    public void onSuccess(FirebaseVisionText firebaseVisionText) {
                        // Task completed successfully
                        // ...
                        for (FirebaseVisionText.TextBlock block: firebaseVisionText.getTextBlocks()) {
                            Rect boundingBox=block.getBoundingBox ();
                            Point[] cornerPoint=block.getCornerPoints ();
                            String text=block.getText ();
                            textView.setText (firebaseVisionText.getText ());

                        }

                        }
                })
                .addOnFailureListener(
                        new OnFailureListener () {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                // Task failed with an exception
                                // ...
                                Toast.makeText (MainActivity.this,"onFailer" ,Toast.LENGTH_LONG).show ();
                            }
                        });

    }
    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat ("yyyyMMdd_HHmmss").format(new Date ());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        //File storageDir=Environment.getExternalStoragePublicDirectory (Environment.DIRECTORY_PICTURES);
        Log.v ("external", String.valueOf (storageDir));
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        Log.e ("path",currentPhotoPath);

        return image;

    }
    private void setPic() {
        // Get the dimensions of the View
        int targetW = imageView.getWidth();
        int targetH = imageView.getHeight();

        // Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;

        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        // Determine how much to scale down the image
        int scaleFactor = Math.min(photoW/targetW, photoH/targetH);

        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;

        Bitmap bitmap = BitmapFactory.decodeFile(currentPhotoPath, bmOptions);
        MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, "nana" , "nana");
        RecognizePhoto(bitmap);
        imageView.setImageBitmap(bitmap);
        Log.e ("bitmap",String.valueOf (bitmap));

    }
    private void galleryAddPic()   {
        File f = new File(currentPhotoPath);
        if(f.length ()==0){f.delete ();}else {
            Uri contentUri = Uri.fromFile (f);
            Intent mediaScanIntent = new Intent (Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, contentUri);
            sendBroadcast (mediaScanIntent);
            Log.d ("media", String.valueOf (mediaScanIntent));
            setPic ( );


        }}


}
