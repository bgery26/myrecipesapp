package com.example.myapplication;

public class RecipeItem {
    private String id;
    private String name;
    private String info;
    private String time;
    private int imageResource;

    public RecipeItem() {

    }

    public RecipeItem(String name, String info, String time, int imageResource) {
        this.name = name;
        this.info = info;
        this.time = time;
        this.imageResource = imageResource;

    }

    public String getName() {return name;}
    public String getInfo() {return info;}
    public String getTime() {return time;}
    public int getImageResource() {return imageResource;}

    public String _getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

}
