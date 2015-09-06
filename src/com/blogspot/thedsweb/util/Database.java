/*******************************************************************************
 * Copyright (c) 2015 Dominik Brämer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.blogspot.thedsweb.util;

public class Database {
    // Path for every configuration file
    public final static String PATH_MIN = "/var/lib/yawls/min_value.txt";
    public final static String PATH_MAX = "/var/lib/yawls/max_value.txt";
    public final static String PATH_LAST = "/var/lib/yawls/last_value.txt";
    public final static String PATH_LIB = "/var/lib/yawls";
    public final static String PATH_TO_CONFIG = "/etc/";
    public final static String PATH_TO_CONFIG_FILE = "/etc/yawls.properties";

    // Path for log file and variables
    public final static String LOG_PATTERN = "/var/log/yawls%u.%g.log";
    public final static int LOG_SIZE = 50000;
    public final static int LOG_ROTATE = 1;

    // Path for library file
    public final static String PATH_TO_XML = "/usr/share/yawls/lbpcascade_frontalface.xml";

    // Path for GUI files
    public final static String PATH_ICON = "/usr/share/yawls/yawls_icon_64x64.png";
    public final static String VERSION = "1.2.2";
    public final static String CREDITS = "Xuacu Saturio\nVPablo\nDominik Brämer\nAndi Chandler\nlann\nClaudio Arseni\nJulio Alexander Sieg\nOleg Koptev\nÖzgür Sarıer\nМикола Ткач\nJean-Marc\nGherman Ionuț\n☠Jay ZDLin☠";

    // Path for lock files
    public final static String PATH_PID = "/run/yawls.pid";
    public final static String PATH_LOCK = "/run/yawls.lock";
    public final static String PATH_CRON_LOCK = "/run/yawls_cron.lock";
    public final static String PATH_GUI_LOCK = "/run/yawls_gui.lock";
    public final static String PATH_LOCK_DAEMON = "/run/yawls_calibration.lock";
    public final static String PATH_LOCK_CONFIG = "/run/yawls_configuration.lock";

    // Path for every battery information file needed
    public final static String PATH_CHARGE_NOW = "/sys/class/power_supply/BAT0/charge_now";
    public final static String PATH_CHARGE_FULL = "/sys/class/power_supply/BAT0/charge_full";

    // List of external Links
    public final static String QUESTIONS = "https://answers.launchpad.net/yawls";

    // Configuration file
    public final static String CONFIG_CONTENT = "#face Detect dim the screen if no one is in front of the camera\n"
	    + "#Set true to enable or false to disable face detection\n"
	    + "faceDetect = false\n\n"
	    + "#Minimum time between camera activations in milliseconds\n"
	    + "lowerLimit = 500\n\n"
	    + "#Maximum time between camera activations in milliseconds\n"
	    + "upperLimit = 30000\n\n"
	    + "#Minimum time increase between camera activations in milliseconds if nothing to do\n"
	    + "minIncrease = 100\n\n"
	    + "#Maximum time increase between camera activations in milliseconds if nothing to do\n"
	    + "maxIncrease = 500\n\n"
	    + "#Value for darkening threshold in percent (0-100)\n"
	    + "#For example a value of 25 means that the current brightness\n"
	    + "#has to be less than 75 percent of the last measured brightness.\n"
	    + "darkeningThreshold = 50\n\n"
	    + "#Value for brightening threshold in percent (0-100)\n"
	    + "#For example a value of 10 means that the current brightness\n"
	    + "#has to be greater than 110 percent of the last measured brightness.\n"
	    + "brighteningThreshold = 25\n\n"
	    + "#Log level 0 (error) < 1 (warning) < 2 (info) < 3 (debug)\n"
	    + "logLevel = 1";

    public final static boolean FACE_DETECT = false;
    public final static int LOWER_LIMIT = 500;
    public final static int UPPER_LIMIT = 30000;
    public final static int MIN_INCREASE = 100;
    public final static int MAX_INCREASE = 500;
    public final static int DARKENING_THRESHOLD = 50;
    public final static int BRIGHTENING_THRESHOLD = 25;
    public final static int LOG_LEVEL = 1;

    private Database() {
    }
}
