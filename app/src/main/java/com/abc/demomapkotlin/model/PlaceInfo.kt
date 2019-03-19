package com.abc.demomapkotlin.model

import android.net.Uri

import com.google.android.gms.maps.model.LatLng

class PlaceInfo {

    var name: String? = null
    var address: String? = null
    var phoneNumber: String? = null
    var id: String? = null
    var websiteUri: Uri? = null
    var latLng: LatLng? = null
    var rating: Float = 0.toFloat()
    var attributions: String? = null

    constructor(
        name: String, address: String, phoneNumber: String,
        id: String, websiteUri: Uri, latLng: LatLng, rating: Float, attributions: String
    ) {
        this.name = name
        this.address = address
        this.phoneNumber = phoneNumber
        this.id = id
        this.websiteUri = websiteUri
        this.latLng = latLng
        this.rating = rating
        this.attributions = attributions
    }

    constructor()

    override fun toString(): String {
        return "PlaceInfo{" +
                "name='" + name + '\''.toString() +
                ", address='" + address + '\''.toString() +
                ", phoneNumber='" + phoneNumber + '\''.toString() +
                ", id='" + id + '\''.toString() +
                ", websiteUri=" + websiteUri +
                ", latLng=" + latLng +
                ", rating=" + rating +
                ", attributions='" + attributions + '\''.toString() +
                '}'.toString()
    }
}