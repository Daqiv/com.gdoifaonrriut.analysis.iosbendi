#!/bin/sh
export LC_ALL="zh_CN.UTF-8"
export LANG="zh_CN.UTF-8"

DIR=/home/analysis
cd $DIR

LIBS="$DIR/conf"
for i in $DIR/lib/*.jar;do
    LIBS="$LIBS:$i"
done

for i in $DIR/app/*.jar;do
    LIBS="$LIBS:$i"
done

JVMS='-server -Xmx2048m -Xms1024m -XX:+UseConcMarkSweepGC'

#java $JVMS -cp $LIBS com.dianru.analysis.boot.BootServer
/usr/bin/java $JVMS -cp $LIBS com.dianru.analysis.boot.BootServer &


