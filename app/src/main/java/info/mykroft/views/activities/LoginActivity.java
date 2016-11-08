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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.util.HashMap;

import info.mykroft.R;
import info.mykroft.utils.Constants;

public class LoginActivity extends AppCompatActivity {
    private MaterialEditText username;
    private MaterialEditText password;
    private Button login;
    private MaterialDialog materialDialog;
    private String returnTo = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        if (intent != null) returnTo = intent.getStringExtra(Constants.RETURN_TO);
        setContentView(R.layout.activity_login);
        setupToolbar();
        username = (MaterialEditText) findViewById(R.id.username);
        password = (MaterialEditText) findViewById(R.id.password);
        login = (Button) findViewById(R.id.login);
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                login(username.getText().toString(), password.getText().toString());
            }
        });
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

    private boolean isNetworkAvailable() {
        ConnectivityManager cm =
                (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
        return isConnected;
    }

    private void login(final String username, final String password) {
        if (isNetworkAvailable()) {
            showMaterialDialog();
            final FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference myRef = database.getReference(Constants.CONNECT_RAILS);
            DatabaseReference users = myRef.child("users");
            users.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    dismissMaterialDialog();
                    DataSnapshot snapshot = dataSnapshot.child(username);
                    HashMap<String, String> map = (HashMap<String, String>) snapshot.getValue();
                    if (map != null) {
                        String value = map.get("password");
                        if (value != null && value.contentEquals(password)) {
                            // login successful
                            if (returnTo != null && returnTo.contentEquals(Constants.LOG_TRAIN)) {
                                Intent intent = new Intent(getActivity(), LogTrainActivity.class);
                                intent.putExtra(Constants.USER, username);
                                getActivity().startActivity(intent);
                                getActivity().finish();
                            } else if (returnTo != null && returnTo.contentEquals(Constants.VIEW_REPORT)) {
                                Intent intent = new Intent(getActivity(), DocumentationActivity.class);
                                intent.putExtra(Constants.USER, username);
                                getActivity().startActivity(intent);
                                getActivity().finish();
                            } else {
                                Intent intent = new Intent(getActivity(), TrainsActivity.class);
                                intent.putExtra(Constants.USER, username);
                                getActivity().startActivity(intent);
                                getActivity().finish();
                            }
                        } else {
                            //login failed
                            Snackbar snackbar = Snackbar.make(getActivity().findViewById(android.R.id.content), "Invalid username or Password", Snackbar.LENGTH_LONG);
                            snackbar.show();
                        }
                    } else {
                        //login failed
                        Snackbar snackbar = Snackbar.make(getActivity().findViewById(android.R.id.content), "Invalid username or Password", Snackbar.LENGTH_LONG);
                        snackbar.show();
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

    private LoginActivity getActivity() {
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
