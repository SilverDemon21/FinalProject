package com.example.finalproject.mainAplication;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import com.example.finalproject.R;

import java.util.List;

public class AdapterUserGroups extends ArrayAdapter<Object_GroupOfUsers> {
    private Context mContext;

    public AdapterUserGroups(Context context, List<Object_GroupOfUsers> UserGroups) {
        super(context, 0, UserGroups);
        mContext = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Object_GroupOfUsers UserGroup = getItem(position);

        convertView = LayoutInflater.from(mContext).inflate(R.layout.user_group_item, parent, false);


        TextView groupNameTextView = convertView.findViewById(R.id.groupNameUserGroupsTextView);
        TextView groupCreatorUsernameTextView = convertView.findViewById(R.id.groupCreatorUsernameUserGroupsTextView);
        TextView groupType = convertView.findViewById(R.id.groupType);



        if(UserGroup != null){
            groupNameTextView.setText(UserGroup.getGroupName());
            UserGroup.getGroupUsers().entrySet().stream()
                    .filter(entry -> entry.getValue().equals("Manager"))
                    .findFirst()
                    .ifPresent(entry ->groupCreatorUsernameTextView.setText(entry.getKey()));


            groupType.setText(UserGroup.getGroupType());
        }

        if (UserGroup.getGroupState().equals("Pending")){
            TextView titleForGroupName = convertView.findViewById(R.id.titleForGroupName);
            TextView titleForGroupManager = convertView.findViewById(R.id.titleForGroupManager);
            TextView pendingIdentifier = convertView.findViewById(R.id.pendingIdentifier);
            TextView titleForGroupType = convertView.findViewById(R.id.titleForGroupType);
            ConstraintLayout usersGroupsCardView = convertView.findViewById(R.id.usersGroupsLayout);


            pendingIdentifier.setText(UserGroup.getGroupName());
            usersGroupsCardView.setBackgroundResource(R.drawable.background_user_groups_pending_item);
            groupNameTextView.setTextColor(ContextCompat.getColor(mContext, R.color.silver));
            groupCreatorUsernameTextView.setTextColor(ContextCompat.getColor(mContext, R.color.silver));
            groupType.setTextColor(ContextCompat.getColor(mContext,R.color.silver));
            titleForGroupManager.setTextColor(ContextCompat.getColor(mContext, R.color.silver));
            titleForGroupName.setTextColor(ContextCompat.getColor(mContext,R.color.silver));
            titleForGroupType.setTextColor(ContextCompat.getColor(mContext,R.color.silver));
        }

        return convertView;
    }
}
