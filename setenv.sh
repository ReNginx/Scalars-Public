#!/bin/bash

##
#  Sets relevant environment variables in the current shell instance.
#  e.g. For bash, use .[SPACE]setenv.sh
#  Invoked by build.sh and run.sh
#
#  For MIT Athena, customize athena.environment
#  For your local workspace, customize local.environment
##

gitroot=$(git rev-parse --show-toplevel)

ATHENA_ENV_FILE="${gitroot}/athena.environment"
LOCAL_ENV_FILE="${gitroot}/local.environment"

if [[ $(dnsdomainname) = mit.edu ]]; then
  set -a
  . ${ATHENA_ENV_FILE}
  set +a
else
  set -a
  . ${LOCAL_ENV_FILE}
  set +a
fi
