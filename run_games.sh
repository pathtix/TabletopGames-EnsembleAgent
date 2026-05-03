#!/bin/sh
# Usage: ./run_games.sh <ram> <config.json>
#   <ram>         heap size passed to -Xmx (e.g. 2g, 4g, 8g, 512m)
#   <config.json> RunGames config file
ram=$1
config=$2
stem="${config%.json}"
java -jar -Xmx${ram} --add-opens="java.base/sun.nio.ch=ALL-UNNAMED" ./target/TAG.jar RunGames config=$config > ./${stem}.log 2>&1
