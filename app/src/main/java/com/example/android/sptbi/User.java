package com.example.android.sptbi;

/**
 * Created by aashayjain611 on 26/12/17.
 */

public class User
{
    private String uid,emailId;

    public User(String uid, String emailId) {
        this.uid = uid;
        this.emailId = emailId;
    }

    public String getUid() {
        return uid;
    }

    public String getEmailId() {
        return emailId;
    }
}
