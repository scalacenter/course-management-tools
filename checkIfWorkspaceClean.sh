#!/bin/ksh

MASTER_REPO=$1
cd $MASTER_REPO

print "CHECKING WORKSPACE in $MASTER_REPO"
xx=`git status --porcelain|wc -l`

if [ $xx -eq 0 ];then
  exit 0
else
  exit 1
fi
