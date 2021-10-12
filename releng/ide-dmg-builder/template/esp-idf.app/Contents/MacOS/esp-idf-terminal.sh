#!/bin/sh
export PY_VERSION=3.8.7
export SHORTEN_PY_VERSION=3.8
export IDF_VERSION=4.3

export IDFPYINSTALLDIR=$(cd $(dirname $0); pwd)
export IDF_PATH=$IDFPYINSTALLDIR/application/esp-idf
export IDF_TOOLS_PATH=$IDFPYINSTALLDIR/application/.espressif
export PYTHON=$IDFPYINSTALLDIR/application/python/bin/python3
export PYTHON_PATH=$IDFPYINSTALLDIR/application/python/bin/python3
export IDF_PYTHON_ENV_PATH=$IDFPYINSTALLDIR/application/.espressif/python_env/idf${IDF_VERSION}_py${SHORTEN_PY_VERSION}_env
export PATH=$IDF_PYTHON_ENV_PATH/bin:$IDFPYINSTALLDIR/application/python/bin:$PATH
source $IDFPYINSTALLDIR/application/esp-idf/export.sh
sh