package com.allgsafe.gsafe;

public class LinkModel {

    String UploadTime, uid, url;

    public LinkModel() {
    }

    public LinkModel(String uploadTime, String uid, String url) {
        UploadTime = uploadTime;
        this.uid = uid;
        this.url = url;
    }

    public String getUploadTime() {
        return UploadTime;
    }

    public void setUploadTime(String uploadTime) {
        UploadTime = uploadTime;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
