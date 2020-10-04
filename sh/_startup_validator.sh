#!/bin/bash
nohup java -Xmx512m -cp tim.jar pro.belbix.tim.validators.TickValidator > /dev/null 2>&1 &
MyPID=$!
echo Start TIM TickValidator with PID: $MyPID
echo $MyPID > ./pid
