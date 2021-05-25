package com.example.sociochat;

import android.widget.Button;

public class Contacts
{
    String name,Status,image;


    public Contacts()
    {

    }


    public Contacts(String name, String status, String image)
    {
        this.name = name;
        this.Status = status;
        this.image = image;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStatus() {
        return Status;
    }

    public void setStatus(String status) {
        this.Status = status;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}