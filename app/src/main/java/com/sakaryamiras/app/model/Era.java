package com.sakaryamiras.app.model;

import com.google.firebase.firestore.Exclude;

public class Era {

    @Exclude
    private String id;

    private String name;
    private Integer startYear;
    private Integer endYear;
    private String colorHex;

    public Era() {
    }

    public Era(String name, Integer startYear, Integer endYear, String colorHex) {
        this.name = name;
        this.startYear = startYear;
        this.endYear = endYear;
        this.colorHex = colorHex;
    }

    @Exclude
    public String getId() {
        return id;
    }

    @Exclude
    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getStartYear() {
        return startYear;
    }

    public void setStartYear(Integer startYear) {
        this.startYear = startYear;
    }

    public Integer getEndYear() {
        return endYear;
    }

    public void setEndYear(Integer endYear) {
        this.endYear = endYear;
    }

    public String getColorHex() {
        return colorHex;
    }

    public void setColorHex(String colorHex) {
        this.colorHex = colorHex;
    }
}
