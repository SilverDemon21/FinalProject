package com.example.finalproject.mainAplication;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;

import com.example.finalproject.AllObjects.Object_SavedLocation;

public class NavigationHelper {
    public static void navigateToLocation(Context context, Object_SavedLocation location, boolean isGoogleMaps) {
        String uri;

        if (isGoogleMaps) {
            uri = "geo:" + location.getLatitude() + "," + location.getLongitude() + "?q=" + Uri.encode(location.getAddress());
        } else {
            uri = "https://waze.com/ul?ll=" + location.getLatitude() + "," + location.getLongitude() + "&navigate=yes";
        }


        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));

        // No setPackage for this test
        try {
            context.startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(context, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }


}
