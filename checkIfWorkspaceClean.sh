#!/bin/bash

MASTER_REPO=$1
cd $MASTER_REPO

print "CHECKING WORKSPACE in $MASTER_REPO"
dirtyLines=`git status --porcelain|wc -l`

if [ $dirtyLines -eq 0 ];then
  exit 0
else
  exit 1
fi
