package info.mykroft.views.activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;
import com.wdullaer.materialdatetimepicker.time.RadialPickerLayout;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;

import java.util.Calendar;
import java.util.HashMap;

import info.mykroft.R;
import info.mykroft.models.Train;
import info.mykroft.utils.Constants;

public class NewTrainActivity extends AppCompatActivity {
    private static final int DEST_PICKER_REQUEST = 2;
    private static final int ORIGIN_PICKER_REQUEST = 1;
    private MaterialEditText trainId;
    private TextView departureTime;
    private TextView departureDay;
    private TextView destination;
    private TextView origin;
    private Button save;
    private MaterialDialog materialDialog;
    private LatLng latlngOrigin;
    private LatLng latlngDest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_train);
        setupToolbar();
        trainId = (MaterialEditText) findViewById(R.id.train_id);
        departureTime = (TextView) findViewById(R.id.departure_time);
        departureDay = (TextView) findViewById(R.id.departure_day);
        destination = (TextView) findViewById(R.id.destination);
        origin = (TextView) findViewById(R.id.origin);
        save = (Button) findViewById(R.id.save);
        departureDay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar now = Calendar.getInstance();
                DatePickerDialog dpd = DatePickerDialog.newInstance(
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
                                String date = dayOfMonth + "/" + (monthOfYear + 1) + "/" + year;
                                departureDay.setText(date);
                            }
                        },
                        now.get(Calendar.YEAR),
                        now.get(Calendar.MONTH),
                        now.get(Calendar.DAY_OF_MONTH)
                );
                dpd.show(getFragmentManager(), "Datepickerdialog");
            }
        });
        departureTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar now = Calendar.getInstance();
                TimePickerDialog timePickerDialog = TimePickerDialog.newInstance(new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(RadialPickerLayout view, int hourOfDay, int minute, int second) {
                        String time = +hourOfDay + ":" + minute + ":" + second;
                        departureTime.setText(time);
                    }
                }, now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE), false);
                timePickerDialog.show(getFragmentManager(), "Timepickerdialog");
            }
        });
        origin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
                try {
                    startActivityForResult(builder.build(NewTrainActivity.this), ORIGIN_PICKER_REQUEST);
                } catch (GooglePlayServicesRepairableException e) {
                    e.printStackTrace();
                } catch (GooglePlayServicesNotAvailableException e) {
                    e.printStackTrace();
                }
            }
        });
        destination.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
                try {
                    startActivityForResult(builder.build(NewTrainActivity.this), DEST_PICKER_REQUEST);
                } catch (GooglePlayServicesRepairableException e) {
                    e.printStackTrace();
                } catch (GooglePlayServicesNotAvailableException e) {
                    e.printStackTrace();
                }
            }
        });
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (trainId.getText().toString().trim().length() < 1 || origin.getText().toString().trim().contentEquals("Origin") || destination.getText().toString().trim().contentEquals("Destination") || departureDay.getText().toString().trim().contentEquals("Departure day") || departureTime.getText().toString().trim().contentEquals("Departure time")) {
                    Snackbar snackbar = Snackbar.make(getActivity().findViewById(android.R.id.content), "Field cant be empty", Snackbar.LENGTH_LONG);
                    snackbar.show();
                } else {
                    submit(trainId.getText().toString(), departureTime.getText().toString(), departureDay.getText().toString(), (String) destination.getTag(), (String) origin.getTag(), destination.getText().toString(), origin.getText().toString());
                }
            }
        });
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == ORIGIN_PICKER_REQUEST) {
            if (resultCode == RESULT_OK) {
                Place place = PlacePicker.getPlace(data, this);
                latlngOrigin = place.getLatLng();
                origin.setText(place.getAddress());
                origin.setTag(latlngOrigin.toString());
            }
        } else if (requestCode == DEST_PICKER_REQUEST) {
            if (resultCode == RESULT_OK) {
                Place place = PlacePicker.getPlace(data, this);
                latlngDest = place.getLatLng();
                destination.setText(place.getAddress());
                destination.setTag(latlngDest.toString());
            }
        }
    }

    private void submit(final String trainId, final String deptTime, final String deptDay, final String dest, final String origin, final String destS, final String originS) {
        if (isNetworkAvailable()) {
            showMaterialDialog();
            final FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference myRef = database.getReference(Constants.CONNECT_RAILS);
            DatabaseReference trains = myRef.child(Constants.TRAINS);
            final DatabaseReference trainRef = trains.child(trainId);
            final Train train = new Train();
            train.setDeptDay(deptDay);
            train.setDeptTime(deptTime);
            train.setDestination(dest);
            train.setDestinationS(destS);
            train.setStatus(0);
            train.setOriginS(originS);
            train.setOrigin(origin);
            train.setTrainId(trainId);
            final DatabaseReference trainsReference = myRef.child(Constants.TRAINS);
            trains.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    DataSnapshot snapshot = dataSnapshot.child(trainId);
                    HashMap<String, String> map = (HashMap<String, String>) snapshot.getValue();
                    if (map != null) {
                        String value = map.get(Constants.TRAIN_ID);
                        if (value != null && value.contentEquals(trainId)) {
                            dismissMaterialDialog();
                            Snackbar snackbar = Snackbar.make(getActivity().findViewById(android.R.id.content), "Train already exist and hasn't departed", Snackbar.LENGTH_LONG);
                            snackbar.show();
                        } else {
                            trainRef.setValue(train, new DatabaseReference.CompletionListener() {
                                @Override
                                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                    dismissMaterialDialog();
                                    if (databaseError == null) {
                                        Toast.makeText(getApplicationContext(), "Successful", Toast.LENGTH_LONG).show();
                                        Intent intent = new Intent();
                                        intent.setAction(Constants.TRAIN_ADDED);
                                        LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(intent);
                                        getActivity().finish();
                                    } else {
                                        Snackbar snackbar = Snackbar.make(getActivity().findViewById(android.R.id.content), databaseError.toString(), Snackbar.LENGTH_LONG);
                                        snackbar.show();
                                    }
                                }
                            });
                        }
                    } else {
                        trainRef.setValue(train, new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                dismissMaterialDialog();
                                if (databaseError == null) {
                                    Toast.makeText(getApplicationContext(), "Successful", Toast.LENGTH_LONG).show();
                                    Intent intent = new Intent();
                                    intent.setAction(Constants.TRAIN_ADDED);
                                    LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(intent);
                                    getActivity().finish();
                                } else {
                                    Snackbar snackbar = Snackbar.make(getActivity().findViewById(android.R.id.content), databaseError.toString(), Snackbar.LENGTH_LONG);
                                    snackbar.show();
                                }
                            }
                        });
                    }
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
    private NewTrainActivity getActivity() {
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

}
