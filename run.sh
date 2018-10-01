#!/bin/bash

if [[ $(dnsdomainname) = mit.edu ]]; then
  eval $(attach -Padd -b -f scala)
fi

gitroot=$(git rev-parse --show-toplevel)
. $gitroot/setenv.sh

if [ -n "$JAVA_TOOL_OPTIONS" ]; then
  export JAVA_OPTS="$JAVA_TOOL_OPTIONS"
  unset JAVA_TOOL_OPTIONS
fi

scala -classpath $gitroot/build/lib/project.jar:$gitroot/lib/antlr.jar compile.Compiler "$@"

