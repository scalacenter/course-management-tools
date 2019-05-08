#!/bin/bash

MASTER=$1
TMP_DIR=$2
REPO_NAME=$3
REMOTE_REPO=${REPO_NAME}.git
CUR_DIR=`pwd`

cd $MASTER
MASTER=`pwd`
CUR_BRANCH=`git branch | sed -e '/^ /d' -e 's/^..//'`

TMP_REMOTE=CMT-`uuidgen|tr '[:upper:]' '[:lower:]'`

cd $TMP_DIR                               &&
mkdir ${REMOTE_REPO}                      &&
git init --bare $REMOTE_REPO              &&
cd $REMOTE_REPO                           &&
REPO=`pwd`                                &&
cd $MASTER                                &&
git remote add $TMP_REMOTE $REPO          &&
git push $TMP_REMOTE $CUR_BRANCH          &&
cd $TMP_DIR                               &&
git clone -b $CUR_BRANCH $REPO

trap "cd $MASTER; git remote remove $TMP_REMOTE" 0