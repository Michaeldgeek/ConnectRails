package info.mykroft.views.activities;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import info.mykroft.R;
import info.mykroft.utils.Constants;

public class CargoSuccessActivity extends AppCompatActivity {
    private Button rtnHome;
    private TextView trackingId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        if (intent == null) {
            startActivity(new Intent(this, LoginActivity.class));
            this.finish();
        } else {
            final String uniqueId = getIntent().getStringExtra(Constants.TRACKING_ID);
            if (uniqueId == null) {
                startActivity(new Intent(this, LoginActivity.class));
                this.finish();
            } else {
                setContentView(R.layout.activity_cargo_success);
                getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.colorPrimary)));
                trackingId = (TextView)findViewById(R.id.tracking_id);
                rtnHome = (Button)findViewById(R.id.rtn_home);
                trackingId.setText("Tracking id: " + uniqueId);
                rtnHome.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        getActivity().startActivity(new Intent(getActivity(),HomeActivity.class));
                        getActivity().finish();
                    }
                });
            }
        }
    }

    private CargoSuccessActivity getActivity() {
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


}
