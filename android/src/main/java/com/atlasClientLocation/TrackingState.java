package com.atlasClientLocation;

public enum TrackingState {
    NOT_TRACKING(0),
    WAITING_TO_START( 1),
    WAITING_TO_STOP( 2),
    WAITING_FOR_SIGNAL( 3),
    TRACKING_IN_PROGRESS( 4);

    private final int trackingValue;

    TrackingState(int trackingValue){
        this.trackingValue = trackingValue;
    }

    public int getValue(){
        return trackingValue;
    }
}