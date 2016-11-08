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

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.security.SecureRandom;

import info.mykroft.R;
import info.mykroft.models.Belongs;
import info.mykroft.models.Cargo;
import info.mykroft.utils.Constants;

public class CargoActivity extends AppCompatActivity {
    private MaterialEditText customerName;
    private MaterialEditText goodsType;
    private MaterialEditText qty;
    private Button enter;
    private MaterialDialog materialDialog;
    private String trainId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        if (intent == null) {
            startActivity(new Intent(this, LoginActivity.class));
            this.finish();
        } else {
            final String user = getIntent().getStringExtra(Constants.USER);
            trainId = getIntent().getStringExtra(Constants.TRAIN_ID);
            if (user == null || trainId == null) {
                startActivity(new Intent(this, LoginActivity.class));
                this.finish();
            } else {
                setContentView(R.layout.activity_cargo);
                setupToolbar();
                customerName = (MaterialEditText) findViewById(R.id.ct_name);
                goodsType = (MaterialEditText) findViewById(R.id.goods_type);
                qty = (MaterialEditText) findViewById(R.id.qty);
                enter = (Button) findViewById(R.id.enter);
                enter.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        submit(customerName.getText().toString(), goodsType.getText().toString(), qty.getText().toString(), user);
                    }
                });
            }
        }
    }

    private String generateRandomUniqueId(int len) {
        String ab = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
        SecureRandom secureRandom = new SecureRandom();
        StringBuilder uniqueString = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            uniqueString.append(ab.charAt(secureRandom.nextInt(ab.length())));
        }
        return uniqueString.toString();
    }

    private void submit(final String customerName, final String goodsType, final String qty, final String user) {
        if(isNetworkAvailable()) {
            showMaterialDialog();
            final FirebaseDatabase database = FirebaseDatabase.getInstance();
            final DatabaseReference myRef = database.getReference(Constants.CONNECT_RAILS);
            DatabaseReference cargoes = myRef.child(Constants.CARGO);
            final String uniqueId = generateRandomUniqueId(12);
            DatabaseReference cargoRef = cargoes.child(uniqueId);
            Cargo cargo = new Cargo();
            cargo.setCustomerName(customerName);
            cargo.setGoodsType(goodsType);
            cargo.setQty(qty);
            cargo.setMonitoringOfficer(user);
            cargo.setUniqueId(uniqueId);
            cargoRef.setValue(cargo, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                    dismissMaterialDialog();
                    if (databaseError == null) {
                        Intent intent = new Intent(getActivity(), CargoSuccessActivity.class);
                        intent.putExtra(Constants.TRACKING_ID, uniqueId);
                        addCargoToTrain();
                        getActivity().startActivity(intent);
                        getActivity().finish();
                    } else {
                        Snackbar snackbar = Snackbar.make(getActivity().findViewById(android.R.id.content), databaseError.toString(), Snackbar.LENGTH_LONG);
                        snackbar.show();
                    }
                }

                private void addCargoToTrain() {
                    DatabaseReference belongs = myRef.child(Constants.BELONGS).push();
                    Belongs belong = new Belongs();
                    belong.setCargoId(uniqueId);
                    belong.setTrainId(trainId);
                    belongs.setValue(belong, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                            dismissMaterialDialog();
                            if (databaseError == null) {
                            } else {
                                Snackbar snackbar = Snackbar.make(getActivity().findViewById(android.R.id.content), databaseError.toString(), Snackbar.LENGTH_LONG);
                                snackbar.show();
                            }
                        }
                    });

                }
            });
        }
        else {
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

    private void dismissMaterialDialog() {
        if (materialDialog != null && materialDialog.isShowing()) {
            materialDialog.dismiss();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dismissMaterialDialog();
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

    private CargoActivity getActivity() {
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
