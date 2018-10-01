#!/bin/bash

if [[ $(dnsdomainname) = mit.edu ]]; then
  export SCALA_HOME=/mit/scala/scala/scala-2.11.2/
else
  export JAVA_HOME="/usr/lib/jvm/java-8-jdk/"
  export SCALA_HOME="/opt/scala/scala-2.11.2/"
  export PATH="$SCALA_HOME/bin:$PATH"
fi
