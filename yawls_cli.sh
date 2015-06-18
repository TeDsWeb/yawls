#!/bin/bash
#
# Author: Dominik Brämer <thedsweb@googlemail.com>
#
# Please remove the "Author" lines above and replace them
# with your own name if you copy and modify this script.
#
# License GPLv3
# This program is free software: you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation, either version 3 of the License, or
# (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.

export NO_AT_BRIDGE=1

NAME="yawls"

#Refresh symlink to OpenCV jar file
OPENCVJAR=$(ls /usr/share/OpenCV/java)
ln -sf "/usr/share/OpenCV/java/$OPENCVJAR" '/usr/share/yawls/opencv.jar' &>/dev/null

#Path to openjdk 7
JAVA="/usr/lib/jvm/java-7-openjdk-*/bin/java"

#Set Java optimal options
if [[ $(arch) == "x86_64" ]]; then
	OPTIONS="-d64 -server -XX:+AggressiveOpts -Xmn12M  -Xms38M -Xmx38M"
else
	OPTIONS="-d32 -server -XX:+AggressiveOpts -Xmn12M  -Xms38M -Xmx38M"
fi

#Start program
$JAVA $OPTIONS -jar '/usr/share/yawls/yawls.jar' $@ &

#Create pid file if not existing already 
PID=$!
if [[ ! -f /var/run/$NAME.pid ]]; then
	trap "rm -f /var/run/$NAME.pid &>/dev/null" 0 1 2 3 15
	(echo $PID > /var/run/$NAME.pid) &>/dev/null
	wait $PID
else
	wait $PID
fi
