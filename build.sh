#!/bin/bash
#
# Auto generate properties files out of po files
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

TEMPLATE=./po/yawls_template.pot
MANTEMPLATE=./po/yawls_man_template.pot

#Check Translation for empty or fuzzy strings -------------------------------------------
function checkPoFile() {
	COUNT=0
	
	while read LINE; do
  	if [[ "$LINE" == 'msgstr ""' ]]; then
    	read LINE
    	[[ -z "$LINE" ]] && ((COUNT++))
    fi
    
    if [[ "$LINE" == 'fuzzy' ]]; then
    	((COUNT++))
  	fi
  done <$1
  
  PERC=$((100*$COUNT/$MSGCOUNT))
  
  if [[ "$PERC" -lt "20" ]]; then
  	return 0
	else
		return 1
	fi
}

#Prepare translation --------------------------------------------------------------------

#Concat Last-Translators ----------------------------------------------------------------
AUTHORS=""

function translators() {
	LT=$(grep 'Last-Translator:' "$1" | grep -o ' .* ')
	LT=${LT# }
	LT=${LT% }

	if [[ "$(echo "$AUTHORS" | grep "$LT")" == "" ]] && [[ "$LT" != "FULL NAME" ]]; then
		if [[ "$AUTHORS" == "" ]]; then
			AUTHORS="$LT"
		else
			AUTHORS="$AUTHORS\n$LT"
		fi
	fi
}

#Delete old translations
rm -f ./src/com/blogspot/thedsweb/local/*.properties

#Upate template
xgettext './src/com/blogspot/thedsweb/main/Yawls.java' './src/com/blogspot/thedsweb/gui/Show.java'  --from-code=UTF-8 -k_ -o $TEMPLATE
sed 's/CHARSET/UTF-8/g' -i $TEMPLATE

msgcat -p $TEMPLATE -o ./src/com/blogspot/thedsweb/local/yawls_template.properties

for i in ./po/yawls_template-*.po; do
	msgmerge -q -U $i $TEMPLATE
	
	MSGCOUNT=$(grep -c 'msgid' $i)
	
	if checkPoFile $i; then
		translators $i
		
		FILE=./src/com/blogspot/thedsweb/local/$(basename ${i%.*} | sed 's/-/_/g').properties
		msgcat -p $i -o $FILE
		sed -i 's/\\\\/\\/g' $FILE
	fi
done

#Prepare man page translation -----------------------------------------------------------

#Delete old man page lists
rm -f ./debian/yawls.manpages
rm -f ./man/yawls.*.1

#Create new man page lists
echo man/yawls.1 >> ./debian/yawls.manpages

#Upate template
po4a-gettextize -f man -M UTF-8 -m ./man/yawls.1 -p $MANTEMPLATE

COMMENTS=""

for i in ./po/yawls_man_template-*.po; do
	LOCAL=$(basename ${i%.*})
	LOCAL=${LOCAL#*-}
	
	if [[ $LOCAL == "*" ]]; then
		break
	fi
	
	po4a-updatepo --msgmerge-opt -q -f man -M UTF-8 -m ./man/yawls.1 -p $i
	
	MSGCOUNT=$(grep -c 'msgid' $i)
	
	if checkPoFile $i; then
		translators $i
		
		DIS=$(grep -A 1 'msgid "Yawls automatic display brightness"' "$i" | grep 'msgstr')
		DIS=${DIS#*\"}
		DIS=${DIS%\"}
		
		if [[ "$DIS" != "" ]]; then
			if [[ "$COMMENTS" == "" ]]; then
				COMMENTS="Comment[$LOCAL]=$DIS"
			else
				COMMENTS="$COMMENTS\nComment[$LOCAL]=$DIS"
			fi
		fi
		
		if [[ "$(echo "$LOCAL" | grep "en_*")" == "" ]]; then
			po4a-translate -f man -M UTF-8 -m ./man/yawls.1 -p $i -l ./man/yawls.$LOCAL.1
			
			echo man/yawls.$LOCAL.1 >> ./debian/yawls.manpages
		fi
	fi
done

echo $AUTHORS > ./translator.txt

#Add translated discription to menu file ------------------------------------------------
sed -e '/^Comment\[/ d' \
-e '/Comment=Yawls automatic display brightness/a '"$COMMENTS" \
-i './Yawls.desktop'
