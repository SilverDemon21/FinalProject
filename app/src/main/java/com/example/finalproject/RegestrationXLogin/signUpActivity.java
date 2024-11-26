package com.example.finalproject.RegestrationXLogin;

import static com.example.finalproject.Permission.DoesHavePrem;
import static com.example.finalproject.Permission.GrantPermission;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.finalproject.MainActivity;
import com.example.finalproject.R;
import com.example.finalproject.info_validation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class signUpActivity extends AppCompatActivity {
    EditText signUp_name, signUp_email, signUp_username, signUp_password, signUp_phoneNum;
    private ImageView imgGallery, imgCamera, imgProfile;
    TextView loginRedirectText;
    Button signUp_button;
    FirebaseDatabase database;
    DatabaseReference reference;
    DatabaseReference mDatabase;
    ImageButton RbackToMainActivity;
    private Uri uriPhoto;
    private Bitmap photoBitmap;

    private boolean findCamera, findGallery;

    private String photoName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        getSupportActionBar().hide();

        // implements all the variables



        imgCamera = findViewById(R.id.imgCamera);
        imgGallery = findViewById(R.id.imgGallery);
        signUp_name = findViewById(R.id.signUp_name);
        signUp_email = findViewById(R.id.signUp_email);
        signUp_username = findViewById(R.id.signUp_username);
        signUp_password = findViewById(R.id.signUp_password);
        signUp_button = findViewById(R.id.signUp_button);
        loginRedirectText = findViewById(R.id.loginRedirectText);
        RbackToMainActivity = findViewById(R.id.RbackMainActivity);
        signUp_phoneNum = findViewById(R.id.signUp_phoneNum);

        imgProfile = findViewById(R.id.imgProfile);

        findCamera = false;
        findGallery = false;


        // button to pick up image from gallery
        imgGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!DoesHavePrem(signUpActivity.this)){
                    GrantPermission(signUpActivity.this);
                }
                else{
                    findCamera = false;
                    findGallery = true;
                    Intent intent = new Intent();
                    intent.setType("image/*");
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    openPhoto.launch(intent);

                }
            }
        });

        // button to pick up image from camera (get photo)
        imgCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!DoesHavePrem(signUpActivity.this)){
                    GrantPermission(signUpActivity.this);
                }
                else{
                    findGallery = false;
                    findCamera = true;

                    ContentValues values = new ContentValues();
                    values.put(MediaStore.Images.Media.TITLE, "New Picture");
                    values.put(MediaStore.Images.Media.TITLE, "From Camera");

                    uriPhoto = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, uriPhoto);
                    openPhoto.launch(intent);
                }
            }
        });

        // go back to main activity button
        RbackToMainActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(signUpActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

        // button for crossing from signUp to login
        loginRedirectText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(signUpActivity.this, loginActivity.class);
                startActivity(intent);
            }
        });

        // button for signingUp
        signUp_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                database = FirebaseDatabase.getInstance();
                reference = database.getReference("users");


                String name = signUp_name.getText().toString();
                String email = signUp_email.getText().toString();
                String username = signUp_username.getText().toString();
                String password = signUp_password.getText().toString();
                String phone = signUp_phoneNum.getText().toString();



                // the signup process
                reference.child(username).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DataSnapshot> task) {
                        if(task.getResult().getValue() != null && !username.isEmpty()){
                            signUp_username.setError("The username is already in use");
                            profileIsGood();
                        }
                        else{
                            if(!profileIsGood()){

                            }
                            else {
                                HellperSignUpClass hellperSignUpClass = new HellperSignUpClass(name, email, username, password, phone, photoName);
                                reference.child(username).setValue(hellperSignUpClass);

                                Intent intent = new Intent(signUpActivity.this, loginActivity.class);
                                startActivity(intent);
                                Toast.makeText(signUpActivity.this, "You sign up successfully", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
            }
        });
    }

    // validation for all the fields
    public Boolean profileIsGood(){
        boolean profileGood = true;
        String phone = signUp_phoneNum.getText().toString().trim();
        String email = signUp_email.getText().toString().trim();
        String name = signUp_name.getText().toString().trim();
        String username = signUp_username.getText().toString().trim();
        String password = signUp_password.getText().toString().trim();
        if(photoBitmap == null){
            Toast.makeText(signUpActivity.this, "Pls save the image", Toast.LENGTH_SHORT).show();
            profileGood = false;
        }
        if (!info_validation.email_validation(email)){
            signUp_email.setError("Pls enter a valid email / the email is already exist");
            profileGood = false;
        }
        if (!info_validation.phoneNumber_validation(phone)){
            signUp_phoneNum.setError("Pls enter a valid phone / the phone is already exist");
            profileGood = false;
        }
        if (!info_validation.name_validation(name)){
            signUp_name.setError("The name should be between 2 and 10 characters");
            profileGood = false;
        }
        if (!info_validation.password_validation(password)){
            signUp_password.setError("The password should be between 6 and 18 characters");
            profileGood = false;
        }
        if (!info_validation.username_validation(username)){
            signUp_username.setError("The username should be between 5 and 15 characters");
            profileGood = false;
        }
        if(profileGood){
            if(!saveImageInFolder(photoBitmap)){
                profileGood = false;
            }
        }
        return profileGood;
    }


    ActivityResultLauncher<Intent> openPhoto = registerForActivityResult
            (new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            if (result.getResultCode() == Activity.RESULT_OK){
                if(findCamera) {
                    try {
                        photoBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uriPhoto);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    imgProfile.setImageURI(uriPhoto);
                }


                else if (findGallery)
                {
                    Intent filePath = result.getData();
                    if(filePath !=  null && filePath.getData() != null){
                        try {
                            photoBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath.getData());
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                        uriPhoto = filePath.getData();
                        imgProfile.setImageURI(uriPhoto);
                    }

                }
            }
        }
    });

    public boolean saveImageInFolder(Bitmap bitmap){
        photoName = new SimpleDateFormat("ddMMyy-HHmmss").format(new Date()) + ".jpg";
        File myDir = new File(Environment.getExternalStorageDirectory(), "/" + "Pictures");
        if(!myDir.exists()){
            myDir.mkdirs();
        }

        File dest = new File(myDir, photoName);

        FileOutputStream out = null;
        try {
            out = new FileOutputStream(dest);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.flush();
            out.close();
            Toast.makeText(signUpActivity.this, "The image have been saved", Toast.LENGTH_SHORT).show();
            return true;
        }
        catch (Exception e)
        {
            Toast.makeText(signUpActivity.this, "The image have not been saved", Toast.LENGTH_SHORT).show();
            return false;
        }
    }
}