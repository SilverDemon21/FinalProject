package com.example.finalproject.adminStaff;

import android.content.Context;
import android.widget.ArrayAdapter;

import com.example.finalproject.ShowAllUsers.User;
import com.example.finalproject.mainAplication.GroupOfUsers;
import com.example.finalproject.mainAplication.SavedLocation;

import java.util.List;

public class PendingGroupsAdapter extends ArrayAdapter<GroupOfUsers> {

    private Context mContext;
    private List<GroupOfUsers> groups;

    public PendingGroupsAdapter(Context context, List<GroupOfUsers> pendingGroups) {
        super(context, 0, locationList);
        mContext = context;
        groups = pendingGroups;

    }

}
