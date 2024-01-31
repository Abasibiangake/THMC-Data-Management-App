package com.example.thmcdatamanagementapplication;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Firebase;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

//import com.google.firebase.auth.FirebaseAuth;

public class RegisterActivity extends AppCompatActivity {

    private EditText editTextRegisterFullName, editTextRegisterEmail, editTextRegisterAddress,
            editTextRegisterPhoneNo, editTextRegisterPassword, editTextRegisterConfirmPassword;

    private ProgressBar progressBar;
    private RadioGroup radioGroupRegisterGender;
    private RadioButton radioButtonSelectedGender;
    private static final String TAG = "RegisterActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        getSupportActionBar().setTitle("Register");
        Toast.makeText(RegisterActivity.this, "You can register now", Toast.LENGTH_LONG).show();

        progressBar = findViewById(R.id.progressBar);
        editTextRegisterFullName = findViewById(R.id.editText_register_full_name);
        editTextRegisterEmail = findViewById(R.id.editText_register_email);
        editTextRegisterAddress = findViewById(R.id.editText_register_address);
        editTextRegisterPhoneNo = findViewById(R.id.editText_register_mobile);
        editTextRegisterPassword = findViewById(R.id.editText_register_password);
        editTextRegisterConfirmPassword = findViewById(R.id.editText_register_confirm_password);

        //view for radio button
        radioGroupRegisterGender = findViewById(R.id.radio_group_register_gender);
        radioGroupRegisterGender.clearCheck();

        Button buttonRegister = findViewById(R.id.button_register);
        buttonRegister.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                int selectedGenderId = radioGroupRegisterGender.getCheckedRadioButtonId();
                radioButtonSelectedGender = findViewById(selectedGenderId);

                //obtain entered data
                String memberFullName= editTextRegisterFullName.getText().toString();
                String memberEmail= editTextRegisterEmail.getText().toString();
                String memberAddress= editTextRegisterAddress.getText().toString();
                String memberPhoneNo= editTextRegisterPhoneNo.getText().toString();
                String memberPassword= editTextRegisterPassword.getText().toString();
                String memberConfirmPassword= editTextRegisterConfirmPassword.getText().toString();
                String memberGender;

                if (TextUtils.isEmpty(memberFullName)){
                    Toast.makeText(RegisterActivity.this, "Please enter your full name", Toast.LENGTH_LONG).show();
                    editTextRegisterFullName.setError("Full Name cannot be empty");
                    editTextRegisterFullName.requestFocus();
                }else if (TextUtils.isEmpty(memberEmail)){
                    Toast.makeText(RegisterActivity.this, "Please enter your email address", Toast.LENGTH_LONG).show();
                    editTextRegisterEmail.setError("Email cannot be empty");
                    editTextRegisterEmail.requestFocus();
                }else if (!Patterns.EMAIL_ADDRESS.matcher(memberEmail).matches()){
                    Toast.makeText(RegisterActivity.this, "Please re-enter your email", Toast.LENGTH_LONG).show();
                    editTextRegisterEmail.setError("Valid email is required");
                    editTextRegisterEmail.requestFocus();
                }else if (TextUtils.isEmpty(memberAddress)){
                    Toast.makeText(RegisterActivity.this, "Please enter your home address", Toast.LENGTH_LONG).show();
                    editTextRegisterAddress.setError("Home address is required");
                    editTextRegisterAddress.requestFocus();
                }else if (radioGroupRegisterGender.getCheckedRadioButtonId() == -1) {
                    Toast.makeText(RegisterActivity.this, "Please select your gender", Toast.LENGTH_LONG).show();
                    radioButtonSelectedGender.setError("Gender is required");
                    radioButtonSelectedGender.requestFocus();
                }else if (TextUtils.isEmpty(memberPhoneNo)){
                    Toast.makeText(RegisterActivity.this, "Please enter your phone number", Toast.LENGTH_LONG).show();
                    editTextRegisterPhoneNo.setError("Phone Number is required");
                    editTextRegisterPhoneNo.requestFocus();
                }else if (memberPhoneNo.length() != 10){
                    Toast.makeText(RegisterActivity.this, "Please re-enter your phone number", Toast.LENGTH_LONG).show();
                    editTextRegisterPhoneNo.setError("Phone Number should be 10 digits");
                    editTextRegisterPhoneNo.requestFocus();
                }
                else if (TextUtils.isEmpty(memberPassword)){
                    Toast.makeText(RegisterActivity.this, "Please enter your password", Toast.LENGTH_LONG).show();
                    editTextRegisterPassword.setError("Password cannot be empty");
                    editTextRegisterPassword.requestFocus();
                }
                else if (memberPassword.length() < 6){
                    Toast.makeText(RegisterActivity.this, "Password should be at least 6 characters", Toast.LENGTH_LONG).show();
                    editTextRegisterPassword.setError("Password is too weak");
                    editTextRegisterPassword.requestFocus();
                }
                else if (TextUtils.isEmpty(memberConfirmPassword)){
                    Toast.makeText(RegisterActivity.this, "Please confirm your password", Toast.LENGTH_LONG).show();
                    editTextRegisterConfirmPassword.setError("Password confirmation is required");
                    editTextRegisterConfirmPassword.requestFocus();
                }
                else if (!memberPassword.equals(memberConfirmPassword)){
                    Toast.makeText(RegisterActivity.this, "Please re-enter the same password", Toast.LENGTH_LONG).show();
                    editTextRegisterConfirmPassword.setError("Password not identical");
                    editTextRegisterConfirmPassword.requestFocus();

                    //clear the passwords once matched
                    editTextRegisterPassword.clearComposingText();
                    editTextRegisterConfirmPassword.clearComposingText();
                }else{
                    memberGender = radioButtonSelectedGender.getText().toString();
                    progressBar.setVisibility(View.VISIBLE);
                    registerUser(memberFullName, memberEmail, memberAddress, memberGender, memberPhoneNo, memberPassword);
                }


            }
        });

    }

    private void registerUser(String memberFullName, String memberEmail, String memberAddress, String memberGender, String memberPhoneNo, String memberPassword) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        //create member profile
        auth.createUserWithEmailAndPassword(memberEmail, memberPassword).addOnCompleteListener(RegisterActivity.this,
                new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            //verify email
                            FirebaseUser firebaseUser = auth.getCurrentUser();

                            //extracting the user details from registered users
                            DatabaseReference referenceProfile = FirebaseDatabase.getInstance().getReference("Registered Members");

                            Log.d(TAG, "Firebase UID: " + firebaseUser.getUid());
                            //update member display name in profile
                            UserProfileChangeRequest profileChangeRequest = new UserProfileChangeRequest.Builder().setDisplayName(memberFullName).build();
                            firebaseUser.updateProfile(profileChangeRequest);

                            //Enter User Data into Firebase Realtime Database
                            MemberDAO memberDAO = new MemberDAO(firebaseUser.getUid(), memberFullName, memberAddress, memberEmail, memberGender, memberPhoneNo);

                            referenceProfile.child(memberFullName).setValue(memberDAO).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()){
                                        //send verification email
                                        firebaseUser.sendEmailVerification();
                                        //display text
                                        Toast.makeText(RegisterActivity.this, "User registration is successful. Please verify your email", Toast.LENGTH_LONG).show();

                                        //open userprofile after successful registration
                                        Intent intent = new Intent(RegisterActivity.this, UserProfileActivity.class);

                                        //use flag for example when you want to either remove previous activity, for example if user
                                        //logged out, they shouldnt be able to backspace to previous task.
                                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                        startActivity(intent);
                                        finish(); //close register activity

                                    }else{
                                        //display text
                                        Toast.makeText(RegisterActivity.this, "User registration failed. Please try again", Toast.LENGTH_LONG).show();
                                    }

                                }
                            });
                            //remove progressbar if succesful
                            progressBar.setVisibility(View.GONE);

                        }else{
                            try{
                                throw task.getException();
                            }catch (FirebaseAuthWeakPasswordException e){
                                editTextRegisterPassword.setError("Your password is too weak. Kindly use a mix of alphabet, number and special characters.");
                                editTextRegisterPassword.requestFocus();
                            }catch(FirebaseAuthInvalidCredentialsException e){
                                editTextRegisterEmail.setError("Your email is invalid or already in use. Kindly re-enter.");
                                editTextRegisterEmail.requestFocus();
                            }catch(FirebaseAuthUserCollisionException e){
                                Log.e(TAG, "User is already registered with this email. Please use another email");
                                editTextRegisterEmail.setError("User is already registered with this email. Please use another email");
                                editTextRegisterEmail.requestFocus();
                            }catch(Exception e){
                                Log.e(TAG, e.getMessage());
                                Toast.makeText(RegisterActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();

                            }
                            progressBar.setVisibility(View.GONE);

                        }
                    }
                });
    }
}