#!/bin/bash
ps aux | grep [p]ro.belbix.tim.EvolutionApp > /dev/null
if [ $? -eq 0 ]; then
  echo "process_status,host=$(hostname),proc=evo working=1"
else
  echo "process_status,host=$(hostname),proc=evo working=0"
fi
