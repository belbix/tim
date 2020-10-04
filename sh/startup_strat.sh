#!/bin/bash
nohup java -Xmx512m -cp tim.jar pro.belbix.tim.StrategyApp > /dev/null 2>&1 &
MyPID=$!
echo Start TIM strategy with PID: $MyPID
echo $MyPID > ./pid