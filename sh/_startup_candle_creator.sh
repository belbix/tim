#!/bin/bash
nohup java -Xmx2g -cp tim.jar pro.belbix.tim.CandleCreator > /dev/null 2>&1 &
MyPID=$!
echo Start TIM CandleCreator with PID: $MyPID
echo $MyPID > ./pid