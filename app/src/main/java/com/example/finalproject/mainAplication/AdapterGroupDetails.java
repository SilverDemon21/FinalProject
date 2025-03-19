package com.example.finalproject.mainAplication;

import android.app.Activity;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.Context;
import android.content.OperationApplicationException;
import android.content.pm.PackageManager;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;

import com.example.finalproject.R;
import com.example.finalproject.ShowAllUsers.Object_User;
import com.example.finalproject.sharedPref_manager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class AdapterGroupDetails extends ArrayAdapter<Object_User> {

    private Context mContext;
    private List<String> membersStatus;
    private HashMap<String, String> memberWithStatus;

    public AdapterGroupDetails(Context context, List<Object_User> objectUsers, List<String> membersStatus) {
        super(context, 0, objectUsers);
        mContext = context;
        this.membersStatus = membersStatus;


        memberWithStatus = new HashMap<>();
        for(int i = 0; i < membersStatus.size(); i++){
            memberWithStatus.put(membersStatus.get(i), objectUsers.get(i).getUsername());
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Object_User clickedObjectUser = getItem(position);

        convertView = LayoutInflater.from(mContext).inflate(R.layout.member_of_group_item, parent, false);


        TextView memberUserName = convertView.findViewById(R.id.memberUserName);
        TextView memberName = convertView.findViewById(R.id.memberName);
        ConstraintLayout membersInGroupLayout = convertView.findViewById(R.id.membersInGroupLayout);
        CircleImageView MemberPhoto = convertView.findViewById(R.id.MemberPhoto);
        ImageView managerCrownInGroup = convertView.findViewById(R.id.managerCrownInGroup);
        Button addContactButton = convertView.findViewById(R.id.addContactButton);


        if(clickedObjectUser != null){
            memberUserName.setText(clickedObjectUser.getUsername());
            memberName.setText(clickedObjectUser.getName());

            Glide.with(mContext)
                    .load(clickedObjectUser.getPhotoUrl())
                    .into(MemberPhoto);
        }

        sharedPref_manager manager = new sharedPref_manager(mContext, "LoginUpdate");

        if(!memberWithStatus.get("Manager").equals(clickedObjectUser.getUsername())){
            managerCrownInGroup.setVisibility(View.GONE);
        }
        else{
            membersInGroupLayout.setBackgroundResource(R.drawable.background_manager_in_group);
        }


        if(manager.getUsername().equals(clickedObjectUser.getUsername())){
            addContactButton.setVisibility(View.GONE);
        }

        addContactButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Check if the WRITE_CONTACTS permission is granted
                if (ContextCompat.checkSelfPermission(mContext, android.Manifest.permission.WRITE_CONTACTS)
                        != PackageManager.PERMISSION_GRANTED) {

                    // Permission is not granted, request permission
                    ActivityCompat.requestPermissions((Activity) mContext,
                            new String[]{android.Manifest.permission.WRITE_CONTACTS}, 1);
                } else {
                    // Permission is granted, proceed with adding the user to contacts
                    String userPhone = clickedObjectUser.getPhoneNum();
                    String usersName = clickedObjectUser.getName();
                    addUserToContact(userPhone, usersName);
                }
            }
        });
        return convertView;
    }
    

    private void addUserToContact(String userPhone, String usersName){
        ArrayList<ContentProviderOperation> ops = new ArrayList<>();

        // Insert Raw Contact
        ops.add(ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
                .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
                .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null)
                .build());

        // Insert Name
        ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, usersName)
                .build());

        // Insert Phone Number
        ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, userPhone)
                .withValue(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE)
                .build());



        // Apply batch operation
        try {
            ContentResolver contentResolver = mContext.getContentResolver();
            contentResolver.applyBatch(ContactsContract.AUTHORITY, ops);
            Toast.makeText(mContext, "The contact was added successfully", Toast.LENGTH_SHORT).show();
        } catch (RemoteException | OperationApplicationException e) {
            e.printStackTrace();
        }
    }
}
