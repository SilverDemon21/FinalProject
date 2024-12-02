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
import com.example.finalproject.sharedPref_manager;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.DatabaseError;

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
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class signUpActivity extends AppCompatActivity {
    EditText signUp_name, signUp_email, signUp_username, signUp_password, signUp_phoneNum;
    private ImageView imgGallery, imgCamera, imgProfile;
    TextView loginRedirectText, title;
    Button signUp_button;
    ImageButton RbackToMainActivity;
    private Uri uriPhoto;
    private Bitmap photoBitmap;


    private final DatabaseReference database = FirebaseDatabase.getInstance().getReference();

    private boolean findCamera, findGallery;

    private String photoName, type;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        getSupportActionBar().hide();

        // implements all the variables
        sharedPref_manager manger = new sharedPref_manager(signUpActivity.this, "LoginUpdate");

        title = findViewById(R.id.title);
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

        Intent intent = getIntent();

        type = intent.getStringExtra("activity");


        if (type.equals("update")){
            loginRedirectText.setVisibility(View.GONE);
            loginRedirectText.setEnabled(false);

            title.setText("Update Profile");
            signUp_button.setText("Update");
            title.setTextSize(24);

            signUp_password.setVisibility(View.GONE);
            signUp_password.setEnabled(false);

            signUp_username.setVisibility(View.GONE);
            signUp_username.setEnabled(false);

            signUp_name.setText(manger.getName());
            signUp_email.setText(manger.getEmail().replace("_", "."));
            signUp_phoneNum.setText(manger.getPhoneNum());

            photoName = manger.getPhotoName();

            File file = new File(Environment.getExternalStorageDirectory() + "/" + "Pictures" + "/" + photoName);
            if (file.exists()){
                Uri uri = Uri.parse(file.getAbsolutePath());
                imgProfile.setImageURI(uri);
            }
        }


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


                String name = signUp_name.getText().toString();
                String email = signUp_email.getText().toString();
                String formattedEmail = email.replace(".", "_");
                String username = signUp_username.getText().toString();
                String password = signUp_password.getText().toString();
                String phone = signUp_phoneNum.getText().toString();


                // the signup process
               checkUniqueness(username, formattedEmail, phone, new UniquenessCallback() {
                   @Override
                   public void onResult(String isUnique) {
                       if(isUnique.equals("good")){
                           if(profileIsGood()){

                               if(type.equals("create")){
                                   saveUserDetails(name, formattedEmail, username, password, phone, photoName);
                                   Intent intent = new Intent(signUpActivity.this, loginActivity.class);
                                   startActivity(intent);

                               } else if (type.equals("update")) {

                                    String cUsername = manger.getUsername();
                                    updateUser(cUsername, formattedEmail, phone, name, photoName, new UpdateCallback() {
                                        @Override
                                        public void onResult(boolean updated) {
                                            Intent intent = new Intent(signUpActivity.this, MainActivity.class);
                                            startActivity(intent);
                                        }
                                    });
                               }
                           }
                       }
                       else if(isUnique.equals("username")){
                           signUp_username.setError("Username is already exists");
                       }
                       else if(isUnique.equals("email")){
                           signUp_email.setError("Email is already exists");
                       }
                       else if(isUnique.equals("phone")){
                           signUp_phoneNum.setError("Phone number is already exists");
                       }
                   }
               });
            }
        });
    }

    // check the uniqueness of an email, username and phone number
    private void checkUniqueness(String username, String email, String phoneNumber, UniquenessCallback callback) {
        // Check if username already exists
        sharedPref_manager manager = new sharedPref_manager(signUpActivity.this,"LoginUpdate");
        database.child("users").child(username).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists() && !username.isEmpty()) {
                    if(username.equals(manager.getUsername()) && type.equals("update")){}
                    else{
                        callback.onResult("username"); // Username already taken
                        return;
                    }

                }
                // Check if email is already taken
                database.child("emails").child(email).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        if (snapshot.exists() && !email.isEmpty()) {
                            if(email.equals(manager.getEmail()) && type.equals("update")){}
                            else{
                                callback.onResult("email"); // Email already in use
                                return;
                            }
                        }

                        // Check if phone number is already taken
                        database.child("phoneNumbers").child(phoneNumber).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot snapshot) {
                                if (snapshot.exists() && !phoneNumber.isEmpty()) {
                                    if(phoneNumber.equals(manager.getPhoneNum()) && type.equals("update"))
                                    {
                                        callback.onResult("good");
                                    }
                                    else{
                                        callback.onResult("phone"); // Email already in use
                                    }
                                }
                                else{
                                    callback.onResult("good");
                                }

                            }

                            @Override
                            public void onCancelled(DatabaseError error) {
                                callback.onResult("cna");
                            }
                        });
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        callback.onResult("cna");
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError error) {
                callback.onResult("cna");
            }
        });
    }

    // callback interface for the function above
    interface UniquenessCallback {
        void onResult(String isUnique);
    }

    // update the user profile
    private void updateUser(String username, String newEmail, String newPhone, String newName, String newPhotoName, UpdateCallback callback){
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

        databaseReference.child("users").child(username).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(!snapshot.exists()){
                    return;
                }

                sharedPref_manager manager = new sharedPref_manager(signUpActivity.this, "LoginUpdate");
                Map<String, Object> updates = new HashMap<>();

                String currentEmail = manager.getEmail();
                String currentPhone = manager.getPhoneNum();
                String currentName = manager.getName();
                String currentPhotoName = manager.getPhotoName();

                manager.setEmail(newEmail);
                manager.setName(newName);
                manager.setPhoneNum(newPhone);
                manager.setPhotoName(newPhotoName);

                if(newEmail != null && !newEmail.equals(currentEmail)){
                    updates.put("email", newEmail);

                    databaseReference.child("emails").child(currentEmail).setValue(null);
                    databaseReference.child("emails").child(newEmail).setValue(username);
                }

                if(newPhone != null && !newPhone.equals(currentPhone)){
                    updates.put("phoneNum", newPhone);

                    databaseReference.child("phoneNumbers").child(currentPhone).setValue(null);
                    databaseReference.child("phoneNumbers").child(newPhone).setValue(username);
                }

                if(newName != null && !newName.equals(currentName)){
                    updates.put("name", newName);
                }

                if(newPhotoName!= null && !newPhotoName.equals(currentPhotoName)){
                    updates.put("photoName", photoName);
                }

                if(!updates.isEmpty()){
                    databaseReference.child("users").child(username).updateChildren(updates).addOnCompleteListener(task -> {
                        if(task.isSuccessful()){
                            Toast.makeText(signUpActivity.this, "The profile was Updated", Toast.LENGTH_SHORT).show();
                            callback.onResult(true);
                        }
                        else{
                            Toast.makeText(signUpActivity.this, "The profile wasn't Updated", Toast.LENGTH_SHORT).show();
                            callback.onResult(false);
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    // callback interface for the function above
    interface UpdateCallback{
        void onResult(boolean updated);
    }


    // save the user details when signing up
    private void saveUserDetails(String name, String email, String username, String password, String phoneNum, String photoName) {
        // Create a new User object with the provided data
        HellperSignUpClass user = new HellperSignUpClass(name, email, username, password, phoneNum, photoName);

        database.child("users").child(username).setValue(user);
        database.child("emails").child(email).setValue(username);
        database.child("phoneNumbers").child(phoneNum).setValue(username);

    }




    // validation for all the fields
    public Boolean profileIsGood(){
        boolean profileGood = true;
        String phone = signUp_phoneNum.getText().toString().trim();
        String email = signUp_email.getText().toString().trim();
        String name = signUp_name.getText().toString().trim();
        String username = signUp_username.getText().toString().trim();
        String password = signUp_password.getText().toString().trim();
        if(photoBitmap == null && type.equals("create")){
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
            signUp_name.setError("The name should be between 2 and 10 characters and only contain letters");
            profileGood = false;
        }
        if (!info_validation.password_validation(password) && !type.equals("update")){
            signUp_password.setError("The password should be between 6 and 18 characters");
            profileGood = false;
        }
        if (!info_validation.username_validation(username) && !type.equals("update")){
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


    // open the photo at the top of the activity
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


    // save the image at a folder to be able to use it again directly
    public boolean saveImageInFolder(Bitmap bitmap){
        if (photoBitmap == null){
            return true;
        }
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