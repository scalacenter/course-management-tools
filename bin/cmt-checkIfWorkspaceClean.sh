#!/bin/bash

MAIN_REPO=$1
cd $MAIN_REPO

echo "CHECKING WORKSPACE in $MAIN_REPO"
dirtyLines=`git status --porcelain|wc -l`

if [ $dirtyLines -eq 0 ];then
  exit 0
else
  exit 1
fi
