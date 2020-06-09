#!/bin/bash

# The path to the master repository
MASTER=$1
# A temp directory that has already been created for us...
TMP_DIR=$2
# The base name of the master repository
REPO_NAME=$3
# We're going to use this as the name of a folder that will be initialised
# as a base gt repo. Next, we'll push the content of checked out branch
# in the master repo into this repo.

REMOTE_REPO=${REPO_NAME}.git

CUR_DIR=$( pwd )

cd $MASTER
MASTER=$( pwd )
INIT_BRANCH=$( uuidgen )  # Name of branch that we'll be in on the cloned repo

TMP_REMOTE=CMT-$( uuidgen|tr '[:upper:]' '[:lower:]' )

# In the temp directory...
cd $TMP_DIR                                       &&
# We create a bare git repo
mkdir ${REMOTE_REPO}                              &&
git init --bare $REMOTE_REPO                      &&
# Get its path...
cd $REMOTE_REPO                                   &&
REPO=$( pwd )                                     &&
# Switch back to our master repo
cd $MASTER                                        &&
# ...and push whatever is in the current branch
git remote add $TMP_REMOTE $REPO                  &&
# ... into the bare repo
git push $TMP_REMOTE HEAD:refs/heads/$INIT_BRANCH &&
# We finish off by cloning the copied repo and
# check out a well named branch
cd $TMP_DIR                                       &&
git clone -b $INIT_BRANCH $REPO

# Some housekeeping
trap "cd $MASTER; git remote remove $TMP_REMOTE" 0
