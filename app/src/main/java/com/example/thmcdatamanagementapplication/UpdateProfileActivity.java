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
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UpdateProfileActivity extends AppCompatActivity {

    private EditText editTextUpdateFullName, editTextUpdateAddress, editTextUpdatePhoneNo;
    private ProgressBar progressBar;
    private RadioGroup radioGroupUpdateGender;
    private RadioButton radioButtonSelectedGender;
    private String memberFullName, memberAddress, memberPhoneNo, memberGender;
    private FirebaseAuth authProfile;
    private static final String TAG = "RegisterActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_profile);

        getSupportActionBar().setTitle("Update Profile Details");

        progressBar = findViewById(R.id.progressBar);
        editTextUpdateAddress = findViewById(R.id.editText_update_address);
        editTextUpdateFullName = findViewById(R.id.editText_update_profile_name);
        editTextUpdatePhoneNo = findViewById(R.id.editText_update_profile_mobile);
        radioGroupUpdateGender = findViewById(R.id.radio_group_update_profile_gender);

        authProfile = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = authProfile.getCurrentUser();

        //show Profile data that exist
        showProfile(firebaseUser);

        Button updateProfileButton = findViewById(R.id.button_update_profile);
        updateProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateProfile(firebaseUser);
            }
        });

        TextView uploadPictureButton = findViewById(R.id.textView_profile_upload_pic);
        uploadPictureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent (UpdateProfileActivity.this, UploadProfilePictureActivity.class);
                startActivity(intent);
                finish();
            }
        });
        TextView updateEmailButton = findViewById(R.id.textView_profile_update_email);
        updateEmailButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent (UpdateProfileActivity.this, UpdateEmailActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private void updateProfile(FirebaseUser firebaseUser) {
        int selectedGenderID = radioGroupUpdateGender.getCheckedRadioButtonId();
        radioButtonSelectedGender = findViewById(selectedGenderID);
        //validate mobile number using matcher and pattern
        String mobileRegex = "[4-7][0-9]{9}"; //check if the first number is 4,5,6,7 in canadian line
        Matcher mobilematcher;
        Pattern mobilePattern = Pattern.compile(mobileRegex);
        mobilematcher = mobilePattern.matcher(memberPhoneNo);

        if (TextUtils.isEmpty(memberFullName)) {
            Toast.makeText(UpdateProfileActivity.this, "Please enter your full name", Toast.LENGTH_LONG).show();
            editTextUpdateFullName.setError("Full Name cannot be empty");
            editTextUpdateFullName.requestFocus();
        } else if (TextUtils.isEmpty(memberAddress)) {
            Toast.makeText(UpdateProfileActivity.this, "Please enter your home address", Toast.LENGTH_LONG).show();
            editTextUpdateAddress.setError("Home address is required");
            editTextUpdateAddress.requestFocus();
        } else if (TextUtils.isEmpty(radioButtonSelectedGender.getText())) {
            Toast.makeText(UpdateProfileActivity.this, "Please select your gender", Toast.LENGTH_LONG).show();
            radioButtonSelectedGender.setError("Gender is required");
            radioButtonSelectedGender.requestFocus();
        } else if (TextUtils.isEmpty(memberPhoneNo)) {
            Toast.makeText(UpdateProfileActivity.this, "Please enter your phone number", Toast.LENGTH_LONG).show();
            editTextUpdatePhoneNo.setError("Phone Number is required");
            editTextUpdatePhoneNo.requestFocus();
        } else if (memberPhoneNo.length() != 10) {
            Toast.makeText(UpdateProfileActivity.this, "Please re-enter your phone number", Toast.LENGTH_LONG).show();
            editTextUpdatePhoneNo.setError("Phone Number should be 10 digits");
            editTextUpdatePhoneNo.requestFocus();
        } else if (!mobilematcher.find()) {
            Toast.makeText(UpdateProfileActivity.this, "Please re-enter your phone number", Toast.LENGTH_LONG).show();
            editTextUpdatePhoneNo.setError("Phone Number is not valid");
            editTextUpdatePhoneNo.requestFocus();
        } else {
            memberGender = radioButtonSelectedGender.getText().toString();
//            //obtain entered data
            memberFullName= editTextUpdateFullName.getText().toString();
            memberAddress= editTextUpdateAddress.getText().toString();
            memberPhoneNo= editTextUpdatePhoneNo.getText().toString();
//
//            //write new data to db
//            MemberDAO memberDAO = new MemberDAO(memberAddress, memberGender, memberPhoneNo);

            //get ref of user from the "registered members" db
            DatabaseReference referenceProfile = FirebaseDatabase.getInstance().getReference("Registered Members");
            String userID = firebaseUser.getUid();

            progressBar.setVisibility(View.VISIBLE);
            // Retrieve the existing member data from the database
            DatabaseReference userReference = referenceProfile.child(userID);
            userReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    MemberDAO existingMember = snapshot.getValue(MemberDAO.class);

                    // Update only the required fields
                    if (existingMember != null) {
                        existingMember.setMemberFullName(memberFullName);
                        existingMember.setMemberAddress(memberAddress);
                        existingMember.setMemberGender(memberGender);
                        existingMember.setMemberPhoneNo(memberPhoneNo);

                        // Write the updated member data back to the database
                        userReference.setValue(existingMember).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    UserProfileChangeRequest profileUpdate = new UserProfileChangeRequest.Builder().setDisplayName(memberFullName).build();
                                    firebaseUser.updateProfile(profileUpdate);

                                    Toast.makeText(UpdateProfileActivity.this, "Update Successful", Toast.LENGTH_LONG).show();

                                    //stip user from returning to update profle
                                    //use flag for example when you want to either remove previous activity, for example if user
                                    //logged out, they shouldnt be able to backspace to previous task.
                                    Intent intent = new Intent(UpdateProfileActivity.this, UserProfileActivity.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(intent);
                                    finish(); //close register activity

                                } else {
                                    try {
                                        throw task.getException();
                                    } catch (Exception e) {
                                        Log.e(TAG, e.getMessage());
                                        Toast.makeText(UpdateProfileActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                                    }
                                }
                                progressBar.setVisibility(View.GONE);
                            }
                        });
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(UpdateProfileActivity.this, "Something went wrong", Toast.LENGTH_LONG).show();
                    progressBar.setVisibility(View.GONE);
                }
            });

            progressBar.setVisibility(View.VISIBLE);
        }

    }

    private void showProfile(FirebaseUser firebaseUser) {
        String userIDOfRegistered = firebaseUser.getUid();

        //get reference
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Registered Members");
        progressBar.setVisibility(View.VISIBLE);

        databaseReference.child(userIDOfRegistered).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                MemberDAO memberDAO= snapshot.getValue(MemberDAO.class);
                if(memberDAO != null){
//                    memberFullName = firebaseUser.getDisplayName();
                    memberFullName = memberDAO.memberFullName;
                    memberAddress = memberDAO.memberAddress;
                    memberGender = memberDAO.memberGender;
                    memberPhoneNo = memberDAO.memberPhoneNo;

                    editTextUpdateAddress.setText(memberAddress);
                    editTextUpdateFullName.setText(memberFullName);
                    editTextUpdatePhoneNo.setText(memberPhoneNo);

                    if (memberGender.equals("Male")){
                        radioButtonSelectedGender=findViewById(R.id.radio_male);
                    }else{
                        radioButtonSelectedGender=findViewById(R.id.radio_female);
                    }
                    radioButtonSelectedGender.setChecked(true);
                }else{
                    Toast.makeText(UpdateProfileActivity.this, "Something went wrong", Toast.LENGTH_LONG).show();
                }
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(UpdateProfileActivity.this, "Something went wrong", Toast.LENGTH_LONG).show();
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
            Intent intent = new Intent(UpdateProfileActivity.this, UpdateProfileActivity.class);
            startActivity(intent);
            finish();
        }
        else if (id == R.id.menu_update_email){
            Intent intent = new Intent(UpdateProfileActivity.this, UpdateEmailActivity.class);
            startActivity(intent);
            finish();
        }
////        else if (id == R.id.menu_setting){
////            Intent intent = new Intent(UploadProfilePictureActivity.this, SettingActivity.class);
////        startActivity(intent);
//          finish();
////        }
        else if (id == R.id.menu_change_password){
            Intent intent = new Intent(UpdateProfileActivity.this, ChangePasswordActivity.class);
            startActivity(intent);
            finish();
        }
        else if (id == R.id.menu_delete_profile){
            Intent intent = new Intent(UpdateProfileActivity.this, DeleteProfileActivity.class);
            startActivity(intent);
            finish();

        }
        else if (id == R.id.menu_logout){
            authProfile.signOut();
            Toast.makeText(UpdateProfileActivity.this, "Logged Out", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(UpdateProfileActivity.this, MainActivity.class);
            //clear stack to prevent signout users from accessing profile unless logged in aagian
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP| Intent.FLAG_ACTIVITY_CLEAR_TASK|
                    Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish(); //close userProfileActivity
        }
        else{
            //if no item was clicked
            Toast.makeText(UpdateProfileActivity.this, "Something went wrong. click menu item again", Toast.LENGTH_LONG).show();

        }
        return super.onOptionsItemSelected(item);
    }

}


/*
referenceProfile.child(userID).setValue(memberDAO).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            UserProfileChangeRequest profileUpdate = new UserProfileChangeRequest.Builder().setDisplayName(memberFullName).build();
                            firebaseUser.updateProfile(profileUpdate);

                            Toast.makeText(UpdateProfileActivity.this, "Update Successful", Toast.LENGTH_LONG).show();

                            //stip user from returning to update profle
                            //use flag for example when you want to either remove previous activity, for example if user
                            //logged out, they shouldnt be able to backspace to previous task.
                            Intent intent = new Intent(UpdateProfileActivity.this, UserProfileActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            finish(); //close register activity

                        }else{
                            try{
                                throw task.getException();
                            }catch(Exception e) {
                                Log.e(TAG, e.getMessage());
                                Toast.makeText(UpdateProfileActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        }progressBar.setVisibility(View.GONE);
                    }
                });

                progressBar.setVisibility(View.VISIBLE);
            }
 */