package com.sakaryamiras.app.model;

import com.google.firebase.firestore.Exclude;

public class Category {

    @Exclude
    private String id;

    private String name;
    private String nameEn;
    private String icon;
    private String colorHex;

    public Category() {
    }

    public Category(String name, String icon, String colorHex) {
        this.name = name;
        this.icon = icon;
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

    public String getNameEn() {
        return nameEn;
    }

    public void setNameEn(String nameEn) {
        this.nameEn = nameEn;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getColorHex() {
        return colorHex;
    }

    public void setColorHex(String colorHex) {
        this.colorHex = colorHex;
    }
}
