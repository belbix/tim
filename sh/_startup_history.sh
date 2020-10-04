#!/bin/bash
nohup java -Xmx8G -Xms8G -cp tim.jar pro.belbix.tim.HistoryApp > /dev/null 2>&1 &
MyPID=$!
echo Start TIM HistoryApp with PID: $MyPID
echo $MyPID > ./pid
