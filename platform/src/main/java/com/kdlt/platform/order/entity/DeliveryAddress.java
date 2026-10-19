package com.kdlt.platform.order.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.math.BigDecimal;

// Adresse figée au moment de la commande : elle ne change pas si le client modifie ses informations ensuite.
@Embeddable
public class DeliveryAddress {

    @Column(name = "delivery_phone", length = 30)
    private String phone;

    @Column(name = "delivery_address", length = 300)
    private String address;

    @Column(name = "delivery_city", length = 100)
    private String city;

    @Column(name = "delivery_notes", length = 500)
    private String notes;

    @Column(name = "delivery_latitude", precision = 9, scale = 6)
    private BigDecimal latitude;

    @Column(name = "delivery_longitude", precision = 9, scale = 6)
    private BigDecimal longitude;

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public BigDecimal getLatitude() { return latitude; }
    public void setLatitude(BigDecimal latitude) { this.latitude = latitude; }
    public BigDecimal getLongitude() { return longitude; }
    public void setLongitude(BigDecimal longitude) { this.longitude = longitude; }
}