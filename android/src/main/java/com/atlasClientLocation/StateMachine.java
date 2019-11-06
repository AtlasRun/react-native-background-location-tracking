package com.atlasClientLocation;

import android.util.Log;


public class StateMachine {

    private static final String TAG = "TESTLOCATIONTRACKING" ;
    LocationService locationService;
    private int currentState;

    StateMachine(LocationService locationService){
        this.locationService = locationService;
        this.currentState = TrackingState.NOT_TRACKING.getValue();
    }

    public void setState(int newState){

        if(this.currentState == TrackingState.NOT_TRACKING.getValue()){
            if(newState == TrackingState.WAITING_FOR_SIGNAL.getValue()){
                Log.d(TAG, "SUCCESSFULLY TRANSITIONED FROM: NOT_TRACKING -> WAITING_FOR_SIGNAL");
                this.onTransition(this.currentState, newState);
                this.currentState = newState;
            }else{
                Log.w(TAG, "illegal transition to"+ newState);
            }
        }else if(this.currentState == TrackingState.WAITING_FOR_SIGNAL.getValue()){
            if(newState == TrackingState.TRACKING_IN_PROGRESS.getValue()){
                Log.d(TAG, "SUCCESSFULLY TRANSITIONED FROM: WAITING_FOR_SIGNAL -> TRACKING_IN_PROGRESS");
                this.currentState = newState;
            }else{
                Log.w(TAG, "illegal transition from "+ this.currentState + " -> " + newState);
            }
        } else if(this.currentState == TrackingState.TRACKING_IN_PROGRESS.getValue()){
            if(newState == TrackingState.NOT_TRACKING.getValue()){
                Log.d(TAG, "SUCCESSFULLY TRANSITIONED FROM: TRACKING_IN_PROGRESS -> NOT_TRACKING");
                this.onTransition(this.currentState, newState);
                this.currentState = newState;
            }else{
                Log.w(TAG, "illegal transition from "+ this.currentState + " -> " + newState);
            }
        }
    }

    private void onTransition(int prevState, int newState){
        if(prevState == TrackingState.NOT_TRACKING.getValue() && newState == TrackingState.WAITING_FOR_SIGNAL.getValue()){
            locationService._startTracking();
        } else if(prevState == TrackingState.TRACKING_IN_PROGRESS.getValue() && newState == TrackingState.NOT_TRACKING.getValue()){
            locationService._stopTracking();
        }
    }

    public int getCurrentState(){
        return this.currentState;
    }

}
