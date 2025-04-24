package com.example.finalproject.AllAdapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.finalproject.AllObjects.Object_SavedLocation;
import com.example.finalproject.R;

import java.util.List;

public class AdapterSavedLocations extends ArrayAdapter<Object_SavedLocation> {

    private Context mContext;

    public AdapterSavedLocations(Context context, List<Object_SavedLocation> locationList) {
        super(context, 0, locationList);
        mContext = context;

    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Object_SavedLocation location = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.saved_location_item, parent, false);
        }

        TextView addressTextView = convertView.findViewById(R.id.addressTextView);
        TextView latitudeTextView = convertView.findViewById(R.id.latitudeTextView);
        TextView longitudeTextView = convertView.findViewById(R.id.longitudeTextView);
        TextView titleTextView = convertView.findViewById(R.id.tileTextView);

        if(location != null){
            addressTextView.setText(location.getAddress());
            latitudeTextView.setText(String.valueOf(location.getLatitude()));
            longitudeTextView.setText(String.valueOf(location.getLongitude()));
            titleTextView.setText(location.getTitle());
        }

        return convertView;
    }
}
