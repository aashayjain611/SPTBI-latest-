package com.example.android.sptbi;

/**
 * Created by aashayjain611 on 08/10/17.
 */

public class Incubator
{
    private String Name,Email,Founder,Contact,Image,Joined;

    public Incubator() {
    }

    public Incubator(String name, String email, String founder, String contact, String image, String joined) {
        Name = name;
        Email = email;
        Founder = founder;
        Contact = contact;
        Image = image;
        Joined=joined;
    }

    public String getName()
    {
        return Name;
    }
    public String getEmail()
    {
        return Email;
    }
    public String getFounder()
    {
        return Founder;
    }
    public String getContact()
    {
        return Contact;
    }
    public String getImage()
    {
        return Image;
    }
    public String getJoined()
    {
        return Joined;
    }
    public void setName(String name)
    {
        Name=name;
    }
    public void setEmail(String email)
    {
        Email=email;
    }
    public void setFounder(String founder)
    {
        Founder=founder;
    }
    public void setJoined(String joined)
    {
        Joined=joined;
    }
    public void setContact(String contact)
    {
        Contact=contact;
    }
    public void setImage(String image)
    {
        Image=image;
    }
}
