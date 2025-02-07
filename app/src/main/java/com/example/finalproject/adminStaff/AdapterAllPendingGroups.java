package com.example.finalproject.adminStaff;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.finalproject.R;
import com.example.finalproject.mainAplication.Object_GroupOfUsers;

import java.util.List;

public class AdapterAllPendingGroups extends ArrayAdapter<Object_GroupOfUsers> {

    private Context mContext;
    private List<Object_GroupOfUsers> groups;

    public AdapterAllPendingGroups(Context context, List<Object_GroupOfUsers> pendingGroups) {
        super(context, 0, pendingGroups);
        mContext = context;
        groups = pendingGroups;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Object_GroupOfUsers pendingGroup = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.pending_group_item, parent, false);
        }

        TextView groupNameTextView = convertView.findViewById(R.id.groupNameTextView);
        TextView groupCreatorUsernameTextView = convertView.findViewById(R.id.groupCreatorUsernameTextView);


        if(pendingGroup != null){
            groupNameTextView.setText(pendingGroup.getGroupName());
            groupCreatorUsernameTextView.setText(String.valueOf(pendingGroup.getGroupUsers().get("Manager")));
        }

        return convertView;
    }
}



