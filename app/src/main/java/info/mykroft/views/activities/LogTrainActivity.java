package info.mykroft.views.activities;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.util.ArrayList;
import java.util.HashMap;

import info.mykroft.R;
import info.mykroft.services.GeofenceTransitionsIntentservice;
import info.mykroft.services.LocationUpdates;
import info.mykroft.utils.Constants;

public class LogTrainActivity extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, ResultCallback<Status> {
    protected GoogleApiClient mGoogleApiClient;
    protected ArrayList<Geofence> mGeofenceList = new ArrayList<Geofence>();
    int PLACE_PICKER_REQUEST = 1;
    private MaterialEditText logTrains;
    private Button log;
    private String user;
    private MaterialDialog materialDialog;
    private boolean mGeofencesAdded;
    private PendingIntent mGeofencePendingIntent = null;
    private SharedPreferences mSharedPreferences;
    private double lat;
    private double lng;
    private LatLng latlng;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        if (intent == null) {
            Intent intent1 = new Intent(this, LoginActivity.class);
            intent1.putExtra(Constants.RETURN_TO, Constants.LOG_TRAIN);
            startActivity(intent1);
            this.finish();
        } else {
            user = getIntent().getStringExtra(Constants.USER);
            if (user == null) {
                Intent intent1 = new Intent(this, LoginActivity.class);
                intent1.putExtra(Constants.RETURN_TO, Constants.LOG_TRAIN);
                startActivity(intent1);
                this.finish();
            } else {
                setContentView(R.layout.activity_log_train);
                setupToolbar();
                logTrains = (MaterialEditText) findViewById(R.id.log_trains);
                log = (Button) findViewById(R.id.log_train_btn);
                log.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        logTrain(logTrains.getText().toString());
                    }
                });
                mSharedPreferences = getSharedPreferences(Constants.SHARED_PREFERENCES_NAME,
                        MODE_PRIVATE);
                mGeofencesAdded = mSharedPreferences.getBoolean(Constants.GEOFENCES_ADDED_KEY, false);
                buildGoogleApiClient();
            }
        }
    }

    private void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .build();
    }

    private LogTrainActivity getActivity() {
        return this;
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager cm =
                (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
        return isConnected;
    }

    private void logTrain(final String trainId) {
        if (isNetworkAvailable()) {
            showMaterialDialog();
            final FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference myRef = database.getReference(Constants.CONNECT_RAILS);
            final DatabaseReference trains = myRef.child(Constants.TRAINS);
            trains.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    DataSnapshot snapshot = dataSnapshot.child(trainId);
                    HashMap<String, String> map = (HashMap<String, String>) snapshot.getValue();
                    if (map != null) {
                        String value = map.get(Constants.TRAIN_ID);
                        if (value != null && value.contentEquals(trainId)) {
                            // train logged successful
                            HashMap<String, String> map1 = (HashMap<String, String>) snapshot.getValue();
                            String value1 = map1.get(Constants.DESTINATION);
                            String[] latStrings = value1.split(",");
                            lat = Double.parseDouble(latStrings[0].replace("lat/lng: (", ""));
                            lng = Double.parseDouble(latStrings[1].replace(")", ""));
                            latlng = new LatLng(lat, lng);
                            trains.child(trainId).child(Constants.STATUS).setValue(1, new DatabaseReference.CompletionListener() {
                                @Override
                                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                    dismissMaterialDialog();
                                    if (databaseError == null) {
                                        populateGeofenceList();
                                        startGeoFencing();
                                    } else {
                                        Snackbar snackbar = Snackbar.make(getActivity().findViewById(android.R.id.content), "Server error. Retry", Snackbar.LENGTH_LONG);
                                        snackbar.show();
                                    }
                                }
                            });

                        } else {
                            // failed
                            dismissMaterialDialog();
                            Snackbar snackbar = Snackbar.make(getActivity().findViewById(android.R.id.content), "Train not found", Snackbar.LENGTH_LONG);
                            snackbar.show();
                        }
                    } else {

                        dismissMaterialDialog();
                        Snackbar snackbar = Snackbar.make(getActivity().findViewById(android.R.id.content), "Train not found", Snackbar.LENGTH_LONG);
                        snackbar.show();
                    }
                }


                @Override
                public void onCancelled(DatabaseError databaseError) {
                    dismissMaterialDialog();
                    Snackbar snackbar = Snackbar.make(getActivity().findViewById(android.R.id.content), databaseError.toString(), Snackbar.LENGTH_LONG);
                    snackbar.show();
                }
            });
        } else {
            Snackbar snackbar = Snackbar.make(getActivity().findViewById(android.R.id.content), "Network is not available", Snackbar.LENGTH_LONG);
            snackbar.show();
        }
    }

    private void populateGeofenceList() {
        mGeofenceList.add(new Geofence.Builder()
                .setRequestId("key")
                .setCircularRegion(
                        lat,
                        lng,
                        Constants.GEOFENCE_RADIUS_IN_METERS
                )
                .setExpirationDuration(Constants.GEOFENCE_EXPIRATION_IN_MILLISECONDS)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER |
                        Geofence.GEOFENCE_TRANSITION_EXIT)
                .build());

    }

    private void startGeoFencing() {
        if (!mGoogleApiClient.isConnected()) {
            Snackbar snackbar = Snackbar.make(getActivity().findViewById(android.R.id.content), "Google Api is not connected", Snackbar.LENGTH_LONG);
            snackbar.show();
            return;
        }
        try {
            LocationServices.GeofencingApi.addGeofences(
                    mGoogleApiClient,
                    getGeofencingRequest(),
                    getGeofencePendingIntent()
            ).setResultCallback(LogTrainActivity.this);
        } catch (SecurityException securityException) {
            Snackbar snackbar = Snackbar.make(getActivity().findViewById(android.R.id.content), securityException.toString(), Snackbar.LENGTH_LONG);
            snackbar.show();
        }
    }

    private PendingIntent getGeofencePendingIntent() {
        if (mGeofencePendingIntent != null) {
            return mGeofencePendingIntent;
        }
        Intent intent = new Intent(LogTrainActivity.this, GeofenceTransitionsIntentservice.class);
        return PendingIntent.getService(LogTrainActivity.this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void dismissMaterialDialog() {
        if (materialDialog != null && materialDialog.isShowing()) {
            materialDialog.dismiss();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mGoogleApiClient.disconnect();
    }

    private GeofencingRequest getGeofencingRequest() {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
        builder.addGeofences(mGeofenceList);
        return builder.build();
    }


    private void showMaterialDialog() {
        materialDialog = new MaterialDialog.Builder(this)
                .content("Please wait...")
                .progress(true, 0)
                .autoDismiss(false)
                .cancelable(false)
                .backgroundColorRes(R.color.md_white_1000)
                .contentColor(this.getResources().getColor(R.color.text_color))
                .widgetColor(this.getResources().getColor(R.color.progress_bar_color))
                .show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dismissMaterialDialog();
    }


    private void setupToolbar() {
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_back_white);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.colorPrimary)));
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }


    @Override
    public void onResult(@NonNull Status status) {
        if (status.isSuccess()) {
            mGeofencesAdded = !mGeofencesAdded;
            Intent intent = new Intent(this, LocationUpdates.class);
            intent.putExtra(Constants.TRAIN_ID, logTrains.getText().toString());
            Snackbar snackbar = Snackbar.make(getActivity().findViewById(android.R.id.content), "Train successfully logged", Snackbar.LENGTH_LONG);
            snackbar.show();
            startService(intent);
            startActivity(new Intent(this, LogTrainSuccessActivity.class));
            finish();
        } else {
            Snackbar snackbar = Snackbar.make(getActivity().findViewById(android.R.id.content), status.getStatusMessage(), Snackbar.LENGTH_LONG);
            snackbar.show();
        }
    }
}
