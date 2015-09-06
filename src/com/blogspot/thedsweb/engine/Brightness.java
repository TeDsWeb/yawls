/*******************************************************************************
 * Copyright (c) 2015 Dominik Brämer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.blogspot.thedsweb.engine;

import java.util.concurrent.Semaphore;
import java.util.logging.Level;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.highgui.VideoCapture;
import org.opencv.imgproc.Imgproc;

import com.blogspot.thedsweb.main.Debug;
import com.blogspot.thedsweb.util.Config;
import com.blogspot.thedsweb.util.Files;
import com.blogspot.thedsweb.util.Initialization;

public class Brightness extends Files {
    private int min;
    private int max;
    private int last;
    private int current;
    private final int maxLevel;
    private double[] referenceValue;
    private boolean face;
    private final Threshold threshold;
    private final Face probability;
    private final Thread fade;
    private final Fade transition;
    private boolean runDaemon;
    private boolean firstRun;
    private boolean backlit;
    private final BacklightDevice blDev;
    private final Semaphore semaphoreFade = new Semaphore(0, false);
    private final Semaphore semaphoreSet = new Semaphore(1, false);

    static {
	// Load native library of OpenCV
	System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    public Brightness(Config config) {
	// Create threshold object to calculate threshold
	threshold = new Threshold(config);

	// Create face object to track faces
	probability = new Face(config);

	// Create fade object for brightness changes
	transition = new Fade();

	// Daemon to fade between screen brightness values
	fade = new Thread(new FadeDaemon(), "fadeDaemon");
	fade.setDaemon(true);

	// Initialize Files if they dosen't exist
	Initialization.Files();
	final int[] arr = new int[4];
	readStatus(arr);
	min = arr[0];
	max = arr[1];
	last = arr[2];
	if (arr[3] > 0) {
	    maxLevel = arr[3];
	} else {
	    maxLevel = 15;
	}
	blDev = new BacklightDevice();
	blDev.setRef(min, max);
	referenceValue = blDev.getRef();
	firstRun = true;
	backlit = false;
	current = captureAndCalculate();
    }

    // Start Daemon
    public void start() {
	runDaemon = true;
	fade.start();
    }

    // Cancel Daemon
    public void interrupt() {
	runDaemon = false;
	fade.interrupt();
	try {
	    fade.join();
	} catch (final InterruptedException e) {
	}
    }

    // Return the current Brightness
    public int getCurrent() {
	return current;
    }

    // Get the current Brightness
    public void setCurrent() {
	current = captureAndCalculate();
    }

    // Return the last set Brightness
    public int getLast() {
	return last;
    }

    // Return if Faces are detected
    public boolean getFace() {
	return face;
    }

    // Control the difference between last and
    // current Brightness value return true when
    // it is too high
    private boolean control(int current) {
	return current > last << 1 || current << 1 < last;
    }

    // Return the ideal brightness value
    // Method is based on Java's build in binarySearch
    private int brightnessValue(double[] arr, int key) {
	int low = 0;
	int high = arr.length - 1;
	final int LENGTH = arr.length - 1;

	while (low <= high) {
	    final int mid = (low + high) >>> 1;
	    final double midVal = arr[mid];
	    double aMidVal = 255;

	    if (mid != LENGTH) {
		aMidVal = arr[mid + 1];
	    }

	    if (key > aMidVal) {
		low = mid + 1;
	    } else if (key < midVal) {
		high = mid - 1;
	    } else {
		Debug.LOG.log(Level.CONFIG, "Set " + mid
			+ " as brightness value.");
		return mid;
	    }
	}
	Debug.LOG.log(Level.CONFIG, "Set " + LENGTH + " as brightness value.");
	return LENGTH;
    }

    private Mat capture10thFrame(VideoCapture cap, Mat frame) {
	// Grab and decode the 10th frame of the camera
	for (int i = 0; i <= 10; i++) {
	    cap.grab();
	}
	cap.retrieve(frame);

	return frame;
    }

    private int captureAndCalculate() {
	final Mat frame = new Mat();

	// Initialize video capturing and set a small image size
	final VideoCapture cap = new VideoCapture(0);
	cap.set(3, 160);
	cap.set(4, 120);

	int meanValue = current;
	face = true;

	// Return current as meanValue if camera start fail
	if (!cap.isOpened()) {
	    return meanValue;
	}

	capture10thFrame(cap, frame);

	// Calculate mean value of frame
	meanValue = meanCalculation(frame);

	// If the current value change in a extreme way
	// set it again
	if (control(meanValue)) {
	    capture10thFrame(cap, frame);

	    // Re-Calculate mean value of frame
	    meanValue = meanCalculation(frame);
	}

	// Set true if someone's Face is detected
	face = probability.detectFace(frame, meanValue);

	// Release the camera for other programs
	cap.release();

	return meanValue;
    }

    private int meanCalculation(Mat rgb) {
	// Convert RGB to YcrCb for easier luminance calculation
	final Mat yCrCb = new Mat();
	Imgproc.cvtColor(rgb, yCrCb, Imgproc.COLOR_RGB2YCrCb);

	// Calculate luminance
	final Scalar mainMean = Core.mean(yCrCb);
	int meanLumaValue = (int) mainMean.val[0];

	// Test if backlit conditions are true
	if (!firstRun && backlitDetection(yCrCb, mainMean, meanLumaValue)) {
	    Debug.LOG.log(Level.CONFIG, "Backlit detected.");
	    backlit = true;
	    if (meanLumaValue < current) {
		meanLumaValue = current;
	    }
	} else {
	    backlit = false;
	}

	// Set the first run parameter to false so that the next time the mean
	// calculation method is called the backlit detection is also running
	if (firstRun) {
	    firstRun = false;
	}

	// If the frame is completely black or white the camera must already in
	// use by another program
	if (meanLumaValue == 0) {
	    meanLumaValue = current;
	}

	return meanLumaValue;
    }

    private boolean checkChroma(int[] values, int mean) {
	// Check how similar the chroma values of all frame tiles are. By
	// calculate the maximum number of similar tiles.
	int max = 0;
	for (final int value : values) {
	    int count = 0;
	    for (final int valB : values) {
		if (valB + 5 > value && valB - 5 < value) {
		    count++;
		}
	    }
	    if (max < count) {
		max = count;
	    }
	}
	// Return true if more than the half of the tiles have a similar chroma
	// value.
	return max > 4;
    }

    private boolean backlitDetection(Mat yCrCb, Scalar mainMean,
	    int mainMeanLumaValue) {
	// Save the mean brightness and chroma value of the whole frame. Plus
	// the width and height of the frame.
	final int mainMeanChromaValue = (int) mainMean.val[2];
	final int PARTS = 8;
	final int w = yCrCb.width();
	final int h = yCrCb.height();

	final int wStep = w >> 2;
	final int hStep = h >> 1;

	// Separate the image into 8 equal parts.
	final Mat[] tiles = new Mat[PARTS];
	tiles[0] = yCrCb.submat(0, hStep, 0, wStep);
	tiles[1] = yCrCb.submat(0, hStep, wStep, wStep * 2);
	tiles[2] = yCrCb.submat(hStep, h, 0, wStep);
	tiles[3] = yCrCb.submat(hStep, h, wStep, wStep * 2);
	tiles[4] = yCrCb.submat(0, hStep, wStep * 2, wStep * 3);
	tiles[5] = yCrCb.submat(0, hStep, wStep * 2, w);
	tiles[6] = yCrCb.submat(hStep, h, wStep * 2, wStep * 3);
	tiles[7] = yCrCb.submat(hStep, h, wStep * 2, w);

	// Calculate the mean value of all parts.
	final Scalar[] tileMean = new Scalar[PARTS];
	for (int i = 0; i < tileMean.length; i++) {
	    tileMean[i] = Core.mean(tiles[i]);
	}

	// Save the mean brightness and chroma of all parts.
	final int[] tileMeanLuma = new int[PARTS];
	final int[] tileMeanChroma = new int[PARTS];

	// Save min and max value in container
	MinMaxContainer con = null;

	for (int j = 0; j < tileMean.length; j++) {
	    tileMeanLuma[j] = (int) tileMean[j].val[0];
	    if (j == 0) {
		con = new MinMaxContainer(tileMeanLuma[j]);
	    } else {
		con.add(tileMeanLuma[j]);
	    }
	    tileMeanChroma[j] = (int) tileMean[j].val[2];
	}

	// Get the highest and lowest brightness value of all frame tiles.
	final int min = con.getMin();
	final int max = con.getMax() >> 1;

	// Check if their is a consistent chroma distribution and a brightness
	// spike to detect backlit conditions.
	if (checkChroma(tileMeanChroma, mainMeanChromaValue) && min < max) {
	    return true;
	}

	return false;
    }

    public void setBrightness() {
	// Threshold values to prevent flickering
	final int darkeningThreshold = threshold.getDarkeningThreshold(last);
	final int brighteningThreshold = threshold
		.getBrighteningThreshold(last);

	// Set the display brightness value relative to reference
	if (current <= darkeningThreshold || current > brighteningThreshold) {
	    // Check if the new current value is also the new
	    // minimum or maximum value
	    if (current < min) {
		min = current;
		blDev.setRef(min, max);
		referenceValue = blDev.getRef();
	    }
	    if (current > max) {
		max = current;
		blDev.setRef(min, max);
		referenceValue = blDev.getRef();
	    }

	    // Save last current value
	    last = current;

	    // Set Brightness
	    try {
		semaphoreSet.acquire();
	    } catch (final InterruptedException e) {
	    }
	    transition.setValue(brightnessValue(referenceValue, current));
	    semaphoreFade.release();

	    // Save all values
	    saveValue(current, min, max);
	}
    }

    public void resetBrightnessAfterReboot() {
	// Set Brightness
	blDev.set(brightnessValue(referenceValue, last));
    }

    public int calibrateBrightness() {
	firstRun = false;

	// Get new current value
	setCurrent();

	// Return failure value on backlit
	if (backlit) {
	    return -1;
	}

	// Check if the new current value is also the new
	// minimum or maximum value
	if (current < min) {
	    min = current;
	}
	if (current > max) {
	    max = current;
	}

	// Save all values
	saveValue(current, min, max);

	return current;
    }

    public void darkenBrightness() {
	last = 0;
	try {
	    semaphoreSet.acquire();
	} catch (final InterruptedException e) {
	}
	transition.setValue(0);
	semaphoreFade.release();
    }

    class FadeDaemon implements Runnable {
	@Override
	public void run() {
	    int step = 500 / maxLevel;

	    // Ensure that there is a min. laziness of 1 millisecond
	    if (step == 0) {
		step = 1;
	    }

	    while (runDaemon) {
		try {
		    semaphoreFade.acquire();
		} catch (final InterruptedException e) {
		    return;
		}
		int count = 0;
		// Set dynamically the brightness level
		while (blDev.get() != transition.getValue() && count < maxLevel) {
		    int i = blDev.get();
		    if (i > transition.getValue()) {
			i--;
		    } else {
			i++;
		    }
		    blDev.set(i);
		    count++;
		    try {
			Thread.sleep(step);
		    } catch (final InterruptedException e) {
			return;
		    }
		}
		semaphoreSet.release();
	    }
	}
    }
}
