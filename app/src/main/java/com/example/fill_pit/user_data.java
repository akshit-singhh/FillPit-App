package com.example.fill_pit;

public class user_data {
    private String fullName;
    private String userId;
    private String phone;
    private String email;
    private String address;
    private String city;
    private String state;
    private String postalCode;
    private String municipal;
    private String registrationDate;

    // Required no-argument constructor for Firebase
    public user_data() {
    }

    public user_data(String fullName, String userId, String phone, String email,
                     String address, String city, String state, String postalCode,
                     String municipal, String registrationDate) {
        this.fullName = fullName;
        this.userId = userId;
        this.phone = phone;
        this.email = email;
        this.address = address;
        this.city = city;
        this.state = state;
        this.postalCode = postalCode;
        this.municipal = municipal;
        this.registrationDate = registrationDate;
    }

    // Getters
    public String getFullName() { return fullName; }
    public String getUserId() { return userId; }
    public String getPhone() { return phone; }
    public String getEmail() { return email; }
    public String getAddress() { return address; }
    public String getCity() { return city; }
    public String getState() { return state; }
    public String getPostalCode() { return postalCode; }
    public String getMunicipal() { return municipal; }
    public String getRegistrationDate() { return registrationDate; }

    // Setters
    public void setFullName(String fullName) { this.fullName = fullName; }
    public void setUserId(String userId) { this.userId = userId; }
    public void setPhone(String phone) { this.phone = phone; }
    public void setEmail(String email) { this.email = email; }
    public void setAddress(String address) { this.address = address; }
    public void setCity(String city) { this.city = city; }
    public void setState(String state) { this.state = state; }
    public void setPostalCode(String postalCode) { this.postalCode = postalCode; }
    public void setMunicipal(String municipal) { this.municipal = municipal; }
    public void setRegistrationDate(String registrationDate) { this.registrationDate = registrationDate; }
}
