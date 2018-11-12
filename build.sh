#!/bin/bash

## Build script for Scalars Decaf Compiler

if [[ $(dnsdomainname) = mit.edu ]]; then
  eval $(attach -Padd -b -f scala)
fi

gitroot=$(git rev-parse --show-toplevel)
. $gitroot/setenv.sh

ant "$@"
