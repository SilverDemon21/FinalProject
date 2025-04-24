package com.example.finalproject.AllAdapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.finalproject.AllObjects.Object_User;
import com.example.finalproject.R;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserAdapter extends ArrayAdapter<Object_User> {
    private Context mContext;


    // Constructor
    public UserAdapter(Context context, List<Object_User> objectUserList) {
        super(context, 0, objectUserList);
        mContext = context;

    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Object_User objectUser = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.user_item, parent, false);
        }


        TextView nameTextView = convertView.findViewById(R.id.nameTextView);
        TextView emailTextView = convertView.findViewById(R.id.emailTextView);
        TextView usernameTextView = convertView.findViewById(R.id.usernameTextView);
        TextView phoneTextView = convertView.findViewById(R.id.phoneTextView);
        TextView dateOfBirth = convertView.findViewById(R.id.dateTextView);
        CircleImageView imgUrl = convertView.findViewById(R.id.imgPhoto);


        if (objectUser != null) {
            nameTextView.setText(objectUser.getName());
            emailTextView.setText(objectUser.getEmail().replace("_","."));
            usernameTextView.setText(objectUser.getUsername());
            phoneTextView.setText(objectUser.getPhoneNum());
            double age = calculateAge(objectUser.getDateOfBirth());
            dateOfBirth.setText(Double.toString(age));


            Glide.with(mContext)
                    .load(objectUser.getPhotoUrl())
                    .into(imgUrl);
        }


        return convertView;
    }

    private double calculateAge(String dateOfBirth) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        try {
            Date dob = sdf.parse(dateOfBirth);
            Calendar dobCalendar = Calendar.getInstance();
            dobCalendar.setTime(dob);

            Calendar today = Calendar.getInstance();

            int years = today.get(Calendar.YEAR) - dobCalendar.get(Calendar.YEAR);
            int months = today.get(Calendar.MONTH) - dobCalendar.get(Calendar.MONTH);


            if (months < 0) {
                years--;
                months += 12;
            }

            double decimalAge = years + (months / 12.0);

            DecimalFormat df = new DecimalFormat("#.##");
            return Double.parseDouble(df.format(decimalAge));

        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }








}