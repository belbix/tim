#!/bin/bash
nohup java -Xmx512m -cp tim.jar pro.belbix.tim.EmailAuth > /dev/null 2>&1 &
MyPID=$!
echo Start TIM EmailAuth with PID: $MyPID
echo $MyPID > ./pid