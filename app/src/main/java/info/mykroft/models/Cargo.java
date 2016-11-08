package info.mykroft.models;

/**
 * Created by MyKroft on 11/1/2016.
 */

public class Cargo {
    private String uniqueId;
    private String customerName;
    private String goodsType;
    private String qty;
    private String monitoringOfficer;

    public Cargo() {

    }

    public String getMonitoringOfficer() {
        return monitoringOfficer;
    }

    public void setMonitoringOfficer(String monitoringOfficer) {
        this.monitoringOfficer = monitoringOfficer;
    }

    public String getCustomerName() {
        return customerName;
    }

    public String getGoodsType() {
        return goodsType;
    }

    public String getQty() {
        return qty;
    }

    public String getUniqueId() {
        return uniqueId;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public void setGoodsType(String goodsType) {
        this.goodsType = goodsType;
    }

    public void setQty(String qty) {
        this.qty = qty;
    }

    public void setUniqueId(String uniqueId) {
        this.uniqueId = uniqueId;
    }
}
