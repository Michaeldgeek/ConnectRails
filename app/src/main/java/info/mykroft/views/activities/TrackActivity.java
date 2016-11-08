package info.mykroft.views.activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.util.Iterator;

import info.mykroft.R;
import info.mykroft.models.Belongs;
import info.mykroft.models.CurrentLocation;
import info.mykroft.utils.Constants;

public class TrackActivity extends AppCompatActivity {
    private MaterialEditText cargoId;
    private Button findTrain;
    private MaterialDialog materialDialog;

    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_track);
        setupToolbar();
        cargoId = (MaterialEditText) findViewById(R.id.cargo_id);
        findTrain = (Button) findViewById(R.id.find_cargo);
        findTrain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                findCargo(cargoId.getText().toString());
            }
        });
    }

    private void findCargo(final String cargoId) {
        if (isNetworkAvailable()) {
            showMaterialDialog();
            final Boolean[] found = {false};
            final FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference myRef = database.getReference(Constants.CONNECT_RAILS);
            DatabaseReference belongsReference = myRef.child(Constants.BELONGS);
            belongsReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Iterable<DataSnapshot> snapshotIterable = dataSnapshot.getChildren();
                    if (snapshotIterable != null) {
                        Iterator<DataSnapshot> iterator = snapshotIterable.iterator();
                        while (iterator.hasNext()) {
                            Belongs belongs = iterator.next().getValue(Belongs.class);
                            if (belongs != null && belongs.getCargoId().contentEquals(cargoId)) {
                                findTrain(belongs.getTrainId());
                                found[0] = true;
                                break;
                            } else {
                                found[0] = false;
                            }
                        }
                        if (found[0] == false) {
                            dismissMaterialDialog();
                            Snackbar snackbar = Snackbar.make(getActivity().findViewById(android.R.id.content), "Cargo not found", Snackbar.LENGTH_LONG);
                            snackbar.show();
                        }

                    }

                }

                private void findTrain(final String trainId) {
                    final FirebaseDatabase database = FirebaseDatabase.getInstance();
                    DatabaseReference myRef = database.getReference(Constants.CONNECT_RAILS);
                    DatabaseReference curLocReference = myRef.child(Constants.CURRENT_LOCATION);
                    curLocReference.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            Iterable<DataSnapshot> snapshotIterable = dataSnapshot.getChildren();
                            if (snapshotIterable != null) {
                                Iterator<DataSnapshot> iterator = snapshotIterable.iterator();
                                while (iterator.hasNext()) {
                                    CurrentLocation currentLocation = iterator.next().getValue(CurrentLocation.class);
                                    if (currentLocation != null && currentLocation.getTrainId().contentEquals(trainId)) {
                                        found[0] = true;
                                        showOnMap(currentLocation);
                                        break;
                                    } else {
                                        found[0] = false;
                                    }
                                }
                                if (found[0] == false) {
                                    dismissMaterialDialog();
                                    Snackbar snackbar = Snackbar.make(getActivity().findViewById(android.R.id.content), "Train has not departed", Snackbar.LENGTH_LONG);
                                    snackbar.show();
                                }

                            }

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            dismissMaterialDialog();
                            Toast.makeText(getActivity(), databaseError.toString(), Toast.LENGTH_LONG).show();
                        }
                    });
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    dismissMaterialDialog();
                    Toast.makeText(getActivity(), databaseError.toString(), Toast.LENGTH_LONG).show();
                }
            });
        } else {
            Snackbar snackbar = Snackbar.make(getActivity().findViewById(android.R.id.content), "Network is not available", Snackbar.LENGTH_LONG);
            snackbar.show();
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager cm =
                (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
        return isConnected;
    }

    private void showOnMap(CurrentLocation currentLocation) {
        dismissMaterialDialog();
        Intent intent = new Intent(this, MapsActivity.class);
        LatLng latLng = new LatLng(currentLocation.getLat(), currentLocation.getLng());
        intent.putExtra("loc", latLng);
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dismissMaterialDialog();
    }

    private void dismissMaterialDialog() {
        if (materialDialog != null && materialDialog.isShowing()) {
            materialDialog.dismiss();
        }
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

    private TrackActivity getActivity() {
        return this;
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

    private void setupToolbar() {
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_back_white);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.colorPrimary)));
    }

}
