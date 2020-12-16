#!/bin/bash
SERVER_NAME="databaseLink-0.0.1-SNAPSHOT.jar" #servername must less than 32,and must be unique
pid=`ps -eo pid,cmd|grep $SERVER_NAME|grep -v grep|gawk \{print\\$1\}`;
while [ $pid ]; do    # kill process until $pid is null
  let count=$count+1
  echo "Stopping $SERVER_NAME $count times"
  echo "kill -s 9 $pid"
  kill -s 9 $pid
  sleep 3;
  	pid=`ps -eo pid,cmd|grep $SERVER_NAME|grep -v grep|gawk \{print\\$1\}`;
done
	echo "Stop $SERVER_NAME successfully." 