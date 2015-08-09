package com.blogspot.thedsweb.engine;

public interface Backlight {

    /*
     * Indicates whether the method works return true (should work) or false
     * (not working).
     */
    public boolean check();

    /*
     * Build a double Array to link reference luminances with a brightness
     * level. Where the index of the array represent the actual brightness level
     * and the values at the index and the following index represents a value
     * range of luminosity. An example can be found in the Xbrightness class.
     */
    public void setRef(int min, int max);

    // Return the previously build double array.
    public double[] getRef();

    /*
     * Defined how to set the brightness value. Where the integer value
     * represent one of the indexes of the previously build double array.
     */
    public void set(int value);

    // Return the current brightness level of the backlight device.
    public int get();
}
