package com.example.thmcdatamanagementapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class UserProfileActivity extends AppCompatActivity {

    private TextView textViewWelcome, textViewFullName, textViewAddress, textViewEmail,
            textViewGender, textViewPhoneNo;
    private ProgressBar progressBar;
    private String fullName, email, address, gender, phoneNo;
    private ImageView imageView;
    private FirebaseAuth authProfile;
    private static final String TAG= "UserProfileActivity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        getSupportActionBar().setTitle("Home");
        textViewWelcome = findViewById(R.id.textView_show_welcome);
        textViewAddress = findViewById(R.id.textView_show_address);
        textViewEmail = findViewById(R.id.textView_show_email);
        textViewGender = findViewById(R.id.textView_show_gender);
        textViewPhoneNo = findViewById(R.id.textView_show_mobile);
        textViewFullName = findViewById(R.id.textView_show_full_name);
        progressBar = findViewById(R.id.progress_bar);

        //set onclicklistener on the profile image to open the uploadProfilePictureActivity
        imageView = findViewById(R.id.imageView_profile_dp);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UserProfileActivity.this, UploadProfilePictureActivity.class);
                startActivity(intent); // dont use finish so after upload it returns to the user profile page.

            }
        });

        authProfile = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = authProfile.getCurrentUser();

        //check if current user is logged in
        if (firebaseUser == null){
            Toast.makeText(UserProfileActivity.this, "Something is wrong. " +
                    "User Profile not available at the moment", Toast.LENGTH_LONG).show();
        }
        else{

            progressBar.setVisibility(View.VISIBLE);
            checkEmailVerified(firebaseUser);
            //fetch user detail from DB firebase
            showUserProfile(firebaseUser);
        }
    }

    private void checkEmailVerified(FirebaseUser firebaseUser) {
        if (!firebaseUser.isEmailVerified()){
            showAlertDialog();
        }
    }

    //after successful registration, show alert if email isn't verified
    private void showAlertDialog() {
        //setup
        AlertDialog.Builder builder = new AlertDialog.Builder(UserProfileActivity.this);
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

    private void showUserProfile(FirebaseUser firebaseUser) {
//        String userID = firebaseUser.getUid();
        String userID = firebaseUser.getUid();

        //extract each user reference from database under registered members db
        DatabaseReference referenceProfile = FirebaseDatabase.getInstance().getReference("Registered Members");
        //add Listener for every single change that occurs to this child in the database
        referenceProfile.child(userID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                MemberDAO memberDAO = snapshot.getValue(MemberDAO.class);
                //check if obj is null
                if(memberDAO != null){
                    Log.e(TAG, "the result is: "+ memberDAO.memberEmail+ " "+ memberDAO.memberAddress+ " "+ memberDAO.memberPhoneNo+ " "+  memberDAO.memberGender);

                    //save in string value
                    fullName = firebaseUser.getDisplayName();
//                    email = firebaseUser.getEmail();
                    email = memberDAO.memberEmail;
                    address = memberDAO.memberAddress;
                    phoneNo = memberDAO.memberPhoneNo;
                    gender = memberDAO.memberGender;

                    textViewWelcome.setText("Welcome "+ fullName+ "!");
                    textViewFullName.setText(fullName);
                    textViewEmail.setText(email);
                    textViewAddress.setText(address);
                    textViewGender.setText(gender);
                    textViewPhoneNo.setText(phoneNo);

                    //set profile pic
                    Uri uri = firebaseUser.getPhotoUrl();

                    //get from external source- firebase so use picasso
                    Picasso.with(UserProfileActivity.this).load(uri).into(imageView);

                }
                else{
                    Log.e(TAG, "ERROR");
                    Toast.makeText(UserProfileActivity.this, "Something is wrong. ", Toast.LENGTH_LONG).show();

                }


                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(UserProfileActivity.this, "Something is wrong. ", Toast.LENGTH_LONG).show();
                progressBar.setVisibility(View.GONE);
            }
        });


    }

    //Create option menu

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
            Intent intent = new Intent(UserProfileActivity.this, UpdateProfileActivity.class);
            startActivity(intent);
        }
        else if (id == R.id.menu_update_email){
            Intent intent = new Intent(UserProfileActivity.this, UpdateEmailActivity.class);
            startActivity(intent);
            finish();
        }
////        else if (id == R.id.menu_setting){
////            Intent intent = new Intent(UserProfileActivity.this, SettingActivity.class);
////        startActivity(intent);
        //          finish();

////        }
        else if (id == R.id.menu_change_password){
            Intent intent = new Intent(UserProfileActivity.this, ChangePasswordActivity.class);
            startActivity(intent);
            finish();
        }
//        else if (id == R.id.menu_delete_profile){
//            Intent intent = new Intent(UserProfileActivity.this, DeleteProfileActivity.class);
//            startActivity(intent);
        //          finish();

//        }
        else if (id == R.id.menu_logout){
            authProfile.signOut();
            Toast.makeText(UserProfileActivity.this, "Logged Out", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(UserProfileActivity.this, MainActivity.class);
            //clear stack to prevent signout users from accessing profile unless logged in aagian
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP| Intent.FLAG_ACTIVITY_CLEAR_TASK|
                    Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish(); //close userProfileActivity
        }
        else{
        //if no item was clicked
            Toast.makeText(UserProfileActivity.this, "Something went wrong. click menu item again", Toast.LENGTH_LONG).show();

        }
        return super.onOptionsItemSelected(item);
    }
}