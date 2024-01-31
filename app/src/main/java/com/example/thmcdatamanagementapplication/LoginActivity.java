package com.example.thmcdatamanagementapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;

import java.util.regex.Pattern;

public class LoginActivity extends AppCompatActivity {
    private EditText editTextLoginEmail, editTextLoginPwd;
    private ProgressBar progressBar;
    private FirebaseAuth authProfile;
    private static final String TAG = "Login Activity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        getSupportActionBar().setTitle("Login");

        editTextLoginEmail = findViewById(R.id.editText_login_email);
        editTextLoginPwd = findViewById(R.id.editText_login_pwd);
        progressBar = findViewById(R.id.progressBar);

        authProfile = FirebaseAuth.getInstance();

        TextView forgotPwdButton = findViewById(R.id.textView_forgot_password_link);
        forgotPwdButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(LoginActivity.this, "You can reset password now!", Toast.LENGTH_LONG).show();
                startActivity(new Intent(LoginActivity.this, ForgotPasswordActivity.class));
            }
        });


        //show hide password using eye icon
        ImageView imageViewShowHidePwd = findViewById(R.id.imageView_show_hide_pwd);
        imageViewShowHidePwd.setImageResource(R.drawable.ic_hide_pwd);
        imageViewShowHidePwd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //check if the password is already visible when clicked, hence hide it now.
                if(editTextLoginPwd.getTransformationMethod().equals(HideReturnsTransformationMethod.getInstance())){
                    //hide
                    editTextLoginPwd.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    //change icon
                    imageViewShowHidePwd.setImageResource(R.drawable.ic_hide_pwd);
                }
                else{
                    //show password
                    editTextLoginPwd.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    //change icon
                    imageViewShowHidePwd.setImageResource(R.drawable.ic_show_pwd);
                }
            }
        });


        //Login member
        Button buttonLogin = findViewById(R.id.button_login);
        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String memberEmail = editTextLoginEmail.getText().toString();
                String memberPwd = editTextLoginPwd.getText().toString();

                if (TextUtils.isEmpty(memberEmail)){
                    Toast.makeText(LoginActivity.this, "Please select your email address", Toast.LENGTH_LONG).show();
                    editTextLoginEmail.setError("Email is required");
                    editTextLoginEmail.requestFocus();
                }else if (!Patterns.EMAIL_ADDRESS.matcher(memberEmail).matches()){
                    Toast.makeText(LoginActivity.this, "Please re-enter a valid email address", Toast.LENGTH_LONG).show();
                    editTextLoginEmail.setError("Valid email is required");
                    editTextLoginEmail.requestFocus();
                }
                else if (TextUtils.isEmpty(memberPwd)){
                    Toast.makeText(LoginActivity.this, "Please select your password", Toast.LENGTH_LONG).show();
                    editTextLoginPwd.setError("Password is required");
                    editTextLoginPwd.requestFocus();
                }
                else{
                    progressBar.setVisibility(View.VISIBLE);
                    loginUser(memberEmail, memberPwd);
                }
            }
        });
    }

    private void loginUser(String email, String password) {
        authProfile.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()){

                    //Get instance of current user
                    FirebaseUser firebaseUser = authProfile.getCurrentUser();
                    if(firebaseUser.isEmailVerified()){
                        Toast.makeText(LoginActivity.this, "Login Successful!", Toast.LENGTH_LONG).show();
                        startActivity(new Intent(LoginActivity.this, UserProfileActivity.class));
                        finish(); //close login
                    }
                    else{
                        firebaseUser.sendEmailVerification();
                        authProfile.signOut();
                        showAlertDialog();
                    }
                }
                else{
                    try{
                        throw task.getException();
                    }
                    catch (FirebaseAuthInvalidCredentialsException e){
                        editTextLoginEmail.setError("Invalid credentials. Kindly check and re-enter");
                        editTextLoginEmail.requestFocus();
                    }
                    catch (FirebaseAuthInvalidUserException e){
                        editTextLoginEmail.setError("Member does not exist or is no longer valid. Please register again");
                        editTextLoginEmail.requestFocus();
                    } catch (Exception e) {
                        Log.e(TAG, e.getMessage());
                        Toast.makeText(LoginActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        throw new RuntimeException(e);
                    }
                    Toast.makeText(LoginActivity.this, "Please try entering email and password again", Toast.LENGTH_LONG).show();
                }
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    private void showAlertDialog() {
        //setup
        AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
        builder.setTitle("Email not verified!");
        builder.setMessage("Please verify your email to proceed to login!");

        //open email
        builder.setPositiveButton("Continue", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_APP_EMAIL);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // launches email as a separate app and not in the thmc app.
                startActivity(intent);
            }
        });
        //create
        AlertDialog alertDialog = builder.create();

        //show
        alertDialog.show();
    }

    //we want the members to stay logged in once logged in. So remain logged in
    //still you log out. E.g facebook, instagram.
    //if logged in, redirect user to profile.
    @Override
    protected void onStart() {
        super.onStart();
        if (authProfile.getCurrentUser() != null){
            Toast.makeText(LoginActivity.this, "Already Logged in!", Toast.LENGTH_LONG).show();
            //then start UserProfileActivity
            startActivity(new Intent(LoginActivity.this, UserProfileActivity.class));
            finish(); //close login
        }
        else{
            Toast.makeText(LoginActivity.this, "You can log in now!", Toast.LENGTH_LONG).show();
        }
    }
}