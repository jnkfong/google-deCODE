package com.google_decode.decode_google;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.util.Log;
import android.content.pm.PackageManager;
import android.Manifest.permission;
import android.os.Build;
import android.Manifest.permission_group;
import android.content.Context;


import com.google.firebase.appindexing.Action;
import com.google.firebase.appindexing.FirebaseUserActions;
import com.google.firebase.appindexing.builders.Actions;
import android.support.annotation.NonNull;
import android.os.Bundle;
import android.util.Log;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import butterknife.OnClick;

import java.io.File;

public class MainActivity extends Activity{ //used to be "extends AppCompatActivity"
    //will i get an error for changing to activity ??

    // Firebase instance variables
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;


    private static final String TAG = "MainActivity";
    public static final String ANONYMOUS = "anonymous";

    private String mUsername;
    private String mPhotoUrl;
    private Uri downloadUrl;

    //camera stuff
    Button button;
    ImageView imageView;
    Bitmap bmp;
    static final int REQUEST_IMAGE_CAPTURE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // Set default username is anonymous.
        mUsername = ANONYMOUS;
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        uploadPic("/storage/emulated/0/DCIM/Camera/IMG_20170310_151727.jpg" , "IMG_20170310_151727.jpg");
        //Log.v("ImageUpload",downloadUrl.toString());
        if (mFirebaseUser == null) {
            // Not signed in, launch the Sign In activity
            startActivity(new Intent(this, SignInActivity.class));
            finish();
        } else {
            mUsername = mFirebaseUser.getDisplayName();
            if (mFirebaseUser.getPhotoUrl() != null) {
                mPhotoUrl = mFirebaseUser.getPhotoUrl().toString();
            }


//            final Button button = (Button) findViewById(R.id.btnTesting);
//            button.setOnClickListener(new View.OnClickListener() {
//                public void onClick(View v) {
//                   DatabaseRetriever.sendMessage("Working");
//
//                    DatabaseRetriever.getMessage();
//                }
//            });



            FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference myRef = database.getReference("message");

//            myRef.addValueEventListener(new ValueEventListener() {
//                @Override
//                public void onDataChange(DataSnapshot dataSnapshot) {
//                    // This method is called once with the initial value and again
//                    // whenever data at this location is updated.
//                    String value = dataSnapshot.getValue(String.class);
//                    Log.d(TAG, "Value is: " + value);
//                    final TextView textViewTesting = (TextView) findViewById(R.id.textViewTesting);
//                    textViewTesting.setText(value);
//
//                }

//                @Override
//                public void onCancelled(DatabaseError error) {
//                    // Failed to read value
//                    Log.w(TAG, "Failed to read value.", error.toException());
//                }
//            });
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imageView = (ImageView)findViewById(R.id.image_view);
        imageView.setAdjustViewBounds(false);
        imageView.setBackgroundColor(0xff0000);

        Log.v("test", "height: " + imageView.getHeight() + " and width: " + imageView.getWidth());
        Log.v("test", "x and y coordinates: " + imageView.getX() + " " + imageView.getY());
        button = (Button)findViewById(R.id.button); //space? error??
        //imageView = (ImageView)findViewById(R.id.image_view);
        //maybe fix - put this line on other section
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String imageFilePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/picture.jpg";
                Log.v("test", imageFilePath);
                //File folder = new File(Environment.getExternalStorageDirectory(), "myCoolImages");
                File imageFile = new File(imageFilePath);

                Context context = MainActivity.this;

                Uri imageFileUri = FileProvider.getUriForFile(context, context.getApplicationContext().getPackageName() + ".provider", imageFile);

                //Uri imageFileUri = FileProvider.getUriForFile(context, context.getApplicationContext().getPackageName() + ".provider", imageFile);

                //Uri imageFileUri = Uri.fromFile(imageFile); // convert path to Uri

                Intent camera_intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                camera_intent.putExtra (MediaStore.EXTRA_OUTPUT, imageFileUri);
                startActivityForResult(camera_intent, REQUEST_IMAGE_CAPTURE);
            }
        });

        isStoragePermissionGranted();
        isCameraPermissionGranted();

    }

    public  boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.v("test","Permission is granted");
                return true;
            } else {

                Log.v("test","Permission is revoked");
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                return false;
            }
        }
        else { //permission is automatically granted on sdk<23 upon installation
            Log.v("test","Permission is granted");
            return true;
        }
    }

    public  boolean isCameraPermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(android.Manifest.permission.CAMERA)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.v("test","camera Permission is granted");
                return true;
            } else {

                Log.v("test","camera Permission is revoked");
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.CAMERA}, 1);
                return false;
            }
        }
        else { //permission is automatically granted on sdk<23 upon installation
            Log.v("test","camera Permission is granted");
            return true;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) { //add && requestCode == REQUEST_IMAGE_CAPTURE ???

            imageView.setBackgroundColor(0xff0000);
            String imageFilePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/picture.jpg";
            Log.v("test", imageFilePath);

            //decode it
            BitmapFactory.Options bmpFactoryOptions = new BitmapFactory.Options();
            bmpFactoryOptions.inJustDecodeBounds = false;

            //pass intent
            bmp = BitmapFactory.decodeFile(imageFilePath);

            if (bmp != null) {
                Log.v("test", "bmp is valid");
            } else {
                Log.v("test", "bmp is NULL");
            }

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    imageView.setImageBitmap(bmp);
                    imageView.invalidate();
                }
            });



            //imageView.setImageBitmap(bmp);
            Log.v("test", "height: " + imageView.getHeight() + " and width: " + imageView.getWidth());
            Log.v("test", "x and y coordinates: " + imageView.getX() + " " + imageView.getY());

        }
        //String path = "myCoolImages/cam_image.jpg";
        //imageView.setImageDrawable(Drawable.createFromPath(path));
        //does this mean only one image at a time?
    }

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    public Action getIndexApiAction() {
        return Actions.newView("Main", "http://[ENTER-YOUR-URL-HERE]");
    }


    //Accepts the local file location of the image as well as the name that should be used to store the image on FireBase.
    private void uploadPic(String fileLoc, String fileName){
        //Setting up FireBase
        StorageReference mStorageRef;
        mStorageRef = FirebaseStorage.getInstance().getReference();

        //Retrieving Local Image URI
        Uri file = Uri.fromFile(new File(fileLoc));
        StorageReference riversRef = mStorageRef.child("/images/"+fileName);

        //Uploading the file to FireBase storage
        riversRef.putFile(file)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        // Get a URL to the uploaded content
                        //The url of the file online
                        //noinspection VisibleForTests
                        downloadUrl = taskSnapshot.getDownloadUrl();
                        mFirebaseAuth = FirebaseAuth.getInstance();
                        mFirebaseUser = mFirebaseAuth.getCurrentUser();
                        downloadPic(downloadUrl.toString());
                        //downloadPic("IMG_20170310_151727.jpg");

                        //Uploads the image details to FireBase Database
                        //DatabaseRetriever.sendUri(downloadUrl,mFirebaseUser.getDisplayName());
                        Log.v("ImageUpload",downloadUrl.toString());
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Handle unsuccessful uploads
                        // ...
                        Toast.makeText(getApplicationContext(),"Sorry the file wasn't sent!",Toast.LENGTH_LONG).show();
                    }
                });

    }

    private void downloadPic(String src ){
        //This works using file name which downlaods directly from storage
//        File localFile = null;
//        try {
//            localFile = File.createTempFile("images", "jpg");
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        StorageReference mStorageRef;
//        mStorageRef = FirebaseStorage.getInstance().getReference();
//        StorageReference riversRef = mStorageRef.child("/images/"+fileName);
//        if (localFile != null) {
//            riversRef.getFile(localFile)
//                    .addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
//                        @Override
//                        public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
//                            // Successfully downloaded data to local file
//                            // ...
//                            Log.v("Downloader" , "File downloaded and stored");
//                        }
//                    }).addOnFailureListener(new OnFailureListener() {
//                @Override
//                public void onFailure(@NonNull Exception exception) {
//                    // Handle failed download
//                    // ...
//                }
//            });
//        }

        new DownloadImageAsync(this).execute(src);
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_my_qr:
                launchQr();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void launchQr() {
        Log.d(TAG, "onclick");
        Intent getBarcode = new Intent(MainActivity.this,QRBarcodeActivity.class);
        startActivity(getBarcode);
    }
}

