package info.mykroft.views.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.pnikosis.materialishprogress.ProgressWheel;

import java.util.ArrayList;
import java.util.Iterator;

import info.mykroft.R;
import info.mykroft.adapters.TrainsAdapter;
import info.mykroft.models.CurrentLocation;
import info.mykroft.models.Train;
import info.mykroft.utils.Constants;

public class TrainsActivity extends AppCompatActivity {
    BroadcastReceiver trainAdded;
    private FloatingActionButton newTrain;
    private RecyclerView trainsRecyclerView;
    private TextView emptyData;
    private TrainsAdapter trainsAdapter;
    private ArrayList<Train> trains = new ArrayList<>();
    private MaterialDialog materialDialog;
    private ProgressWheel loading;
    private String user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        if (intent == null) {
            startActivity(new Intent(this, LoginActivity.class));
            this.finish();
        } else {
            user = getIntent().getStringExtra(Constants.USER);
            if (user == null) {
                startActivity(new Intent(this, LoginActivity.class));
                this.finish();
            } else {
                setContentView(R.layout.activity_trains);
                setupToolbar();
                emptyData = (TextView) findViewById(R.id.empty_data);
                trainAdded = new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        fetchData();
                    }
                };
                loading = (ProgressWheel) findViewById(R.id.progress_wheel_frag);
                LocalBroadcastManager.getInstance(this).registerReceiver(trainAdded, new IntentFilter(Constants.TRAIN_ADDED));
                newTrain = (FloatingActionButton) findViewById(R.id.new_train);
                newTrain.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        getActivity().startActivity(new Intent(getActivity(), NewTrainActivity.class));
                    }
                });
                trainsRecyclerView = (RecyclerView) findViewById(R.id.trains_rv);
                trainsAdapter = new TrainsAdapter(this, trains);
                trainsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
                trainsRecyclerView.setAdapter(trainsAdapter);
                fetchData();
            }
        }
    }

    private void fetchData() {
        if (isNetworkAvailable()) {
            final FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference myRef = database.getReference(Constants.CONNECT_RAILS);
            final DatabaseReference trainsReference = myRef.child(Constants.TRAINS);
            trainsReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Iterable<DataSnapshot> snapshotIterable = dataSnapshot.getChildren();
                    if (snapshotIterable != null) {
                        Iterator<DataSnapshot> iterator = snapshotIterable.iterator();
                        if (trains.size() > 0) trains.clear();
                        while (iterator.hasNext()) {
                            trains.add(iterator.next().getValue(Train.class));
                        }
                        if (trains.size() > 0) {
                            emptyData.setVisibility(View.GONE);
                            for (int i = 0; i < trains.size(); i++) {
                                final Integer j = i;
                                if (trains.get(i).getStatus() == 1) {
                                    final String trainId = trains.get(i).getTrainId();
                                    final FirebaseDatabase database = FirebaseDatabase.getInstance();
                                    DatabaseReference myRef = database.getReference(Constants.CONNECT_RAILS);
                                    final DatabaseReference curLocReference = myRef.child(Constants.CURRENT_LOCATION);
                                    curLocReference.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            Iterable<DataSnapshot> snapshotIterable = dataSnapshot.getChildren();
                                            if (snapshotIterable != null) {
                                                Iterator<DataSnapshot> iterator = snapshotIterable.iterator();
                                                while (iterator.hasNext()) {
                                                    CurrentLocation currentLocation = iterator.next().getValue(CurrentLocation.class);
                                                    if (currentLocation != null && currentLocation.getTrainId().contentEquals(trainId)) {
                                                        trains.get(j).setCurLoc(currentLocation);
                                                    }
                                                }
                                                if (trains.size() - 1 == j) {
                                                    trainsAdapter.addData(trains);
                                                    newTrain.setVisibility(View.VISIBLE);
                                                    trainsRecyclerView.setVisibility(View.VISIBLE);
                                                    loading.setVisibility(View.GONE);
                                                }
                                            }

                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {
                                            dismissMaterialDialog();
                                            Toast.makeText(getActivity(), databaseError.toString(), Toast.LENGTH_LONG).show();
                                        }
                                    });

                                } else {
                                    if (trains.size() - 1 == j) {
                                        trainsAdapter.addData(trains);
                                        newTrain.setVisibility(View.VISIBLE);
                                        trainsRecyclerView.setVisibility(View.VISIBLE);
                                        loading.setVisibility(View.GONE);
                                    }
                                }
                            }

                        } else {
                            // no trains
                            emptyData.setVisibility(View.VISIBLE);
                            newTrain.setVisibility(View.VISIBLE);
                            loading.setVisibility(View.GONE);
                            trainsRecyclerView.setVisibility(View.GONE);
                        }
                    }

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    loading.setVisibility(View.GONE);
                    Toast.makeText(getActivity(), databaseError.toString(), Toast.LENGTH_LONG).show();
                }
            });
        } else {
            Snackbar snackbar = Snackbar.make(getActivity().findViewById(android.R.id.content), "Network is not available", Snackbar.LENGTH_LONG);
            snackbar.show();
        }
    }

    private void dismissMaterialDialog() {
        if (materialDialog != null && materialDialog.isShowing()) {
            materialDialog.dismiss();
        }
    }

    public String getUser() {
        return user;
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

    private boolean isNetworkAvailable() {
        ConnectivityManager cm =
                (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
        return isConnected;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (trainAdded != null) {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(trainAdded);
        }
        dismissMaterialDialog();
    }

    private TrainsActivity getActivity() {
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
