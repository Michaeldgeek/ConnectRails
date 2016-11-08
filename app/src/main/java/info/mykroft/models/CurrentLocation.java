package info.mykroft.models;

/**
 * Created by MyKroft on 11/6/2016.
 */

public class CurrentLocation {
    private Double lat;
    private Double lng;
    private String trainId;

    public CurrentLocation(){

    }

    public Double getLat() {
        return lat;
    }

    public String getTrainId() {
        return trainId;
    }

    public void setTrainId(String trainId) {
        this.trainId = trainId;
    }

    public Double getLng() {
        return lng;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    public void setLng(Double lng) {
        this.lng = lng;
    }
}
