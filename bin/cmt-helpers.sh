#!/bin/bash
set -x

if [ $# -eq 0 ]; then
  echo "No arguments supplied"
  exit 1
fi

function clean_main_via_git() {
  # The path to the main repository
  local MAIN=$1
  # A temp directory that has already been created for us...
  local TMP_DIR=$2
  # The base name of the main repository
  local REPO_NAME=$3
  # We're going to use this as the name of a folder that will be initialised
  # as a base gt repo. Next, we'll push the content of checked out branch
  # in the main repo into this repo.

  local REMOTE_REPO=${REPO_NAME}.git

  cd "$MAIN" || exit
  MAIN=$( pwd )
  INIT_BRANCH=$( uuidgen )  # Name of branch that we'll be in on the cloned repo

  TMP_REMOTE=CMT-$( uuidgen|tr '[:upper:]' '[:lower:]' )

  # In the temp directory...
  cd "$TMP_DIR"                                         &&
  # We create a bare git repo
  mkdir "${REMOTE_REPO}"                                &&
  git init --bare "$REMOTE_REPO"                        &&
  # Get its path...
  cd "$REMOTE_REPO"                                     &&
  REPO=$( pwd )                                         &&
  # Switch back to our main repo
  cd "$MAIN"                                            &&
  # ...and push whatever is in the current branch
  git remote add "$TMP_REMOTE" "$REPO"                  &&
  # ... into the bare repo
  git push "$TMP_REMOTE" HEAD:refs/heads/"$INIT_BRANCH" &&
  # We finish off by cloning the copied repo and
  # check out a well named branch
  cd "$TMP_DIR"                                         &&
  git clone -b "$INIT_BRANCH" "$REPO"

  # Some housekeeping
  trap "cd $MAIN; git remote remove $TMP_REMOTE" 0
}

function extract_template() {
  local TMP_DIR=$1
  local REMOTE_REPO=$2
  local TEMPLATE_NAME=$3
  local TARGET_DIR=$4
  local COURSE_NAME=$5

  echo "Checkout template into $TMP_DIR"

  cd "$TMP_DIR" || exit
  git init &&
  git remote add -f origin "$REMOTE_REPO" &&
  git config core.sparseCheckout true &&
  echo "course-templates/$TEMPLATE_NAME" >> .git/info/sparse-checkout &&
  git pull origin main --depth=1 --ff-only

  TEMPLATE_PATH="$TMP_DIR/course-templates/$TEMPLATE_NAME/"
  echo "Template created in $TEMPLATE_PATH"
  echo "Copying template to $TARGET_DIR"
  mkdir -p "$TARGET_DIR/$COURSE_NAME"    &&
  cp -r "$TEMPLATE_PATH" "$TARGET_DIR/$COURSE_NAME"

  trap "rm -rf $TMP_DIR" 0
}

function check_workspace_clean() {
  local MAIN_REPO=$1
  cd "$MAIN_REPO" || exit 1

  echo "CHECKING WORKSPACE in $MAIN_REPO"
  dirtyLines=$(git status --porcelain|wc -l)

  if [ "$dirtyLines" -eq 0 ];then
    exit 0
  else
    exit 1
  fi
}

case $1 in
  "cpCleanViaGit")
    clean_main_via_git "$2" "$3" "$4"
    ;;
  "extractTemplate")
   extract_template "$2" "$3" "$4" "$5" "$6"
   ;;
 "checkWorkspace")
  check_workspace_clean "$2"
 ;;
 *)
   echo "Invalid arguments"
   exit 1
esac