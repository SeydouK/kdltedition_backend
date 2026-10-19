package com.kdlt.platform.order.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public class PlaceOrderRequest {

    @NotBlank(message = "Le téléphone de livraison est obligatoire.")
    @Pattern(regexp = "^[+0-9 ().-]{8,30}$", message = "Le numéro de téléphone n'est pas valide.")
    private String deliveryPhone;

    @NotBlank(message = "L'adresse de livraison est obligatoire.")
    @Size(max = 300, message = "L'adresse est trop longue (300 caractères maximum).")
    private String deliveryAddress;

    @NotBlank(message = "La ville ou la commune est obligatoire.")
    @Size(max = 100, message = "La ville est trop longue (100 caractères maximum).")
    private String deliveryCity;

    @Size(max = 500, message = "Les précisions sont trop longues (500 caractères maximum).")
    private String deliveryNotes;

    @DecimalMin(value = "-90.0", message = "Latitude invalide.")
    @DecimalMax(value = "90.0", message = "Latitude invalide.")
    private BigDecimal latitude;

    @DecimalMin(value = "-180.0", message = "Longitude invalide.")
    @DecimalMax(value = "180.0", message = "Longitude invalide.")
    private BigDecimal longitude;

    public String getDeliveryPhone() { return deliveryPhone; }
    public void setDeliveryPhone(String deliveryPhone) { this.deliveryPhone = deliveryPhone; }
    public String getDeliveryAddress() { return deliveryAddress; }
    public void setDeliveryAddress(String deliveryAddress) { this.deliveryAddress = deliveryAddress; }
    public String getDeliveryCity() { return deliveryCity; }
    public void setDeliveryCity(String deliveryCity) { this.deliveryCity = deliveryCity; }
    public String getDeliveryNotes() { return deliveryNotes; }
    public void setDeliveryNotes(String deliveryNotes) { this.deliveryNotes = deliveryNotes; }
    public BigDecimal getLatitude() { return latitude; }
    public void setLatitude(BigDecimal latitude) { this.latitude = latitude; }
    public BigDecimal getLongitude() { return longitude; }
    public void setLongitude(BigDecimal longitude) { this.longitude = longitude; }
}