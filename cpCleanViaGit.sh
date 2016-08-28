#!/bin/ksh

MASTER=$1
TMP_DIR=$2
REPO_NAME=$3
REMOTE_REPO=${REPO_NAME}.git
CUR_DIR=`pwd`

cd $TMP_DIR                               &&
mkdir ${REMOTE_REPO}                      &&
git init --bare $REMOTE_REPO              &&
cd $REMOTE_REPO                           &&
REPO=`pwd`                                &&
cd $MASTER                                &&
git remote add tmpRepo $REPO              &&
git push tmpRepo master                   &&
git remote remove tmpRepo                 &&
cd $TMP_DIR                               &&
git clone $REPO
