package info.mykroft.views.activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
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
import info.mykroft.adapters.CargoesAdapter;
import info.mykroft.models.Belongs;
import info.mykroft.models.Cargo;
import info.mykroft.utils.Constants;

public class CargoesOnTrainActivity extends AppCompatActivity {
    private RecyclerView cargoRecyclerView;
    private CargoesAdapter cargoesAdapter;
    private ArrayList<Cargo> cargoes = new ArrayList<>();
    private MaterialDialog materialDialog;
    private ProgressWheel loading;
    private String trainId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        if (intent == null) {
            startActivity(new Intent(this, TrainsActivity.class));
            this.finish();
        } else {
            trainId = getIntent().getStringExtra(Constants.TRAIN_ID);
            if (trainId == null) {
                startActivity(new Intent(this, TrainsActivity.class));
                this.finish();
            } else {
                setContentView(R.layout.activity_cargoes_on_train);
                setupToolbar();
                loading = (ProgressWheel) findViewById(R.id.progress_wheel_frag);
                cargoRecyclerView = (RecyclerView) findViewById(R.id.rv);
                cargoesAdapter = new CargoesAdapter(this, cargoes);
                cargoRecyclerView.setLayoutManager(new LinearLayoutManager(this));
                cargoRecyclerView.setAdapter(cargoesAdapter);
                fetchData();
            }
        }
    }

    private void fetchData() {
        if(isNetworkAvailable()) {
            final FirebaseDatabase database = FirebaseDatabase.getInstance();
            final DatabaseReference myRef = database.getReference(Constants.CONNECT_RAILS);
            DatabaseReference belongsReference = myRef.child(Constants.BELONGS);
            belongsReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Iterable<DataSnapshot> snapshotIterable = dataSnapshot.getChildren();
                    if (snapshotIterable != null) {
                        Iterator<DataSnapshot> iterator = snapshotIterable.iterator();
                        if (cargoes.size() > 0) cargoes.clear();
                        ArrayList<Belongs> belongsArrayList = new ArrayList<Belongs>();
                        while (iterator.hasNext()) {
                            belongsArrayList.add(iterator.next().getValue(Belongs.class));
                        }
                        if (belongsArrayList.size() > 0) {
                            for (int i = 0; i < belongsArrayList.size(); i++) {
                                final Integer j = i;
                                Belongs belongs = belongsArrayList.get(i);
                                if (belongs.getTrainId().contentEquals(trainId)) {
                                    final String cargoId = belongs.getCargoId();
                                    DatabaseReference databaseReference = myRef.child(Constants.CARGO);
                                    databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            Iterable<DataSnapshot> snapshotIterable = dataSnapshot.getChildren();
                                            if (snapshotIterable != null) {
                                                Iterator<DataSnapshot> iterator = snapshotIterable.iterator();
                                                while (iterator.hasNext()) {
                                                    Cargo cargo = iterator.next().getValue(Cargo.class);
                                                    if (cargo.getUniqueId().contentEquals(cargoId))
                                                        cargoes.add(cargo);
                                                }
                                            }
                                            if (cargoes.size() - 1 == j) {
                                                cargoesAdapter.addData(cargoes);
                                                cargoRecyclerView.setVisibility(View.VISIBLE);
                                                loading.setVisibility(View.GONE);
                                            }
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {
                                            loading.setVisibility(View.GONE);
                                            Toast.makeText(getActivity(), databaseError.toString(), Toast.LENGTH_LONG).show();
                                        }
                                    });
                                } else {
                                    if (cargoes.size() - 1 == j) {
                                        cargoesAdapter.addData(cargoes);
                                        cargoRecyclerView.setVisibility(View.VISIBLE);
                                        loading.setVisibility(View.GONE);
                                    }
                                }
                            }
                        } else {
                            loading.setVisibility(View.GONE);
                            cargoRecyclerView.setVisibility(View.GONE);
                            Snackbar snackbar = Snackbar.make(getActivity().findViewById(android.R.id.content), "Train left with no cargo", Snackbar.LENGTH_LONG);
                            snackbar.show();
                        }
                    }

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Toast.makeText(getActivity(), databaseError.toString(), Toast.LENGTH_LONG).show();
                }
            });
        }
        else {
            Snackbar snackbar = Snackbar.make(getActivity().findViewById(android.R.id.content), "Network is not available", Snackbar.LENGTH_LONG);
            snackbar.show();
        }
    }

    private void dismissMaterialDialog() {
        if (materialDialog != null && materialDialog.isShowing()) {
            materialDialog.dismiss();
        }
    }

    public String getTrainId() {
        return trainId;
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
        dismissMaterialDialog();
    }

    private CargoesOnTrainActivity getActivity() {
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
