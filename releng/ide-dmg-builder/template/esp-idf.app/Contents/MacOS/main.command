#!/bin/sh

# main.command

#  Created by Brian Ignacio
#  Copyright (c) 2021 Espressif Systems, All Rights Reserved.

# Get local path of Application
FILEPATH=$(dirname $0)
BASEPATH=${FILEPATH%/*/*/*}
echo $BASEPATH


# Insert shell script code
# osascript -e 'tell app "Finder" to display dialog "Hello Cool IT Help"'
# sh $FILEPATH/start.sh
open -a Terminal.app $FILEPATH/esp-idf-terminal.sh