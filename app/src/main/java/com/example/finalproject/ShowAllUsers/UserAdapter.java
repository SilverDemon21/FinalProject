package com.example.finalproject.ShowAllUsers;

import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.finalproject.R;
import com.example.finalproject.User;

import java.util.List;
import java.io.File;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserAdapter extends ArrayAdapter<User> {
    private Context mContext;


    // Constructor
    public UserAdapter(Context context, List<User> userList) {
        super(context, 0, userList);
        mContext = context;

    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        User user = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.user_item, parent, false);
        }




        TextView nameTextView = convertView.findViewById(R.id.nameTextView);
        TextView emailTextView = convertView.findViewById(R.id.emailTextView);
        TextView usernameTextView = convertView.findViewById(R.id.usernameTextView);
        TextView phoneTextView = convertView.findViewById(R.id.phoneTextView);
        CircleImageView imgUrl = convertView.findViewById(R.id.imgPhoto);


        if (user != null) {
            nameTextView.setText(user.getName());
            emailTextView.setText(user.getEmail().replace("_","."));
            usernameTextView.setText(user.getUsername());
            phoneTextView.setText(user.getPhoneNum());


            Glide.with(mContext)
                    .load(user.getPhotoUrl())
                    .into(imgUrl);
        }


        return convertView;
    }






}