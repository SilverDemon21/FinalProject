package com.example.finalproject.mainAplication;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.finalproject.R;
import com.example.finalproject.ShowAllUsers.User;

import java.util.List;

public class LocationAdapter extends ArrayAdapter<SavedLocation> {

    private Context mContext;
    private List<SavedLocation> locations;

    public LocationAdapter(Context context, List<SavedLocation> locationList) {
        super(context, 0, locationList);
        mContext = context;
        locations = locationList;

    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        SavedLocation location = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.saved_location_item, parent, false);
        }

        TextView addressTextView = convertView.findViewById(R.id.addressTextView);
        TextView latitudeTextView = convertView.findViewById(R.id.latitudeTextView);
        TextView longitudeTextView = convertView.findViewById(R.id.longitudeTextView);

        if(location != null){
            addressTextView.setText(location.getAddress());
            latitudeTextView.setText(String.valueOf(location.getLatitude()));
            longitudeTextView.setText(String.valueOf(location.getLongitude()));
        }

        return convertView;
    }
}
