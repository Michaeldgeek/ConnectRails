package info.mykroft.models;

import android.support.annotation.Nullable;

import java.util.ArrayList;

/**
 * Created by MyKroft on 11/1/2016.
 */

public class Train {
    private String trainId;
    private String deptTime;
    private String deptDay;
    private String destination;
    private String origin;
    private String destinationS;
    private String originS;
    private Integer status = 0;
    @Nullable
    private CurrentLocation curLoc = null;
    @Nullable
    private ArrayList<Cargo> cargos = new ArrayList<>();

    public Train() {

    }

    @Nullable
    public CurrentLocation getCurLoc() {
        return curLoc;
    }

    public void setCurLoc(@Nullable CurrentLocation curLoc) {
        this.curLoc = curLoc;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getDestinationS() {
        return destinationS;
    }

    public void setDestinationS(String destinationS) {
        this.destinationS = destinationS;
    }

    public String getOriginS() {
        return originS;
    }

    public void setOriginS(String originS) {
        this.originS = originS;
    }

    public String getDeptDay() {
        return deptDay;
    }

    public void setDeptDay(String deptDay) {
        this.deptDay = deptDay;
    }

    public String getDeptTime() {
        return deptTime;
    }

    public void setDeptTime(String deptTime) {
        this.deptTime = deptTime;
    }

    public String getTrainId() {
        return trainId;
    }

    public void setTrainId(String trainId) {
        this.trainId = trainId;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    @Nullable
    public ArrayList<Cargo> getCargos() {
        return cargos;
    }

    public void setCargos(@Nullable ArrayList<Cargo> cargos) {
        this.cargos = cargos;
    }

}
