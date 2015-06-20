/*******************************************************************************
 * Copyright (c) 2013-2015 Dominik Brämer.
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
    private final int MAXLEVEL;
    private double[] referenceValue;
    private final String PATH;
    private boolean face;
    private final Threshold THRESHOLD;
    private final Face PROBABILITY;
    private final Thread fade;
    private final Fade TRANSITION;
    private boolean runDaemon;
    private boolean firstRun;
    private boolean backlit;
    private final Semaphore semaphoreFade = new Semaphore(0, false);
    private final Semaphore semaphoreSet = new Semaphore(1, false);

    static {
	// Load native library of OpenCV
	System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    public Brightness(Config config) {
	// Create threshold object to calculate threshold
	THRESHOLD = new Threshold(config);

	// Create face object to track faces
	PROBABILITY = new Face(config);

	// Create fade object for brightness changes
	TRANSITION = new Fade();

	// Daemon to fade between screen brightness values
	fade = new Thread(new FadeDaemon(), "fadeDaemon");
	fade.setDaemon(true);

	// Initialize Files if they dosen't exist
	Initialization.Files();
	final int[] arr = new int[4];
	PATH = readStatus(arr);
	min = arr[0];
	max = arr[1];
	last = arr[2];
	MAXLEVEL = arr[3];
	setReference();
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
    public boolean control() {
	return current > last << 1 || current << 1 < last;
    }

    private void setReference() {
	// Reference mean values
	final double referenceStep = (double) (max ^ min) / (MAXLEVEL + 1);
	final double[] referenceValue = new double[MAXLEVEL + 1];
	referenceValue[MAXLEVEL] = max - referenceStep;

	for (int i = MAXLEVEL; i > 1; i--) {
	    if (referenceValue[i] - referenceStep < 0) {
		referenceValue[i - 1] = 0;
	    } else {
		referenceValue[i - 1] = referenceValue[i] - referenceStep;
	    }
	}

	this.referenceValue = referenceValue;
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

	// Grab and decode the 10th frame of the camera
	for (int i = 0; i <= 10; i++) {
	    cap.grab();
	}
	cap.retrieve(frame);

	// Calculate mean value of frame
	meanValue = meanCalculation(frame);

	// Set true if someone's Face is detected
	face = PROBABILITY.detectFace(frame, meanValue);

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

    private int maxVal(int[] values) {
	int max = 0;
	for (final int value : values) {
	    if (max < value) {
		max = value;
	    }
	}
	return max;
    }

    private int minVal(int[] values) {
	int min = 260;
	for (final int value : values) {
	    if (min > value) {
		min = value;
	    }
	}
	return min;
    }

    private boolean checkChroma(int[] values, int mean) {
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
	return max > 4;
    }

    private boolean backlitDetection(Mat yCrCb, Scalar mainMean,
	    int mainMeanLumaValue) {
	final int mainMeanChromaValue = (int) mainMean.val[2];
	final int w = yCrCb.width();
	final int h = yCrCb.height();

	final int wStep = w >> 2;
	final int hStep = h >> 1;

	final Mat[] tiles = new Mat[8];
	tiles[0] = yCrCb.submat(0, hStep, 0, wStep);
	tiles[1] = yCrCb.submat(0, hStep, wStep, wStep * 2);
	tiles[2] = yCrCb.submat(hStep, h, 0, wStep);
	tiles[3] = yCrCb.submat(hStep, h, wStep, wStep * 2);
	tiles[4] = yCrCb.submat(0, hStep, wStep * 2, wStep * 3);
	tiles[5] = yCrCb.submat(0, hStep, wStep * 2, w);
	tiles[6] = yCrCb.submat(hStep, h, wStep * 2, wStep * 3);
	tiles[7] = yCrCb.submat(hStep, h, wStep * 2, w);

	final Scalar[] tileMean = new Scalar[8];
	for (int i = 0; i < tileMean.length; i++) {
	    tileMean[i] = Core.mean(tiles[i]);
	}

	final int[] tileMeanLuma = new int[8];
	final int[] tileMeanChroma = new int[8];
	for (int j = 0; j < tileMean.length; j++) {
	    tileMeanLuma[j] = (int) tileMean[j].val[0];
	    tileMeanChroma[j] = (int) tileMean[j].val[2];
	}

	final int min = minVal(tileMeanLuma);
	final int max = maxVal(tileMeanLuma) >> 1;

	if (checkChroma(tileMeanChroma, mainMeanChromaValue) && min < max) {
	    return true;
	}

	return false;
    }

    public void setBrightness() {
	// Threshold values to prevent flickering
	final int darkeningThreshold = THRESHOLD.getDarkeningThreshold(last);
	final int brighteningThreshold = THRESHOLD
		.getBrighteningThreshold(last);

	// Set the display brightness value relative to reference
	if (current <= darkeningThreshold || current > brighteningThreshold) {
	    // Check if the new current value is also the new
	    // minimum or maximum value
	    if (current < min) {
		min = current;
		setReference();
	    }
	    if (current > max) {
		max = current;
		setReference();
	    }

	    // Save last current value
	    last = current;

	    // Set Brightness
	    try {
		semaphoreSet.acquire();
	    } catch (final InterruptedException e) {
	    }
	    TRANSITION.setValue(brightnessValue(referenceValue, current));
	    semaphoreFade.release();

	    // Save all values
	    saveValue(current, min, max);
	}
    }

    public void resetBrightnessAfterReboot() {
	// Set Brightness
	writeInt(brightnessValue(referenceValue, last), PATH);
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
	TRANSITION.setValue(0);
	semaphoreFade.release();
    }

    class FadeDaemon implements Runnable {
	@Override
	public void run() {
	    final int step = 500 / MAXLEVEL;
	    while (runDaemon) {
		try {
		    semaphoreFade.acquire();
		} catch (final InterruptedException e) {
		    return;
		}
		int count = 0;
		while (readInt(PATH) != TRANSITION.getValue()
			&& count < MAXLEVEL) {
		    int i = readInt(PATH);
		    if (i > TRANSITION.getValue()) {
			i--;
		    } else {
			i++;
		    }
		    writeInt(i, PATH);
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
