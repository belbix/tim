#!/bin/bash
AppPID=`cat ./pid`
kill $AppPID
echo Stop app with PID: $AppPID
rm ./pid