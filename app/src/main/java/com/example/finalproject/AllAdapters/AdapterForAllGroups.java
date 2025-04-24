package com.example.finalproject.AllAdapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import com.example.finalproject.R;
import com.example.finalproject.AllObjects.Object_GroupOfUsers;

import java.util.List;

public class AdapterForAllGroups extends ArrayAdapter<Object_GroupOfUsers> {
    private Context mContext;
    private List<Object_GroupOfUsers> groups;

    public AdapterForAllGroups(Context context, List<Object_GroupOfUsers> allGroups) {
        super(context, 0, allGroups);
        mContext = context;
        groups = allGroups;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Object_GroupOfUsers UserGroup = getItem(position);

        convertView = LayoutInflater.from(mContext).inflate(R.layout.user_group_item, parent, false);

        int amountOfUsers = UserGroup.getGroupUsers().size();

        TextView groupNameTextView = convertView.findViewById(R.id.groupNameUserGroupsTextView);
        TextView groupCreatorUsernameTextView = convertView.findViewById(R.id.groupCreatorUsernameUserGroupsTextView);
        TextView groupType = convertView.findViewById(R.id.groupType);
        TextView amountOfMembersInGroupChange = convertView.findViewById(R.id.amountOfMembersInGroupChange);



        if(UserGroup != null){
            groupNameTextView.setText(UserGroup.getGroupName());
            UserGroup.getGroupUsers().entrySet().stream()
                    .filter(entry -> entry.getValue().equals("Manager"))
                    .findFirst()
                    .ifPresent(entry ->groupCreatorUsernameTextView.setText(entry.getKey()));


            groupType.setText(UserGroup.getGroupType());
            amountOfMembersInGroupChange.setText(String.valueOf(amountOfUsers));

        }

        if (UserGroup.getGroupState().equals("Pending")){
            TextView titleForGroupName = convertView.findViewById(R.id.titleForGroupName);
            TextView titleForGroupManager = convertView.findViewById(R.id.titleForGroupManager);
            TextView pendingIdentifier = convertView.findViewById(R.id.pendingIdentifier);
            TextView titleForGroupType = convertView.findViewById(R.id.titleForGroupType);
            TextView amountOfMembersInGroup = convertView.findViewById(R.id.amountOfMembersInGroup);
            ConstraintLayout usersGroupsCardView = convertView.findViewById(R.id.usersGroupsLayout);


            pendingIdentifier.setText("Pending");
            usersGroupsCardView.setBackgroundResource(R.drawable.background_item_user_groups_pending);
            groupNameTextView.setTextColor(ContextCompat.getColor(mContext, R.color.silver));
            groupCreatorUsernameTextView.setTextColor(ContextCompat.getColor(mContext, R.color.silver));
            groupType.setTextColor(ContextCompat.getColor(mContext,R.color.silver));
            amountOfMembersInGroup.setTextColor(ContextCompat.getColor(mContext,R.color.silver));
            amountOfMembersInGroupChange.setTextColor(ContextCompat.getColor(mContext, R.color.silver));
            titleForGroupManager.setTextColor(ContextCompat.getColor(mContext, R.color.silver));
            titleForGroupName.setTextColor(ContextCompat.getColor(mContext,R.color.silver));
            titleForGroupType.setTextColor(ContextCompat.getColor(mContext,R.color.silver));
        }

        return convertView;
    }
}
