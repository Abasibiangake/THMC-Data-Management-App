package com.example.thmcdatamanagementapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

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
import com.google.firebase.auth.EmailAuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ChangePasswordActivity extends AppCompatActivity {
    private FirebaseAuth authProfile;
    private FirebaseUser firebaseUser;
    private ProgressBar progressBar;
    private TextView textViewAuthenticated;
    private String memberCurrentPassword, memberNewPassword;
    private Button buttonUpdatePassword, authenticationButton;
    private EditText editTextNewPassword, editTextCurrentPassword, editConfirmNewPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        getSupportActionBar().setTitle("Update Password");

        progressBar = findViewById(R.id.progressBar);
        editTextNewPassword = findViewById(R.id.editText_change_pwd_new);
        editTextCurrentPassword = findViewById(R.id.editText_change_pwd_current);
        textViewAuthenticated = findViewById(R.id.textView_change_pwd_authenticated);
        buttonUpdatePassword = findViewById(R.id.button_change_pwd);
        authenticationButton = findViewById(R.id.button_change_pwd_authenticate);

        buttonUpdatePassword.setEnabled(false); //disabled until user is authenticated.
        editTextNewPassword.setEnabled(false);

        authProfile = FirebaseAuth.getInstance();
        firebaseUser = authProfile.getCurrentUser();

        if (firebaseUser.equals("")){
            Toast.makeText(ChangePasswordActivity.this, "Something is wrong", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(ChangePasswordActivity.this, UserProfileActivity.class);
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
                    Toast.makeText(ChangePasswordActivity.this, "Password is needed", Toast.LENGTH_SHORT).show();
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
                                editTextNewPassword.setEnabled(true);
//                                editConfirmNewPassword.setEnabled(true);
                                buttonUpdatePassword.setEnabled(true);
                                authenticationButton.setEnabled(false);

                                Toast.makeText(ChangePasswordActivity.this, "Password has been verified. Reset Now!", Toast.LENGTH_SHORT).show();

                                //set TextView to show that the member has been authenticated
                                textViewAuthenticated.setText("You are authenticated. You can reset password now!");


                                //change button color
//                              buttonUpdatePassword.setBackgroundTintList(ContextCompat.getColorStateList(UpdateEmailActivity.this,
//                                    R.color.dark_green));
                                buttonUpdatePassword.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        changePassword(firebaseUser);

                                    }
                                });
                            }else {
                                try {
                                    throw task.getException();
                                } catch (Exception e) {
                                    Toast.makeText(ChangePasswordActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
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

    private void changePassword(FirebaseUser firebaseUser) {
        memberNewPassword = editTextNewPassword.getText().toString();
        
        if (TextUtils.isEmpty(memberNewPassword)){
            Toast.makeText(ChangePasswordActivity.this, "New Password is needed", Toast.LENGTH_SHORT).show();
            editTextNewPassword.setError("Please Enter A New Password");
            editTextNewPassword.requestFocus();
        }else if (memberNewPassword.matches(memberCurrentPassword)){
            Toast.makeText(ChangePasswordActivity.this, "New password cannot match the old password", Toast.LENGTH_SHORT).show();
            editTextNewPassword.setError("Please Enter A New Password");
            editTextNewPassword.requestFocus();
        }else{
            progressBar.setVisibility(View.VISIBLE);
            firebaseUser.updatePassword(memberNewPassword).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()){
                        Toast.makeText(ChangePasswordActivity.this, "Password update was successful.", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(ChangePasswordActivity.this, UserProfileActivity.class);
                        startActivity(intent);
                        finish();
                    }else{
                        try {
                            throw task.getException();
                        } catch (Exception e) {
//                            Log.e("Change Password Activity:", "Password update failed", e);
                            Toast.makeText(ChangePasswordActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                    progressBar.setVisibility(View.GONE);
                }
            });
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
            Intent intent = new Intent(ChangePasswordActivity.this, UpdateProfileActivity.class);
            startActivity(intent);
            finish();
        }
        else if (id == R.id.menu_update_email){
            Intent intent = new Intent(ChangePasswordActivity.this, UpdateEmailActivity.class);
            startActivity(intent);
            finish();
        }
////        else if (id == R.id.menu_setting){
////            Intent intent = new Intent(UpdateEmailActivity.this, SettingActivity.class);
////        startActivity(intent);
//          finish();
////        }
        else if (id == R.id.menu_change_password){
            Intent intent = new Intent(ChangePasswordActivity.this, ChangePasswordActivity.class);
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
            Toast.makeText(ChangePasswordActivity.this, "Logged Out", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(ChangePasswordActivity.this, MainActivity.class);
            //clear stack to prevent signout users from accessing profile unless logged in aagian
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP| Intent.FLAG_ACTIVITY_CLEAR_TASK|
                    Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish(); //close userProfileActivity
        }
        else{
            //if no item was clicked
            Toast.makeText(ChangePasswordActivity.this, "Something went wrong. click menu item again", Toast.LENGTH_LONG).show();

        }
        return super.onOptionsItemSelected(item);
    }
}