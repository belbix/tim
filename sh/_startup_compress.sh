#!/bin/bash
nohup java -Xmx8G -Xms8G -cp tim.jar pro.belbix.tim.CompressApp > /dev/null 2>&1 &
MyPID=$!
echo Start TIM CompressApp with PID: $MyPID
echo $MyPID > ./pid
