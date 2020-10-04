#!/bin/bash
nohup java -Xmx512m -cp tim.jar pro.belbix.tim.LoadAndTradeApp > /dev/null 2>&1 &
MyPID=$!
echo Start TIM LoadAndTradeApp with PID: $MyPID
echo $MyPID > ./pid