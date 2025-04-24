package com.example.finalproject.RegestrationXLogin;

import static com.example.finalproject.Permission.GrantAllPermissions;
import static com.example.finalproject.Permission.DoesUserHasAllOfThePermissions;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.finalproject.sharedPref_manager;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.DatabaseError;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.finalproject.MainActivity;
import com.example.finalproject.R;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class signUpActivity extends AppCompatActivity {
    EditText signUp_name, signUp_email, signUp_username, signUp_password, signUp_phoneNum,
            signUp_comfirm_password,signUp_date_of_birth;
    private ImageView imgGallery, imgCamera, imgProfile;
    TextView loginRedirectText, title;
    Button signUp_button;
    ImageButton RbackToMainActivity;
    private Uri uriPhoto;
    private Bitmap photoBitmap;
    Calendar calendar;


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
        signUp_comfirm_password = findViewById(R.id.signUp_comfirm_password);
        signUp_date_of_birth = findViewById(R.id.signUp_date_of_birth);

        imgProfile = findViewById(R.id.imgProfile);

        findCamera = false;
        findGallery = false;

        Intent intent = getIntent();

        type = intent.getStringExtra("activity");

        if (type.equals("update")) {
            loginRedirectText.setVisibility(View.GONE);
            loginRedirectText.setEnabled(false);

            Glide.with(this)
                    .load(manger.getPhotoUrl())
                    .into(imgProfile);


            title.setText("Update Profile");
            signUp_button.setText("Update");
            title.setTextSize(24);

            signUp_password.setVisibility(View.GONE);

            signUp_username.setVisibility(View.GONE);

            signUp_comfirm_password.setVisibility(View.GONE);

            signUp_date_of_birth.setVisibility(View.GONE);

            signUp_name.setText(manger.getName());
            signUp_email.setText(manger.getEmail().replace("_", "."));
            signUp_phoneNum.setText(manger.getPhoneNum());
        }

        calendar = Calendar.getInstance();
        signUp_date_of_birth.setOnClickListener(v -> showDatePickerDialog());


        // button to pick up image from gallery
        imgGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!DoesUserHasAllOfThePermissions(signUpActivity.this)) {
                    GrantAllPermissions(signUpActivity.this);
                } else {
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
                if (!DoesUserHasAllOfThePermissions(signUpActivity.this)) {
                    GrantAllPermissions(signUpActivity.this);
                } else {
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
                finish();
            }
        });

        // button for crossing from signUp to login
        loginRedirectText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(signUpActivity.this, loginActivity.class);
                startActivity(intent);
                finish();
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
                String dateOfBirth = signUp_date_of_birth.getText().toString();


                // the signup process
                checkUniqueness(username, formattedEmail, phone, new UniquenessCallback() {
                    @Override
                    public void onResult(String isUnique) {
                        if (isUnique.equals("good")) {
                            if (profileIsGood()) {

                                if (type.equals("create")) {
                                    saveUserDetails(name, formattedEmail, username, password, phone, uriPhoto, dateOfBirth, new ImageUploadCallback() {
                                        @Override
                                        public void onResult(boolean created) {
                                            Intent intent = new Intent(signUpActivity.this, loginActivity.class);
                                            startActivity(intent);
                                            finish();
                                        }
                                    });
                                }
                                else if (type.equals("update")) {
                                    String cUsername = manger.getUsername();
                                    String Url;
                                    if(uriPhoto == null){
                                        Url = manger.getPhotoUrl();
                                    }
                                    else{
                                        Url = uriPhoto.toString();
                                    }
                                    updateUser(cUsername, formattedEmail, phone, name, Url, new UpdateCallback() {
                                        @Override
                                        public void onResult(boolean updated) {
                                            Intent intent = new Intent(signUpActivity.this, MainActivity.class);
                                            startActivity(intent);
                                            finish();
                                        }
                                    });
                                }
                            }
                        } else if (isUnique.equals("username")) {
                            signUp_username.setError("Username is already exists");
                        } else if (isUnique.equals("email")) {
                            signUp_email.setError("Email is already exists");
                        } else if (isUnique.equals("phone")) {
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
        sharedPref_manager manager = new sharedPref_manager(signUpActivity.this, "LoginUpdate");
        database.child("users").child(username).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists() && !username.isEmpty()) {
                    if (username.equals(manager.getUsername()) && type.equals("update")) {
                    } else {
                        callback.onResult("username"); // Username already taken
                        return;
                    }

                }
                // Check if email is already taken
                database.child("emails").child(email).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        if (snapshot.exists() && !email.isEmpty()) {
                            if (email.equals(manager.getEmail()) && type.equals("update")) {
                            } else {
                                callback.onResult("email"); // Email already in use
                                return;
                            }
                        }

                        // Check if phone number is already taken
                        database.child("phoneNumbers").child(phoneNumber).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot snapshot) {
                                if (snapshot.exists() && !phoneNumber.isEmpty()) {
                                    if (phoneNumber.equals(manager.getPhoneNum()) && type.equals("update")) {
                                        callback.onResult("good");
                                    } else {
                                        callback.onResult("phone"); // Email already in use
                                    }
                                } else {
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

    // update the user profile
    private void updateUser(String username, String newEmail, String newPhone, String newName, String newPhotoUrl, UpdateCallback callback) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

        databaseReference.child("users").child(username).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    return;
                }


                sharedPref_manager manager = new sharedPref_manager(signUpActivity.this, "LoginUpdate");
                Map<String, Object> updates = new HashMap<>();

                String currentEmail = manager.getEmail();
                String currentPhone = manager.getPhoneNum();
                String currentName = manager.getName();
                String currentPhotoUrl = manager.getPhotoUrl();

                
                if (newEmail != null && !newEmail.equals(currentEmail)) {
                    updates.put("email", newEmail);

                    databaseReference.child("emails").child(currentEmail).setValue(null);
                    databaseReference.child("emails").child(newEmail).setValue(username);
                    manager.setEmail(newEmail);
                }

                if (newPhone != null && !newPhone.equals(currentPhone)) {
                    updates.put("phoneNum", newPhone);

                    databaseReference.child("phoneNumbers").child(currentPhone).setValue(null);
                    databaseReference.child("phoneNumbers").child(newPhone).setValue(username);
                    manager.setPhoneNum(newPhone);
                }

                if (newName != null && !newName.equals(currentName)) {
                    updates.put("name", newName);
                    manager.setName(newName);
                }

                if (newPhotoUrl != null && !newPhotoUrl.equals(currentPhotoUrl)) {
                    updatePhoto(username, newPhotoUrl, new UpdatePhotoCallback() {
                        @Override
                        public void onResult(boolean updated) {

                            String Url = uriPhoto.toString();
                            updates.put("photoUrl", Url);
                            manager.setPhotoUrl(Url);
                            databaseReference.child("users").child(username).updateChildren(updates).addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    Toast.makeText(signUpActivity.this, "The profile was Updated", Toast.LENGTH_SHORT).show();
                                    callback.onResult(true);
                                } else {
                                    Toast.makeText(signUpActivity.this, "The profile wasn't Updated", Toast.LENGTH_SHORT).show();
                                    callback.onResult(false);
                                }
                            });
                        }
                    });
                }

                else if (!updates.isEmpty()) {
                    databaseReference.child("users").child(username).updateChildren(updates).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(signUpActivity.this, "The profile was Updated", Toast.LENGTH_SHORT).show();
                            callback.onResult(true);
                        } else {
                            Toast.makeText(signUpActivity.this, "The profile wasn't Updated", Toast.LENGTH_SHORT).show();
                            callback.onResult(false);
                        }
                    });
                }
                else{
                    callback.onResult(true);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    // save the user details when signing up
    private void saveUserDetails(String name, String email, String username, String password, String phoneNum, Uri photoUri,String dateOfBirth, ImageUploadCallback callback) {


        StorageReference storageRef = FirebaseStorage.getInstance("gs://final-project-be550.firebasestorage.app").getReference();
        StorageReference imageRef = storageRef.child(username);


        UploadTask uploadTask = imageRef.putFile(photoUri);
        uploadTask.addOnSuccessListener(taskSnapshot -> {
            imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                String photoUrl = uri.toString();

                HellperSignUpClass user = new HellperSignUpClass(name, email, username, password, phoneNum, photoUrl, dateOfBirth);

                database.child("users").child(username).setValue(user);
                database.child("emails").child(email).setValue(username);
                database.child("phoneNumbers").child(phoneNum).setValue(username);

                HashMap<String, Object> profileSettings= new HashMap<>();
                profileSettings.put("concentrateOnUserMap",true);
                profileSettings.put("showToastHelperMap", true);
                database.child("users").child(username).child("profileSettings").setValue(profileSettings);

                callback.onResult(true);

            });
        });
        uploadTask.addOnFailureListener(e ->{
            Log.e("fail", e.getMessage());
        });
    }

    // save user photo at database when updating the profile image
    private void updatePhoto(String username, String photoUrl, UpdatePhotoCallback callback){
        Uri photoUri = Uri.parse(photoUrl);
        StorageReference storageRef = FirebaseStorage.getInstance("gs://final-project-be550.firebasestorage.app").getReference();
        StorageReference imageRef = storageRef.child(username);

        UploadTask uploadTask = imageRef.putFile(photoUri);
        uploadTask.addOnSuccessListener(taskSnapshot -> {
            imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                uriPhoto = uri;
                callback.onResult(true);
            });
        });
    }


    public interface UpdateCallback {
        void onResult(boolean updated);
    }
    public interface ImageUploadCallback {
        void onResult(boolean isUnique);
    }
    public interface UniquenessCallback {
        void onResult(String isUnique);
    }
    public interface UpdatePhotoCallback {
        void onResult(boolean updated);
    }



    // validation for all the fields
    public Boolean profileIsGood() {
        boolean profileGood = true;
        String phone = signUp_phoneNum.getText().toString().trim();
        String email = signUp_email.getText().toString().trim();
        String name = signUp_name.getText().toString().trim();
        String username = signUp_username.getText().toString().trim();
        String password = signUp_password.getText().toString().trim();
        String comfirmPass = signUp_comfirm_password.getText().toString().trim();
        String dateOfBirth = signUp_date_of_birth.getText().toString();

        signUp_date_of_birth.setError(null);

        if (photoBitmap == null && type.equals("create")) {
            Toast.makeText(signUpActivity.this, "Pls save the image", Toast.LENGTH_SHORT).show();
            profileGood = false;
        }
        if (!InfoValidation.email_validation(email)) {
            signUp_email.setError("Pls enter a valid email");
            profileGood = false;
        }
        if (!InfoValidation.phoneNumber_validation(phone)) {
            signUp_phoneNum.setError("Pls enter a valid phone");
            profileGood = false;
        }
        if (!InfoValidation.name_validation(name)) {
            signUp_name.setError("The name should be between 2 and 10 characters and only contain letters");
            profileGood = false;
        }
        if (dateOfBirth.isEmpty() && !type.equals("update")){
            signUp_date_of_birth.setError("pls put your date of birth");
            profileGood = false;
        }
        if (!InfoValidation.password_validation(password) && !type.equals("update")) {
            signUp_password.setError("The password should be between 6 and 18 characters");
            profileGood = false;
        }
        if ((!comfirmPass.equals(password) | comfirmPass.isEmpty()) && !type.equals("update")){
            signUp_comfirm_password.setError("The password does not match the password above");
            profileGood = false;
        }
        if (!InfoValidation.username_validation(username) && !type.equals("update")) {
            signUp_username.setError("The username should be between 5 and 15 characters and contain only letters and numbers");
            profileGood = false;
        }
        return profileGood;
    }

    // date dialog for date of birth
    private void showDatePickerDialog(){
        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    // Set selected date in Calendar
                    calendar.set(Calendar.YEAR, year);
                    calendar.set(Calendar.MONTH, month);
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                    // Format and display date
                    SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
                    signUp_date_of_birth.setText(sdf.format(calendar.getTime()));
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));

        Calendar maxDate = Calendar.getInstance();
        maxDate.add(Calendar.YEAR, -7);
        datePickerDialog.getDatePicker().setMaxDate(maxDate.getTimeInMillis());

        datePickerDialog.show();
    }



    // open the photo at the top of the activity
    ActivityResultLauncher<Intent> openPhoto = registerForActivityResult
            (new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        if (findCamera) {
                            try {
                                photoBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uriPhoto);
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                            imgProfile.setImageURI(uriPhoto);
                        } else if (findGallery) {
                            Intent filePath = result.getData();
                            if (filePath != null && filePath.getData() != null) {
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


}

