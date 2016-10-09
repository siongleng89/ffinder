package com.ffinder.android;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import com.ffinder.android.absint.activities.MyActivityAbstract;
import com.ffinder.android.enums.AnalyticEvent;
import com.ffinder.android.helpers.Analytics;
import com.ffinder.android.utils.Strings;
import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.*;

public class ActivityMap extends MyActivityAbstract implements OnMapReadyCallback{

    private String username, latitude, longitude, address, datetime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setTitle(R.string.map_activity_title);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Bundle bundle = this.getIntent().getExtras();
        username = bundle.getString("username");
        latitude = bundle.getString("latitude");
        longitude = bundle.getString("longitude");
        address = bundle.getString("address");
        datetime = bundle.getString("datetime");

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.map_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        else if (item.getItemId() == R.id.action_direction) {
            Analytics.logEvent(AnalyticEvent.Use_Direction);
            String uri = "geo:" + latitude + ","
                    + longitude + "?q=" + latitude
                    + "," + longitude;
            startActivity(new Intent(android.content.Intent.ACTION_VIEW,
                    Uri.parse(uri)));
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        if(Strings.isEmpty(latitude) || Strings.isEmpty(longitude)) return;


        googleMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {

            @Override
            public View getInfoWindow(Marker arg0) {
                return null;
            }

            @Override
            public View getInfoContents(Marker arg0) {
                View v = getLayoutInflater().inflate(R.layout.marker_layout, null);

                TextView txtUserName = (TextView) v.findViewById(R.id.txtName);
                TextView txtAddress = (TextView) v.findViewById(R.id.txtAddress);
                TextView txtDateTime = (TextView) v.findViewById(R.id.txtDateTime);

                txtUserName.setText(username);
                txtAddress.setText(address);
                txtDateTime.setText(datetime);

                return v;

            }
        });


        LatLng latLng = new LatLng(Double.valueOf(latitude), Double.valueOf(longitude));
        LatLng markerLatLng = new LatLng(Double.valueOf(latitude) + 0.0025, Double.valueOf(longitude));

        Marker mapMarker = googleMap.addMarker(new MarkerOptions().alpha(0)
                .position(markerLatLng));
        mapMarker.showInfoWindow();


        Circle circle = googleMap.addCircle(new CircleOptions()
                .center(latLng)
                .radius(500)
                .strokeColor(Color.RED)
                .fillColor(Color.argb(50, 170, 57, 57)));

        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15), 1, new GoogleMap.CancelableCallback() {
            @Override
            public void onFinish() {

            }

            @Override
            public void onCancel() {

            }
        });
    }
}
