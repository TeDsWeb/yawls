## Yawls (Yet Another Webcam Light Sensor):
Adjust the brightness level of your display by using the internal/external camera of your Notebook as an ambient light sensor and is designed to combine comfort and energy saving. Your privacy is a high good and therefore all images are processed internally of Yawls. So that no images will be saved on your drive.

If you install Yawls it runs as a system daemon in background in an interval of 30 seconds per default.
Therefore other programs are also able  to use the camera. The interval is divided into four configurable
variables which will be explained later.

If you have Yawls fresh installed [please enable the universe packages in Ubuntu to avoid an dependency errors] is it handy 
to calibrate it because otherwise it may not function properly but Yawls is also able to calibrate itself over time.
There are two ways to calibrate Yawls:

* via CLI: run once in a dark and once in a bright room [yawls -c] 
* via GUI: there is a calibration wizard under preferences

Since version 1.2.0 Yawls is also able to detect backlit conditions to avoid a low screen brightness in such situations.
A good example for such a situation is if you sit with your back to a great window or the other way around.

As mentioned above Yawls has some configurable parameters which you can edit with the GUI or direct in the configuration file
(/etc/yawls.properties) and here is a list of all parameters with a short description:

##### lowerLimit/upperLimit/minIncrease/maxIncrease:
The lowerLimit defines the shortest time in milliseconds between the camera activations to check the ambient light conditions. This value will be increased over time with the value of maxIncrease
if the light conditions are stable. This will be go on until it reaches the upperLimit.
If there is a change in the ambient light condition Yawls will fall back to the lowerLimit for a short time and jumps back to the value where it came from if the light conditions are stable.
Yawls will also increase the lowerLimit if your Notebook battery falls under 30% to be more battery friendly.

Default values:
- lowerLimit: 500
- upperLimit: 30000
- minIncrease: 100
- maxIncrease: 500

##### faceDetect:
If set to true Yawls tries to detect if someone is or is not in front of the Notebook. If Yawls does not detect any faces with the camera the screen will be dimmed to save battery (here Yawls fall
back to the lowerLimit and use the minIncrease like described above). If Yawls detect a face or you came back to your Notebook the screen wakes up and Yawls works like normal.

Default: false

##### darkeningThreshold:
This variable controls the darkening threshold. Here an example which is also included in the configuration file a value of 10 means that the current brightness has to be less than 75% of the last measured
ambient brightness. Only values between 0 and 100 are allowed.

Default: 50

##### brighteningThreshold:
This variable controls the brightening threshold. Here an example which is also included in the configuration file a value of 10 means that the current brightness has to be greater than 110 percent of the
last measured brightness.

Default: 25

##### logLevel:
The logLevel defines how detailed the log is: 0 (error) < 1 (warning) < 2 (info) < 3 (debug).

Default: 1

### Dependencies
* openjdk-7-jre-headless
* gettext-base
* libgtk-3-0
* libjava-gnome-java
* libopencv2.4-java
* policykit-1-gnome

### Issue tracking and more:

https://launchpad.net/yawls

You can find a PPA here:

https://launchpad.net/~thedsweb/+archive/ubuntu/yawls-daily

German Project Blog:

http://thedsweb.blogspot.de/p/yawls.html

### Downloads of the current stable release:
[yawls-gtk_1.1.2.deb](https://launchpad.net/yawls/1.1.x/1.1.2/+download/yawls-gtk_1.1.2_all.deb)

[yawls-cli_1.1.2.deb](https://launchpad.net/yawls/1.1.x/1.1.2/+download/yawls-cli_1.1.2_all.deb)

### BETA Download
[yawls_1.2.0.deb](https://launchpad.net/yawls/1.1.x/1.1.2/+download/yawls_1.2.0_all.deb)

### Build dependencies
* javahelper
* openjdk-7-jdk
* gettext-base
* libjava-gnome-java
* libopencv2.4-java
* ant
* po4a
