package com.example.finalproject.mainAplication;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.bumptech.glide.Glide;
import com.example.finalproject.R;
import com.example.finalproject.ShowAllUsers.Object_User;

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

        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.member_of_group_item, parent, false);
        }

        TextView memberUserName = convertView.findViewById(R.id.memberUserName);
        TextView memberName = convertView.findViewById(R.id.memberName);
        ConstraintLayout membersInGroupLayout = convertView.findViewById(R.id.membersInGroupLayout);
        CircleImageView MemberPhoto = convertView.findViewById(R.id.MemberPhoto);

        if(clickedObjectUser != null){
            memberUserName.setText(clickedObjectUser.getUsername());
            memberName.setText(clickedObjectUser.getName());

            Glide.with(mContext)
                    .load(clickedObjectUser.getPhotoUrl())
                    .into(MemberPhoto);
        }

        if(memberWithStatus.get("Manager").equals(clickedObjectUser.getUsername())){
            membersInGroupLayout.setBackgroundResource(R.drawable.background_user_groups_active_item);
        }

        return convertView;
    }
}
