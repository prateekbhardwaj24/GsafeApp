package com.allgsafe.gsafe;

public class contactModel {

    String Contact_Name, Mobile_Number, cId;

    public contactModel() {
    }

    public contactModel(String contact_Name, String mobile_Number, String cId) {
        Contact_Name = contact_Name;
        Mobile_Number = mobile_Number;
        this.cId = cId;
    }

    public String getContact_Name() {
        return Contact_Name;
    }

    public void setContact_Name(String contact_Name) {
        Contact_Name = contact_Name;
    }

    public String getMobile_Number() {
        return Mobile_Number;
    }

    public void setMobile_Number(String mobile_Number) {
        Mobile_Number = mobile_Number;
    }

    public String getcId() {
        return cId;
    }

    public void setcId(String cId) {
        this.cId = cId;
    }
}
