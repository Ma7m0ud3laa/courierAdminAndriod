package com.kadabra.agent.model;

import com.akexorcist.googledirection.model.Leg;
import com.google.android.gms.maps.model.Polyline;

public class NewPolylineData {

    private Polyline polyline;
    private Leg leg;
    

    public NewPolylineData(Polyline polyline, Leg leg) {
        this.polyline = polyline;
        this.leg = leg;
    }

    
    

    public Polyline getPolyline() {
        return polyline;
    }

    public void setPolyline(Polyline polyline) {
        this.polyline = polyline;
    }

    public Leg getLeg() {
        return leg;
    }

    public void setLeg(Leg leg) {
        this.leg = leg;
    }

    @Override
    public String toString() {
        return "PolylineData{" +
                "polyline=" + polyline +
                ", leg=" + leg +
                '}';
    }
}