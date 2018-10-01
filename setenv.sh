#!/bin/bash

##
#  Sets relevant environment variables in the current shell instance.
#  e.g. For bash, use .[SPACE]setenv.sh
#  Invoked by build.sh and run.sh
#
#  For MIT Athena, customize athena.environment
#  For your local workspace, customize local.environment
##

ATHENA_ENV_FILE="athena.environment"
LOCAL_ENV_FILE="local.environment"

if [[ $(dnsdomainname) = mit.edu ]]; then
  set -a
  . ./${ATHENA_ENV_FILE}
  set +a
else
  set -a
  . ./${LOCAL_ENV_FILE}
  set +a
fi
