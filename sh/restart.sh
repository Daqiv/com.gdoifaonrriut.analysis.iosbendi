#!/bin/sh
export LC_ALL="zh_CN.UTF-8"
export LANG="zh_CN.UTF-8"

DIR=/home/analysis
$DIR/stop.sh

sleep 10

ps -ef | grep java | grep "com.dianru.analysis.boot.BootServer" > litao235

$DIR/start.sh

