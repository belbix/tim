#!/bin/bash
ps aux | grep [p]ro.belbix.tim.DownloaderApp > /dev/null
if [ $? -eq 0 ]; then
  echo "process_status,host=$(hostname),proc=load working=1"
else
  echo "process_status,host=$(hostname),proc=load working=0"
fi
