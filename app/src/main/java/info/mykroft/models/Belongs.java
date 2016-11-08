package info.mykroft.models;

/**
 * Created by MyKroft on 11/6/2016.
 */

public class Belongs {
    private String trainId;
    private String cargoId;

    public Belongs(){

    }

    public void setTrainId(String trainId) {
        this.trainId = trainId;
    }

    public void setCargoId(String cargoId) {
        this.cargoId = cargoId;
    }

    public String getCargoId() {
        return cargoId;
    }

    public String getTrainId() {
        return trainId;
    }
}
