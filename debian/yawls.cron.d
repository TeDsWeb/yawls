#
# Regular cron jobs for yawls
DISPLAY=:0
#
#Set all entries into comment state to deactivate the cron part of yawls completely
#
#Start yawls once every boot
@reboot root sleep 1m && /usr/bin/yawls -c
# Or every 60 Sek.
#*/1 * * * * root /usr/bin/yawls -c
# Or every 5 Min.
#*/5 * * * * root /usr/bin/yawls -c
# Or every 15 Min.
#*/15 * * * * root /usr/bin/yawls -c

