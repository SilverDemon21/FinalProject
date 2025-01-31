package com.example.finalproject.mainAplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.finalproject.MainActivity;
import com.example.finalproject.R;
import com.example.finalproject.User_Profile;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class groups extends AppCompatActivity {

    private Button btnCreateGroup;
    private boolean itemIsSelected = false;
    private String selectedOption;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_groups);

        btnCreateGroup = findViewById(R.id.btnCreateGroup);


        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationBarGroups);

        bottomNavigationView.setSelectedItemId(R.id.menu_groups);


        bottomNavigationView.setOnItemSelectedListener(item ->{
            if(item.getItemId() == R.id.menu_home){
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                overridePendingTransition(0, 0);
                return true;
            }
            else if(item.getItemId() == R.id.menu_map){
                startActivity(new Intent(getApplicationContext(), mapAndLogic.class));
                overridePendingTransition(0, 0);
                return true;
            }
            else if(item.getItemId() == R.id.menu_profile){
                startActivity(new Intent(getApplicationContext(), User_Profile.class));
                overridePendingTransition(0, 0);
                return true;
            }
            else if(item.getItemId() == R.id.menu_groups){
                return true;
            }
            return true;


        });


        btnCreateGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                LayoutInflater inflater = LayoutInflater.from(groups.this);
                View dialogView = inflater.inflate(R.layout.dialog_create_new_group, null);

                AlertDialog.Builder builder = new AlertDialog.Builder(groups.this);
                builder.setView(dialogView);
                AlertDialog dialog = builder.create();

                EditText etGroupName = dialogView.findViewById(R.id.etGroupName);
                Button btnCreateGroup = dialogView.findViewById(R.id.btnCreateGroup);
                Spinner spinner = dialogView.findViewById(R.id.spinnerGroupType);

                String[] options = {"Parents Mode", "Friends Mode"};
                ArrayAdapter<String> adapter = new ArrayAdapter<>(groups.this, android.R.layout.simple_spinner_dropdown_item, options);
                spinner.setAdapter(adapter);

                spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                        selectedOption = options[i];
                        itemIsSelected = true;
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> adapterView) {

                    }
                });

                btnCreateGroup.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        sendRequestToAdmin(etGroupName.getText().toString().trim(), selectedOption);
                        Toast.makeText(groups.this, "Your request to open new group was redirected to the admin")
                        dialog.dismiss();
                    }
                });

                dialog.show();
            }
        });



    }
}