/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.neu.javaserver;

/**
 *
 * @author igortn
 */
public final class StepData {
    private final String userID;
    private final int day;
    private final int hour;
    private final int stepCount;
    
    
    /*
    * Constructior 
    */
    public StepData ( String user, String day, String hour, String stepCount)  throws IllegalArgumentException {
        
        //TODO add preconsition checks
    
        this.userID = user;
        this.day = Integer.parseInt(day);
        this.hour = Integer.parseInt(hour);
        this.stepCount = Integer.parseInt(stepCount);
    }

    /**
     * @return the User
     */

    /**
     *
     * @return the user
     */
    public String getUser() {
        return userID;
    }

    /**
     * @return the day
     */
    public int getDay() {
        return day;
    }

    /**
     * @return the hour
     */
    public int getHour() {
        return hour;
    }

    /**
     * @return the stepCount
     */
    public int getStepCount() {
        return stepCount;
    }
    
}
