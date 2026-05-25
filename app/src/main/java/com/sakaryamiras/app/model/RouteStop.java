package com.sakaryamiras.app.model;

import com.google.firebase.firestore.Exclude;

public class RouteStop {

    @Exclude
    private String id;

    private String locationId;
    private Integer orderIndex;
    private Integer timeAtStop;
    private String note;

    public RouteStop() {
    }

    public RouteStop(String locationId, Integer orderIndex, Integer timeAtStop, String note) {
        this.locationId = locationId;
        this.orderIndex = orderIndex;
        this.timeAtStop = timeAtStop;
        this.note = note;
    }

    @Exclude
    public String getId() {
        return id;
    }

    @Exclude
    public void setId(String id) {
        this.id = id;
    }

    public String getLocationId() {
        return locationId;
    }

    public void setLocationId(String locationId) {
        this.locationId = locationId;
    }

    public Integer getOrderIndex() {
        return orderIndex;
    }

    public void setOrderIndex(Integer orderIndex) {
        this.orderIndex = orderIndex;
    }

    public Integer getTimeAtStop() {
        return timeAtStop;
    }

    public void setTimeAtStop(Integer timeAtStop) {
        this.timeAtStop = timeAtStop;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
