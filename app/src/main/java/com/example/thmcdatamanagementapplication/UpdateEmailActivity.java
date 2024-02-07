package com.example.thmcdatamanagementapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class UpdateEmailActivity extends AppCompatActivity {
    private FirebaseAuth authProfile;
    private FirebaseUser firebaseUser;
    private ProgressBar progressBar;
    private TextView textViewAuthenticated;
    private String memberOldEmail, memberNewEmail, memberPassword;
    private Button buttonUpdateEmail, authenticationButton;
    private EditText editTextNewEmail, editTextPassword;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_email);

        getSupportActionBar().setTitle("Update Email");

        progressBar = findViewById(R.id.progressBar);
        editTextPassword = findViewById(R.id.editText_update_email_verify_password);
        editTextNewEmail = findViewById(R.id.editText_update_email_new);
        textViewAuthenticated = findViewById(R.id.textView_update_email_authenticated);
        buttonUpdateEmail = findViewById(R.id.button_update_email);


        buttonUpdateEmail.setEnabled(false); //disabled until user is authenticated.
        editTextNewEmail.setEnabled(false);

        authProfile = FirebaseAuth.getInstance();
        firebaseUser = authProfile.getCurrentUser();

        //set old email ID on textView
        memberOldEmail = firebaseUser.getEmail();
        TextView textViewOldEmail = findViewById(R.id.textView_update_email_old);
        textViewOldEmail.setText(memberOldEmail);

        if (firebaseUser.equals("")){
            Toast.makeText(UpdateEmailActivity.this, "Something is wrong", Toast.LENGTH_SHORT).show();
        }else{
            reAuthenticate(firebaseUser);
        }
    }

    private void reAuthenticate(FirebaseUser firebaseUser) {
        authenticationButton = findViewById(R.id.button_authenticate_user);
        authenticationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //get the password for authentication
                memberPassword = editTextPassword.getText().toString();
                if (TextUtils.isEmpty(memberPassword)){
                    Toast.makeText(UpdateEmailActivity.this, "Password is needed", Toast.LENGTH_SHORT).show();
                    editTextPassword.setError("Please Enter Password for Authentication");
                    editTextPassword.requestFocus();
                }
                else{
                    progressBar.setVisibility(View.VISIBLE);
                    AuthCredential authCredential = EmailAuthProvider.getCredential(memberOldEmail, memberPassword);
                    // Prompt the user to re-provide their sign-in credentials
                    firebaseUser.reauthenticate(authCredential).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                progressBar.setVisibility(View.GONE);

                                //enable edittext for new email and disable process for authentication
                                editTextNewEmail.setEnabled(true);
                                editTextPassword.setEnabled(false);
                                buttonUpdateEmail.setEnabled(true);
                                authenticationButton.setEnabled(false);

                                Toast.makeText(UpdateEmailActivity.this, "Password has been verified. Reset Now!", Toast.LENGTH_SHORT).show();

                                //set TextView to show that the member has been authenticated
                                textViewAuthenticated.setText("You are authenticated. You can reset email now!");

                                //change button color
//                            buttonUpdateEmail.setBackgroundTintList(ContextCompat.getColorStateList(UpdateEmailActivity.this,
//                                    R.color.dark_green));
                                buttonUpdateEmail.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        memberNewEmail = editTextNewEmail.getText().toString();
                                        if(TextUtils.isEmpty(memberNewEmail)){
                                            Toast.makeText(UpdateEmailActivity.this, "A new email is required", Toast.LENGTH_SHORT).show();
                                            editTextNewEmail.setError("Please Enter A New Email");
                                            editTextNewEmail.requestFocus();
                                        }else if(!Patterns.EMAIL_ADDRESS.matcher(memberNewEmail).matches()){
                                            Toast.makeText(UpdateEmailActivity.this, "A vald email is required", Toast.LENGTH_SHORT).show();
                                            editTextNewEmail.setError("Please Enter A Valid Email");
                                            editTextNewEmail.requestFocus();
                                        }else if (memberOldEmail.matches(memberNewEmail)){
                                            Toast.makeText(UpdateEmailActivity.this, "New email cannot match the old email", Toast.LENGTH_SHORT).show();
                                            editTextNewEmail.setError("Please Enter A New Email");
                                            editTextNewEmail.requestFocus();
                                        }else{
                                            progressBar.setVisibility(View.VISIBLE);
                                            updateEmail(firebaseUser);
                                        }
                                    }
                                });
                            }else {
                                try {
                                    throw task.getException();
                                } catch (Exception e) {
                                    Toast.makeText(UpdateEmailActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                                }
                            }
                        }
                    //xx
                    });
                }
            }
        });
    }

    private void updateEmail(FirebaseUser firebaseUser) {
        firebaseUser.updateEmail(memberNewEmail).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    //verify email
                    firebaseUser.sendEmailVerification();
                    Log.d("Update Email Activity:", "Updated email: " + firebaseUser.getEmail());
                    Toast.makeText(UpdateEmailActivity.this, "Email update was successful. Please check inbox to verify email", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(UpdateEmailActivity.this, UserProfileActivity.class);
                    startActivity(intent);
                    finish();
                }else{
                    try {
                        throw task.getException();
                    } catch (Exception e) {
                        Log.e("Update Email Activity:", "Email update failed", e);
                        Toast.makeText(UpdateEmailActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
                progressBar.setVisibility(View.GONE);
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
            Intent intent = new Intent(UpdateEmailActivity.this, UpdateProfileActivity.class);
            startActivity(intent);
            finish();
        }
        else if (id == R.id.menu_update_email){
            Intent intent = new Intent(UpdateEmailActivity.this, UpdateEmailActivity.class);
            startActivity(intent);
            finish();
        }
////        else if (id == R.id.menu_setting){
////            Intent intent = new Intent(UpdateEmailActivity.this, SettingActivity.class);
////        startActivity(intent);
//          finish();
////        }
        else if (id == R.id.menu_change_password){
            Intent intent = new Intent(UpdateEmailActivity.this, ChangePasswordActivity.class);
            startActivity(intent);
            finish();

        }
//        else if (id == R.id.menu_delete_profile){
//            Intent intent = new Intent(UpdateEmailActivity.this, DeleteProfileActivity.class);
//            startActivity(intent);
        //          finish();

//        }
        else if (id == R.id.menu_logout){
            authProfile.signOut();
            Toast.makeText(UpdateEmailActivity.this, "Logged Out", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(UpdateEmailActivity.this, MainActivity.class);
            //clear stack to prevent signout users from accessing profile unless logged in aagian
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP| Intent.FLAG_ACTIVITY_CLEAR_TASK|
                    Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish(); //close userProfileActivity
        }
        else{
            //if no item was clicked
            Toast.makeText(UpdateEmailActivity.this, "Something went wrong. click menu item again", Toast.LENGTH_LONG).show();

        }
        return super.onOptionsItemSelected(item);
    }
}