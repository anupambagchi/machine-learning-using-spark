#!/usr/bin/env bash

#processcount=`ps auxww | grep java | grep -v "/bin/sh -c" | grep "LOOCVExample" | wc -l`
#if [ "$processcount" != "0" ] ; then
#      exit
#fi

CLASSNAME=com.anupambagchi.emailcampaign.CustomerOpenResponsePredictor

pushd `dirname $0` > /dev/null
SCRIPTPATH=`pwd -P`
popd > /dev/null

JVM="-Xmn100M -Xms1024M -Xmx9048M"
source $SCRIPTPATH/setclasspath.sh
UBERJAR=${SCRIPTPATH}/../target/spark.application-1.0-jar-with-dependencies.jar

echo Running Email Campaign Learner using SPARK ...
echo spark-submit --master local[*] --class $CLASSNAME $UBERJAR
spark-submit --master local[*] --class $CLASSNAME $UBERJAR
