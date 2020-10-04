#!/bin/bash
nohup java -Xmx512m -cp tim.jar pro.belbix.tim.DownloaderApp > /dev/null 2>&1 &
MyPID=$!
echo Start TIM download with PID: $MyPID
echo $MyPID > ./pid