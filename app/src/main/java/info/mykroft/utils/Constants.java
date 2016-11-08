package info.mykroft.utils;

import com.google.android.gms.maps.model.LatLng;

public abstract class Constants {
    public static final String CARGO = "cargo";
    public static final String TRACK = "track";
    public static final String LOCATION = "location";
    public static final String CONNECT_RAILS = "connectRails";
    public static final String USER = "user";
    public static final String TRACKING_ID = "trackingId";
    public static final String TRAINS = "trains";
    public static final String TRAIN_ADDED = "trainAdded";
    public static final String TRAIN_ID = "trainId";
    public static final String BELONGS = "belongs";
    public static final String RETURN_TO = "returnTo";
    public static final String LOG_TRAIN = "logTrain";
    public static final String SHARED_PREFERENCES_NAME = "pref";
    public static final String GEOFENCES_ADDED_KEY = "geofencesAddedKey";
    public static final long GEOFENCE_EXPIRATION_IN_HOURS = 72;
    public static final long GEOFENCE_EXPIRATION_IN_MILLISECONDS =
            GEOFENCE_EXPIRATION_IN_HOURS * 60 * 60 * 1000;
    public static final float GEOFENCE_RADIUS_IN_METERS = 1609; // 1 mile, 1.6 km
    public static final String CURRENT_LOCATION = "currentLocation";
    public static final int LAT_LNG = 401;
    public static final String  ORIGIN = "origin";
    public static final String DESTINATION = "destination";
    public static final String STATUS = "status";
    public static final String VIEW_REPORT = "viewReport";
    public static final String FROM = "from";
    public static final String TRAINS_ADAPTER = "trainsAdapter";
}
