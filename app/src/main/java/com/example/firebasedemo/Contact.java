package com.example.firebasedemo;

public class Contact {
    private String id;
    private String name;
    private String email;
    private String company;
    private String address;
    private String photoUrl;

    public Contact(String id, String name, String email, String company, String address, String photoUrl) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.company = company;
        this.address = address;
        this.photoUrl = photoUrl;
    }
    public Contact(String name, String email, String company, String address) {
        this.name = name;
        this.email = email;
        this.company = company;
        this.address = address;
    }

    public Contact() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }
}
