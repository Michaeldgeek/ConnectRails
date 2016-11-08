package info.mykroft.views.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import info.mykroft.R;
import info.mykroft.utils.Constants;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {
    private Button floatingActionButton;
    private LatLng latlng;
    private double lat;
    private double lng;
    private String from;
    private Button more;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        more = (Button) findViewById(R.id.more);
        Intent intent = getIntent();
        if (intent != null) {
            from = intent.getStringExtra(Constants.FROM);
            if (from != null && from.contentEquals(Constants.TRAINS_ADAPTER)) {
                final String trainId = intent.getStringExtra(Constants.TRAIN_ID);
                more.setVisibility(View.VISIBLE);
                more.setText("View Cargoes on Train");
                more.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent1 = new Intent(MapsActivity.this, CargoesOnTrainActivity.class);
                        intent1.putExtra(Constants.TRAIN_ID, trainId);
                        MapsActivity.this.startActivity(intent1);
                    }
                });
            } else {
                more.setVisibility(View.GONE);
            }
        }
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        latlng = getIntent().getParcelableExtra("loc");
        if (latlng != null) mapFragment.getMapAsync(this);
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        if (googleMap != null) {
            BitmapDescriptor defaultMarker =
                    BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE);

            Marker marker = googleMap.addMarker(new MarkerOptions()
                    .position(latlng)
                    .title("Location of train")
                    .icon(defaultMarker));
            marker.showInfoWindow();
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latlng, 18));

        }
    }


}
