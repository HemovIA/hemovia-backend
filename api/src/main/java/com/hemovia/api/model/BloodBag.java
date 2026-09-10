package com.hemovia.api.model;

import java.util.Date;

public class BloodBag {
    private int id;
    private String bloodType, rhFactor, component;
    private Date collectionDate, expirationDate;
    private BloodBagStatus status;

    public enum BloodBagStatus{
        AVAILABLE, 
        RESERVED, 
        IN_TRANSIT, 
        DELIVERED, 
        DISCARDED
    }


}
