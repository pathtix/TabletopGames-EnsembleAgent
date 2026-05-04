#!/bin/sh
ram=$1
config=$2
stem="RunLogs/$(basename ${config%.json})"
java -jar -Xmx${ram} --add-opens="java.base/sun.nio.ch=ALL-UNNAMED" ./target/TAG.jar RunGames config=$config > ./${stem}.log 2>&1
