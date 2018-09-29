#!/bin/sh
# provide the input filename as $1
# for example,
#     ./tests.sh char1

runscanner() {
    curdir=$PWD
    cd `dirname $1`
    $(git rev-parse --show-toplevel)/run.sh -t scan `basename $1`
    cd $curdir
}

exitcode=0
fail=0
count=0

mkdir "$(dirname $0)/actual_output"
for file in `dirname $0`/input/*; do
  if [ -f "$(dirname $0)/input/$1" ] && [ "$(basename $file)" != "$1" ]; then
    continue
  fi

  echo
  output=$(tempfile 2> /dev/null || mktemp)
  runscanner $file > $output 2>&1;
  if ! colordiff $output "$(dirname $0)/output/$(basename $file).out"; then
    echo "File $file scanner output mismatch.";
    exitcode=1
    fail=$((fail+1))
  fi
  count=$((count+1))
  cat $output > "$(dirname $0)/actual_output/$(basename $file)"
  rm -f $output;
done

echo "Failed $fail tests out of $count";
exit $exitcode;
