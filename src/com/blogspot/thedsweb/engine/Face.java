/*******************************************************************************
 * Copyright (c) 2015 Dominik Brämer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.blogspot.thedsweb.engine;

import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.objdetect.CascadeClassifier;

import com.blogspot.thedsweb.util.Config;
import com.blogspot.thedsweb.util.Database;

public class Face {
    private boolean faceNow;
    private boolean faceBefore;
    private final boolean faceDetect;
    private CascadeClassifier faceDetector;

    public Face(Config config) {
	// Initialize face values
	faceDetect = config.faceDetect();
	if (faceDetect) {
	    faceNow = false;
	    faceBefore = false;
	    faceDetector = new CascadeClassifier(Database.PATH_TO_XML);
	}
    }

    public boolean detectFace(Mat frame, int meanValue) {
	// Return true if it is to dark to track faces correctly
	// or return true if face detection is off
	if (meanValue < 50 || !faceDetect) {
	    return true;
	}

	// Save old face value and get new one
	swapFace(frame);

	// Return true as long as faceNow or faceBefore are true
	return faceNow || faceBefore;
    }

    private void swapFace(Mat frame) {
	// Save faceNow value in faceBefore
	faceBefore = faceNow;

	// Set faceNow new
	faceNow = detect(frame);
    }

    private boolean detect(Mat frame) {
	// Detect Faces on Frame and return true if there are Faces and return
	// false if not
	final MatOfRect faceDetections = new MatOfRect();
	faceDetector.detectMultiScale(frame, faceDetections);
	final boolean faces = faceDetections.toArray().length != 0;

	return faces;
    }
}
