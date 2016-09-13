package com.ffinder.android.models;

/**
 * Created by SiongLeng on 8/9/2016.
 */
public class LanguageModel {

    private String name;
    private String abbr;
    private boolean selected;

    public LanguageModel(String name, String abbr) {
        this.name = name;
        this.abbr = abbr;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAbbr() {
        return abbr;
    }

    public void setAbbr(String abbr) {
        this.abbr = abbr;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }
}
