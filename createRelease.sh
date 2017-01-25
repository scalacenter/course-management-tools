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

WORKING_DIR="target/releases"

VERSIONED_BASE_NAME="$REPO_NAME-exercises-$VERSION"
UNVERIFIED_BASE_NAME="$REPO_NAME-exercises-UNVERIFIED"

function clean {
    echo $SEPARATOR
    echo "DELETING OLD RELEASES: $REPO_NAME*"
    echo $SEPARATOR
    
    rm -rf $REPO_NAME*
}

function studentify_repo {
    echo $SEPARATOR
    echo "STUDENTIFYING REPO: $REPO"
    echo $SEPARATOR
    
    cd $STARTING_DIR
    sbt "studentify $STUDENTIFY_ARGS $REPO $WORKING_DIR"
    cd $WORKING_DIR
}

function validate_repo {
    echo $SEPARATOR
    echo "VALIDATING: $VERSIONED_BASE_NAME"
    echo $SEPARATOR
    
    $STARTING_DIR/validateStudentRepo.sh "$VERSIONED_BASE_NAME"
}

function prepare_repo {
    echo $SEPARATOR
    echo "RENAMING: $REPO_NAME -> $VERSIONED_BASE_NAME"
    echo $SEPARATOR
    
    mv "$REPO_NAME" "$VERSIONED_BASE_NAME"

    echo $SEPARATOR
    echo "ADDING VERSION FILE: $VERSIONED_BASE_NAME/version.properties"
    echo $SEPARATOR
    
    echo "course.version=$VERSION" > "$VERSIONED_BASE_NAME/version.properties"
}

function zip_repo {
    echo $SEPARATOR
    echo "ZIPPING: $UNVERIFIED_BASE_NAME.zip"
    echo $SEPARATOR
    
    zip -r "$UNVERIFIED_BASE_NAME.zip" $VERSIONED_BASE_NAME
}

function release_repo {
    echo $SEPARATOR
    echo "RELEASING: $VERSIONED_BASE_NAME.zip"
    echo $SEPARATOR
    
    mv "$UNVERIFIED_BASE_NAME.zip" "$VERSIONED_BASE_NAME.zip"
}

mkdir -p $WORKING_DIR
cd $WORKING_DIR

clean
studentify_repo
prepare_repo
zip_repo
validate_repo
release_repo

cd $STARTING_DIR

echo $SEPARATOR
echo -e "[${GREEN}SUCCESS${RESET}] RELEASE CREATED $WORKING_DIR/$VERSIONED_BASE_NAME.zip"
echo $SEPARATOR