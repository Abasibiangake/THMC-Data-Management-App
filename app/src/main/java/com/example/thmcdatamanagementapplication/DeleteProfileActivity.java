package com.example.thmcdatamanagementapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class DeleteProfileActivity extends AppCompatActivity {
    private FirebaseAuth authProfile;
    private FirebaseUser firebaseUser;
    private ProgressBar progressBar;
    private TextView textViewAuthenticated;
    private String memberCurrentPassword;
    private Button buttonDeletePassword, authenticationButton;
    private EditText editTextCurrentPassword;
    private static final String TAG = "DeleteProfile activity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delete_profile);

        setContentView(R.layout.activity_change_password);

        getSupportActionBar().setTitle("Delete Profile");

        progressBar = findViewById(R.id.progressBar);
        editTextCurrentPassword = findViewById(R.id.editText_delete_user_pwd);
        textViewAuthenticated = findViewById(R.id.textView_delete_user_authenticated);
        buttonDeletePassword = findViewById(R.id.button_delete_user);
        authenticationButton = findViewById(R.id.button_delete_user_authenticate);

        buttonDeletePassword.setEnabled(false); //disabled until user is authenticated.

        authProfile = FirebaseAuth.getInstance();
        firebaseUser = authProfile.getCurrentUser();

        if (firebaseUser.equals("")){
            Toast.makeText(DeleteProfileActivity.this, "Something is wrong", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(DeleteProfileActivity.this, UserProfileActivity.class);
            startActivity(intent);
            finish();
        }else{
            reAuthenticate(firebaseUser);
        }
    }


    private void reAuthenticate(FirebaseUser firebaseUser) {
        authenticationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                memberCurrentPassword = editTextCurrentPassword.getText().toString();
                if (TextUtils.isEmpty(memberCurrentPassword)){
                    Toast.makeText(DeleteProfileActivity.this, "Password is needed", Toast.LENGTH_SHORT).show();
                    editTextCurrentPassword.setError("Please Enter Password for Authentication");
                    editTextCurrentPassword.requestFocus();
                }else{
                    progressBar.setVisibility(View.VISIBLE);
                    //reauth
                    AuthCredential authCredential = EmailAuthProvider.getCredential(firebaseUser.getEmail(),memberCurrentPassword);
                    firebaseUser.reauthenticate(authCredential).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                progressBar.setVisibility(View.GONE);

                                //enable edittext for new email and disable process for authentication
                                editTextCurrentPassword.setEnabled(false);
                                buttonDeletePassword.setEnabled(true);
                                authenticationButton.setEnabled(false);

                                Toast.makeText(DeleteProfileActivity.this, "Password has been verified. Delete Now!", Toast.LENGTH_SHORT).show();

                                //set TextView to show that the member has been authenticated
                                textViewAuthenticated.setText("You are authenticated. You can delete profile now! Be careful, this action is irreversible");

                                buttonDeletePassword.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        showAlertDialog();

                                    }
                                });
                            }else {
                                try {
                                    throw task.getException();
                                } catch (Exception e) {
                                    Toast.makeText(DeleteProfileActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                                }
                            }
                            progressBar.setVisibility(View.GONE);
                        }
                        //xx
                    });
                }
            }
        });
    }
    //after successful registration, show alert if email isn't verified
    private void showAlertDialog() {
        //setup
        AlertDialog.Builder builder = new AlertDialog.Builder(DeleteProfileActivity.this);
        builder.setTitle("Delete Member and its Data From Database");
        builder.setMessage("Do you really want to delete this profile? This action is irreversible!");

        //open email
        builder.setPositiveButton("Continue", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
//                 deleteProfile(firebaseUser);
                deleteMemberData(firebaseUser);
            }
        });
        //return to user profile if user presses cancel button
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(DeleteProfileActivity.this, UserProfileActivity.class);
                startActivity(intent);
                finish();
            }
        });

        //create
        AlertDialog alertDialog = builder.create();

        //change button color
        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.red));
            }
        });

        //show
        alertDialog.show();
    }

    private void deleteProfile() {
       firebaseUser.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
           @Override
           public void onComplete(@NonNull Task<Void> task) {
               if(task.isSuccessful()){
                   Toast.makeText(DeleteProfileActivity.this, "User profile has been deleted successfully", Toast.LENGTH_SHORT).show();
                   Intent intent = new Intent(DeleteProfileActivity.this, MainActivity.class);
                   startActivity(intent);
                   finish();
               }else {
                   try {
                       throw task.getException();
                   }catch (Exception e){
                       Toast.makeText(DeleteProfileActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                   }
               }
               progressBar.setVisibility(View.GONE);
           }
       });
    }

    private void deleteMemberData(FirebaseUser firebaseUser) {
        //check if user has photo uploaded
        if (firebaseUser.getPhotoUrl() != null){
            //detele profile pic
            FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
            StorageReference storageReference = firebaseStorage.getReferenceFromUrl(firebaseUser.getPhotoUrl().toString());
            storageReference.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void unused) {
                    Log.d(TAG, "OnSucess: Photo Deleted");
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.d(TAG, "OnFAil: Photo Failed to Delete");
                    Toast.makeText(DeleteProfileActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        }

        //delete data from DB
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Registered Members");
        databaseReference.child(firebaseUser.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>(){
            @Override
            public void onSuccess(Void unused) {
                Log.d(TAG, "OnSucess: Data Deleted");
                //now delete user authentication details
                deleteProfile();
            }
        }).addOnFailureListener(new OnFailureListener(){
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, e.getMessage());
                Toast.makeText(DeleteProfileActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });



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
            Intent intent = new Intent(DeleteProfileActivity.this, UpdateProfileActivity.class);
            startActivity(intent);
            finish();
        }
        else if (id == R.id.menu_update_email){
            Intent intent = new Intent(DeleteProfileActivity.this, UpdateEmailActivity.class);
            startActivity(intent);
            finish();
        }
////        else if (id == R.id.menu_setting){
////            Intent intent = new Intent(UpdateEmailActivity.this, SettingActivity.class);
////        startActivity(intent);
//          finish();
////        }
        else if (id == R.id.menu_change_password){
            Intent intent = new Intent(DeleteProfileActivity.this, ChangePasswordActivity.class);
            startActivity(intent);
            finish();

        }
        else if (id == R.id.menu_delete_profile){
            Intent intent = new Intent(DeleteProfileActivity.this, DeleteProfileActivity.class);
            startActivity(intent);
            finish();

        }
        else if (id == R.id.menu_logout){
            authProfile.signOut();
            Toast.makeText(DeleteProfileActivity.this, "Logged Out", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(DeleteProfileActivity.this, MainActivity.class);
            //clear stack to prevent signout users from accessing profile unless logged in aagian
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP| Intent.FLAG_ACTIVITY_CLEAR_TASK|
                    Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish(); //close userProfileActivity
        }
        else{
            //if no item was clicked
            Toast.makeText(DeleteProfileActivity.this, "Something went wrong. click menu item again", Toast.LENGTH_LONG).show();

        }
        return super.onOptionsItemSelected(item);
    }
}