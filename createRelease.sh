#!/bin/bash

set -euo pipefail
IFS=$'\n\t'

GREEN='\033[0;32m'
RESET='\033[0m' # No Color
SEPARATOR="##########################################################"

function help {
    echo ""
    echo "USAGE: createRelease [options] [directory]"
    echo ""
    echo "-v  Set the version number of the release. Defaults to SNAPSHOT."
    echo "-?  Print this help message."
}

while getopts "v:?" opt; do
  case $opt in
    v)
        echo "SETTING VERSION TO $OPTARG"
        VERSION=$OPTARG
        ;;
    ?)
        help
        exit 0
        ;;
    \?)
        echo "Invalid option: -$opt" >&2
        help
        ;;
  esac
done

shift $(($OPTIND - 1))

if [ -z ${1+x} ]; then
    echo "createRelease: Missing Parameter" >&2
    help
    exit 1
fi

STARTING_DIR=`pwd`
REPO=$1
REPO_NAME=`basename $REPO`

if [ -a $REPO/course_management.conf ]; then
    source $REPO/course_management.conf
fi

VERSION=${VERSION:-SNAPSHOT}
STUDENTIFY_ARGS=${STUDENTIFY_ARGS:-}

RELEASE_DIR="./target/releases"

COURSE_RELEASE_FOLDER="$RELEASE_DIR/$REPO_NAME"
COURSE_RELEASE_FILE="$RELEASE_DIR/$REPO_NAME-exercises-$VERSION.zip"

function clean {
    echo $SEPARATOR
    echo "DELETING OLD RELEASES: $COURSE_RELEASE_FOLDER $COURSE_RELEASE_FILE"
    echo $SEPARATOR
    
    rm -rf $COURSE_RELEASE_FOLDER
    rm -rf $COURSE_RELEASE_FILE
}

function studentify_repo {
    echo $SEPARATOR
    echo "STUDENTIFYING REPO: $REPO_NAME"
    echo $SEPARATOR
    
    mkdir -p $RELEASE_DIR
    
    sbt "studentify $STUDENTIFY_ARGS $REPO $RELEASE_DIR"
}

function validate_repo {
    echo $SEPARATOR
    echo "VALIDATING REPO: $REPO_NAME"
    echo $SEPARATOR
    
    ./validateStudentRepo.sh $COURSE_RELEASE_FOLDER
}

function prepare_repo {
    echo $SEPARATOR
    echo "PREPARING REPO: $REPO_NAME"
    echo $SEPARATOR
    
    echo "course.version=$VERSION" > $COURSE_RELEASE_FOLDER/version.properties
}

function zip_repo {
    echo $SEPARATOR
    echo "ZIPPING REPO: $REPO_NAME"
    echo $SEPARATOR
    
    cd $RELEASE_DIR
    
    zip -r "$STARTING_DIR/$COURSE_RELEASE_FILE" $REPO_NAME
    
    cd $STARTING_DIR
}

function clean_target {
    echo "Clean REPO: $REPO_NAME"
    echo find $RELEASE_DIR/$REPO_NAME -name target -depth -type d -exec rm -rf {} \;
    find $RELEASE_DIR/$REPO_NAME -name target -depth -type d -exec rm -rf {} \;
}

function reset_student_repo_state {
    echo "Resetting student repo state"
    (
      cd $RELEASE_DIR/$REPO_NAME
      sbt ";gotoExerciseNr 0;pullSolution"
    )
}

clean
studentify_repo
validate_repo
prepare_repo
reset_student_repo_state
clean_target
zip_repo

echo $SEPARATOR
echo -e "[${GREEN}SUCCESS${RESET}] RELEASE CREATED $COURSE_RELEASE_FILE"
echo $SEPARATOR
