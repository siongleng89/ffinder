package com.ffinder.android;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.Pair;
import android.view.View;
import android.widget.*;
import com.directions.route.*;
import com.ffinder.android.absint.activities.MyActivityAbstract;
import com.ffinder.android.enums.ActionBarActionType;
import com.ffinder.android.enums.AnalyticEvent;
import com.ffinder.android.extensions.ButtonWhite;
import com.ffinder.android.helpers.*;
import com.ffinder.android.statics.Constants;
import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.*;

import java.util.ArrayList;
import java.util.List;

public class ActivityMap extends MyActivityAbstract implements RoutingListener {

    private String username, userId,
            latitude, longitude, address, datetime;
    private String myLatitude, myLongitude;

    private Button btnDirections, btnStreetView, btnGps;
    private RelativeLayout layoutDirections, layoutStreetView;

    private ButtonWhite btnDrive, btnTransit, btnWalk;
    private RelativeLayout layoutDirectionContent, layoutDirectionLoading, layoutNoStreetView;
    private LinearLayout layoutDirectionDetails;
    private TextView txtDirectionTitle, txtDistance, txtDuration;
    private TableLayout tableDirections;

    private LatLng start, end;
    private MapReadyCallback mapReadyCallback;
    private GoogleMap map;
    private List<Polyline> polylines;
    private Routing routing;
    private Route driveRoute, transitRoute, walkRoute;
    private AbstractRouting.TravelMode currentTravelMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        enableCustomActionBar();
        setActionBarTitle(R.string.map_activity_title);
        addActionToActionBar(ActionBarActionType.Back, false, true);


        btnDirections = (Button) findViewById(R.id.btnDirections);
        btnStreetView = (Button) findViewById(R.id.btnStreetView);
        btnGps = (Button) findViewById(R.id.btnGps);
        layoutDirections = (RelativeLayout) findViewById(R.id.layoutDirections);
        layoutStreetView = (RelativeLayout) findViewById(R.id.layoutStreetView);

        btnDrive = (ButtonWhite) findViewById(R.id.btnDrive);
        btnTransit = (ButtonWhite) findViewById(R.id.btnTransit);
        btnWalk = (ButtonWhite) findViewById(R.id.btnWalk);
        layoutDirectionContent = (RelativeLayout) findViewById(R.id.layoutDirectionContent);
        layoutDirectionDetails = (LinearLayout) findViewById(R.id.layoutDirectionDetails);
        layoutDirectionLoading = (RelativeLayout) findViewById(R.id.layoutDirectionLoading);
        layoutNoStreetView = (RelativeLayout) findViewById(R.id.layoutNoStreetView);
        txtDirectionTitle = (TextView) findViewById(R.id.txtDirectonTitle);
        txtDistance = (TextView) findViewById(R.id.txtDistance);
        txtDuration = (TextView) findViewById(R.id.txtDuration);
        tableDirections = (TableLayout) findViewById(R.id.tableDirections);

        polylines = new ArrayList();

        Bundle bundle = this.getIntent().getExtras();

        userId = bundle.getString("userId");
        username = bundle.getString("username");
        latitude = bundle.getString("latitude");
        longitude = bundle.getString("longitude");
        address = bundle.getString("address");
        datetime = bundle.getString("datetime");

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapReadyCallback = new MapReadyCallback();
        mapFragment.getMapAsync(mapReadyCallback);


        SupportStreetViewPanoramaFragment streetViewPanoramaFragment =
                (SupportStreetViewPanoramaFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.streetViewMap);
        streetViewPanoramaFragment.getStreetViewPanoramaAsync(new MapStreetViewReadyCallback());

        setListeners();
    }

    private void changeTravelMode(final AbstractRouting.TravelMode travelMode){
        if(map == null){
            map = mapReadyCallback.getGoogleMap();
        };

        if(map == null) return;

        if(travelMode == AbstractRouting.TravelMode.DRIVING){
            btnTransit.setSelected(false, false);
            btnWalk.setSelected(false, false);
            btnDrive.setSelected(true, false);
        }
        else if(travelMode == AbstractRouting.TravelMode.WALKING){
            btnTransit.setSelected(false, false);
            btnWalk.setSelected(true, false);
            btnDrive.setSelected(false, false);
        }
        else if(travelMode == AbstractRouting.TravelMode.TRANSIT){
            btnTransit.setSelected(true, false);
            btnWalk.setSelected(false, false);
            btnDrive.setSelected(false, false);
        }

        AnimateBuilder.fadeOutAndSetGone(this, layoutDirectionContent, new Runnable() {
            @Override
            public void run() {
                layoutDirectionDetails.setVisibility(View.VISIBLE);
            }
        });

        AnimateBuilder.fadeIn(this, layoutDirectionLoading, new Runnable() {
            @Override
            public void run() {
                if (Strings.isEmpty(myLatitude)){
                    new LocationUpdater(ActivityMap.this, new RunnableArgs<Pair<String, String>>() {
                        @Override
                        public void run() {
                            myLatitude = this.getFirstArg().first;
                            myLongitude = this.getFirstArg().second;
                            startGetDirections(travelMode);
                        }
                    });
                }
                else{
                    startGetDirections(travelMode);
                }
            }
        });




    }

    private void startGetDirections(AbstractRouting.TravelMode travelMode){
        start = new LatLng(Double.valueOf(myLatitude), Double.valueOf(myLongitude));
        end = new LatLng(Double.valueOf(latitude), Double.valueOf(longitude));

        txtDirectionTitle.setText(getString(
                AndroidUtils.getStringIdentifier(this, travelMode.name().toLowerCase())));

        if(routing != null){
            routing.cancel(true);
        }

        if(travelMode == AbstractRouting.TravelMode.DRIVING && driveRoute != null){
            Threadings.delay(600, new Runnable() {
                @Override
                public void run() {
                    handleRoutingResult(driveRoute);
                }
            });
            return;
        }
        else if(travelMode == AbstractRouting.TravelMode.WALKING && walkRoute != null){
            Threadings.delay(600, new Runnable() {
                @Override
                public void run() {
                    handleRoutingResult(walkRoute);
                }
            });
            return;
        }
        else if(travelMode == AbstractRouting.TravelMode.TRANSIT && transitRoute != null){
            Threadings.delay(600, new Runnable() {
                @Override
                public void run() {
                    handleRoutingResult(transitRoute);
                }
            });
            return;
        }


        routing = new Routing.Builder()
                .travelMode(travelMode)
                .withListener(this)
                .waypoints(start, end)
                .key(Constants.GoogleApiKey)
                .language(LocaleHelper.getLanguage(this))
                .build();
        routing.execute();

        currentTravelMode = travelMode;

    }


    @Override
    public void onRoutingFailure(RouteException e) {
        txtDirectionTitle.setText(R.string.no_route_found_error_msg);

        txtDistance.setText(R.string.unknown);
        txtDuration.setText(R.string.unknown);

        tableDirections.removeAllViews();

        AnimateBuilder.fadeOut(ActivityMap.this, layoutDirectionLoading, new Runnable() {
            @Override
            public void run() {
                AnimateBuilder.fadeIn(ActivityMap.this, layoutDirectionContent);
            }
        });
    }

    @Override
    public void onRoutingStart() {

    }

    @Override
    public void onRoutingSuccess(ArrayList<Route> routes, int shortestRouteIndex) {

        routing = null;

        Route route = routes.get(shortestRouteIndex);

        if(currentTravelMode == AbstractRouting.TravelMode.DRIVING){
            driveRoute = route;
        }
        else if(currentTravelMode == AbstractRouting.TravelMode.WALKING){
            walkRoute = route;
        }
        else if(currentTravelMode == AbstractRouting.TravelMode.TRANSIT){
            transitRoute = route;
        }

        handleRoutingResult(route);

        Analytics.logEvent(AnalyticEvent.Use_Direction, "Use direction: " + currentTravelMode.name());
    }

    @Override
    public void onRoutingCancelled() {

    }

    private void handleRoutingResult(Route route){
        AnimateBuilder.fadeOut(ActivityMap.this, layoutDirectionLoading, new Runnable() {
            @Override
            public void run() {
                AnimateBuilder.fadeIn(ActivityMap.this, layoutDirectionContent);
            }
        });


        CameraUpdate center = CameraUpdateFactory.newLatLng(start);
        CameraUpdate zoom = CameraUpdateFactory.zoomTo(16);

        map.moveCamera(center);


        //draw polylines
        if(polylines.size()>0) {
            for (Polyline poly : polylines) {
                poly.remove();
            }
        }

        polylines = new ArrayList<>();
        PolylineOptions polyOptions = new PolylineOptions();
        // polyOptions.color(getResources().getColor(COLORS[colorIndex]));
        polyOptions.color(ContextCompat.getColor(this, R.color.colorPrimaryDark));
        polyOptions.width(10);
        polyOptions.addAll(route.getPoints());
        Polyline polyline = map.addPolyline(polyOptions);
        polylines.add(polyline);


        // Start marker
        MarkerOptions options = new MarkerOptions();
        options.position(start);
        //options.icon(BitmapDescriptorFactory.fromResource(R.drawable.));
        map.addMarker(options);

        // End marker
        options = new MarkerOptions();
        options.position(end);
        //options.icon(BitmapDescriptorFactory.fromResource(R.drawable.end_green));
        map.addMarker(options);

        //update ui
        txtDistance.setText(route.getDistanceText());
        txtDuration.setText(route.getDurationText());

        tableDirections.removeAllViews();
        int i = 1;
        for(Segment segment : route.getSegments()){

            TableRow tableRow = (TableRow) getLayoutInflater().inflate(R.layout.tablerow_direction, null);
            tableDirections.addView(tableRow);

            TextView txtIndex = (TextView) tableRow.findViewById(R.id.txtIndex);
            TextView txtDirection = (TextView) tableRow.findViewById(R.id.txtDirection);

            txtIndex.setText(i + ")");
            txtDirection.setText(segment.getInstruction());
            i++;
        }
    }


    private class MapReadyCallback implements OnMapReadyCallback{

        private GoogleMap googleMap;

        public MapReadyCallback() {
        }



        @Override
        public void onMapReady(GoogleMap googleMap) {
            this.googleMap = googleMap;

            if(Strings.isEmpty(latitude) || Strings.isEmpty(longitude)) return;

            googleMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {

                @Override
                public View getInfoWindow(Marker arg0) {
                    return null;
                }

                @Override
                public View getInfoContents(Marker arg0) {
                    View v = getLayoutInflater().inflate(R.layout.marker_layout, null);

                    ImageView imgViewProfile = (ImageView) v.findViewById(R.id.imageViewProfile);
                    TextView txtUserName = (TextView) v.findViewById(R.id.txtName);
                    TextView txtAddress = (TextView) v.findViewById(R.id.txtAddress);
                    TextView txtDateTime = (TextView) v.findViewById(R.id.txtDateTime);

                    txtUserName.setText(username);
                    txtAddress.setText(address);
                    txtDateTime.setText(datetime);

                    Bitmap bitmap = AndroidUtils.loadImageFromStorage(ActivityMap.this, userId);
                    if (bitmap != null){
                        imgViewProfile.setImageBitmap(bitmap);
                    }


                    return v;

                }
            });


            LatLng latLng = new LatLng(Double.valueOf(latitude), Double.valueOf(longitude));
            LatLng markerLatLng = new LatLng(Double.valueOf(latitude), Double.valueOf(longitude));

            Marker mapMarker = googleMap.addMarker(new MarkerOptions().alpha(1)
                    .position(markerLatLng));
            mapMarker.showInfoWindow();


//            Circle circle = googleMap.addCircle(new CircleOptions()
//                    .center(latLng)
//                    .radius(500)
//                    .strokeColor(Color.RED)
//                    .fillColor(Color.argb(50, 170, 57, 57)));

            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15), 1, new GoogleMap.CancelableCallback() {
                @Override
                public void onFinish() {

                }

                @Override
                public void onCancel() {

                }
            });
        }

        public GoogleMap getGoogleMap() {
            return googleMap;
        }

        public void setGoogleMap(GoogleMap googleMap) {
            this.googleMap = googleMap;
        }
    }

    private class MapStreetViewReadyCallback implements OnStreetViewPanoramaReadyCallback{

        @Override
        public void onStreetViewPanoramaReady(final StreetViewPanorama streetViewPanorama) {
            streetViewPanorama.setPosition(new LatLng(Double.valueOf(latitude),
                    Double.valueOf(longitude)));
            Threadings.delay(5 * 1000, new Runnable() {
                @Override
                public void run() {
                    if(disposed) return;

                    if (streetViewPanorama.getLocation() == null) {
                        AnimateBuilder.fadeIn(ActivityMap.this, layoutNoStreetView);
                    }
                }
            });
        }
    }

    private void setListeners(){
        btnGps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Analytics.logEvent(AnalyticEvent.Use_GPS_NAV);
                String uri = "geo:" + latitude + ","
                        + longitude + "?q=" + latitude
                        + "," + longitude;
                startActivity(new Intent(android.content.Intent.ACTION_VIEW,
                        Uri.parse(uri)));
            }
        });

        btnDirections.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AnimateBuilder.fadeOutAndSetGone(ActivityMap.this, layoutStreetView, new Runnable() {
                    @Override
                    public void run() {
                        AnimateBuilder.fadeIn(ActivityMap.this, layoutDirections);
                    }
                });
            }
        });

        btnStreetView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AnimateBuilder.fadeOutAndSetGone(ActivityMap.this, layoutDirections, new Runnable() {
                    @Override
                    public void run() {
                        AnimateBuilder.fadeIn(ActivityMap.this, layoutStreetView);
                    }
                });
            }
        });

        btnDrive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeTravelMode(AbstractRouting.TravelMode.DRIVING);
            }
        });

        btnTransit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeTravelMode(AbstractRouting.TravelMode.TRANSIT);
            }
        });

        btnWalk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeTravelMode(AbstractRouting.TravelMode.WALKING);
            }
        });



    }




}
