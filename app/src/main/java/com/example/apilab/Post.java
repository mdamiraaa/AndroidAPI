package com.example.apilab;

import com.google.gson.annotations.SerializedName;

public class Post {
    @SerializedName("userId")
    private int userId;
    private int id;
    private String title;
    @SerializedName("body")
    private String bjshdjkfhjksdhfodu;

    public String getBjshdjkfhjksdhfodu() {
        return bjshdjkfhjksdhfodu;
    }

    public String getTitle() {
        return title;
    }
}
