#!/bin/bash
eval $(attach -Padd -b -f scala)
export SCALA_HOME=/usr/local/opt/scala/idea
ant "$@"
