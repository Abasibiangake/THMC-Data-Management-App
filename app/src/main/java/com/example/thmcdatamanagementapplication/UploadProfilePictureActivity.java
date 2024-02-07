package com.example.thmcdatamanagementapplication;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.time.Instant;

public class UploadProfilePictureActivity extends AppCompatActivity {

    private ProgressBar progressBar;
    private ImageView imageViewUploadDp;
    private FirebaseAuth authProfile;

    private StorageReference storageReference;
    private FirebaseUser firebaseUser;

    private Uri uriImage;
    private static final int PICK_IMAGE_REQUEST = 1; //means true

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_profile_picture);

        getSupportActionBar().setTitle("Upload Profile Picture");

        Button buttonUploadPicOption = findViewById(R.id.upload_pic_choose_button);
        Button buttonUploadPic = findViewById(R.id.upload_pic_button);
        progressBar = findViewById(R.id.progressBar);
        imageViewUploadDp = findViewById(R.id.imageView_profile_dp);

        authProfile = FirebaseAuth.getInstance();
        firebaseUser = authProfile.getCurrentUser(); //gets the current user from firebase instance
        //creates a location as DisplayPics
        storageReference = FirebaseStorage.getInstance().getReference("DisplayPics");
        Uri uri = firebaseUser.getPhotoUrl();
        //setImageUri only works for local URI on the local disk file not a URL to an image on the net
        //set user current dp imageView from external location on web using Picasso
        Picasso.with(UploadProfilePictureActivity.this).load(uri).into(imageViewUploadDp);

        //if the user hasnt uploaed a pic before
        //open file explorer to select a picture
        buttonUploadPicOption.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFileExploxer();
            }
        });

        //upload image to firebase once a picture has been selected
        buttonUploadPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               progressBar.setVisibility(View.VISIBLE);
               UploadPic();
            }
        });
    }

    private void UploadPic() {
        if (uriImage != null){
            //save the image to logged user
            StorageReference fileReference = storageReference.child(authProfile.getCurrentUser().getDisplayName()+"."+
                    getFileExtension(uriImage));

            //upload image to storage. Putfile is used to upload
            fileReference.putFile(uriImage).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    fileReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            Uri downloadUri = uri;
                            firebaseUser = authProfile.getCurrentUser();

                            //finally set display name of user after uplaoding. for changes.
                            UserProfileChangeRequest profileChanges = new UserProfileChangeRequest.Builder().setPhotoUri(downloadUri).build();
                            firebaseUser.updateProfile(profileChanges);

                        }
                    });
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(UploadProfilePictureActivity.this, "Upload Successful", Toast.LENGTH_LONG).show();

                    Intent intent = new Intent(UploadProfilePictureActivity.this, UserProfileActivity.class);
                    startActivity(intent);
                    finish();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(UploadProfilePictureActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        }//if user doesnt select a picture but clicks upload
        else{
            progressBar.setVisibility(View.GONE);
            Toast.makeText(UploadProfilePictureActivity.this, "Please select a picture", Toast.LENGTH_LONG).show();

        }
    }
    //obtain file extension of the image
    private String getFileExtension(Uri uriImage) {
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton(); //map mime to file ext
        return mime.getExtensionFromMimeType(contentResolver.getType(uriImage));

    }

    private void openFileExploxer() {
        Intent intent = new Intent();
        intent.setType("image/*");
        //user can choose options based on MIME
        // (Multipurpose Internet Mail Extension) type use ACTION_GET_CONTENT
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //if true, and result is ok and data ia not null or data.getdatat is not empty
        if(requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null){
            uriImage = data.getData();
            imageViewUploadDp.setImageURI(uriImage);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //inflate with menu created
        getMenuInflater().inflate(R.menu.thmc_menu, menu);

        return super.onCreateOptionsMenu(menu);
    }

    //when a menu item is selected
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if(id==R.id.menu_refresh){
            //refresh
            startActivity(getIntent());
            finish();
            //remove animation and directly refresh
            overridePendingTransition(0, 0);
        }
        else if (id == R.id.menu_update_profile){
            Intent intent = new Intent(UploadProfilePictureActivity.this, UpdateProfileActivity.class);
            startActivity(intent);
            finish();
        }
        else if (id == R.id.menu_update_email){
            Intent intent = new Intent(UploadProfilePictureActivity.this, UpdateEmailActivity.class);
            startActivity(intent);
            finish();
        }
////        else if (id == R.id.menu_setting){
////            Intent intent = new Intent(UploadProfilePictureActivity.this, SettingActivity.class);
////        startActivity(intent);
        //          finish();

////        }
        else if (id == R.id.menu_change_password){
            Intent intent = new Intent(UploadProfilePictureActivity.this, ChangePasswordActivity.class);
            startActivity(intent);
            finish();
        }
//        else if (id == R.id.menu_delete_profile){
//            Intent intent = new Intent(UploadProfilePictureActivity.this, DeleteProfileActivity.class);
//            startActivity(intent);
        //          finish();

//        }
        else if (id == R.id.menu_logout){
            authProfile.signOut();
            Toast.makeText(UploadProfilePictureActivity.this, "Logged Out", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(UploadProfilePictureActivity.this, MainActivity.class);
            //clear stack to prevent signout users from accessing profile unless logged in aagian
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP| Intent.FLAG_ACTIVITY_CLEAR_TASK|
                    Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish(); //close userProfileActivity
        }
        else{
            //if no item was clicked
            Toast.makeText(UploadProfilePictureActivity.this, "Something went wrong. click menu item again", Toast.LENGTH_LONG).show();

        }
        return super.onOptionsItemSelected(item);
    }
}