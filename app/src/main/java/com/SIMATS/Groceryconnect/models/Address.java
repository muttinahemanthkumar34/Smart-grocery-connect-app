package com.SIMATS.Groceryconnect.models;

/**
 * Model class representing a user address.
 */
public class Address {
    private int addressId;
    private int userId;
    private String label;
    private String addressLine;
    private String city;
    private String pincode;
    private boolean isDefault;

    public Address() {
    }

    public Address(int addressId, int userId, String label, String addressLine, String city, String pincode, boolean isDefault) {
        this.addressId = addressId;
        this.userId = userId;
        this.label = label;
        this.addressLine = addressLine;
        this.city = city;
        this.pincode = pincode;
        this.isDefault = isDefault;
    }

    // Getters and Setters
    public int getAddressId() {
        return addressId;
    }

    public void setAddressId(int addressId) {
        this.addressId = addressId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getAddressLine() {
        return addressLine;
    }

    public void setAddressLine(String addressLine) {
        this.addressLine = addressLine;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getPincode() {
        return pincode;
    }

    public void setPincode(String pincode) {
        this.pincode = pincode;
    }

    public boolean isDefault() {
        return isDefault;
    }

    public void setDefault(boolean isDefault) {
        this.isDefault = isDefault;
    }
}
