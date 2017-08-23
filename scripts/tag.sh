#!/usr/bin/env bash
set -euo pipefail

while getopts "t:" o; do
    case "${o}" in
        t)
            TYPE=${OPTARG}
            ;;
    esac
done

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
cd $DIR/..

require_clean_work_tree () {
    git rev-parse --verify HEAD >/dev/null || exit 1
    git update-index -q --ignore-submodules --refresh
    err=0

    if ! git diff-files --quiet --ignore-submodules
    then
        echo >&2 "Cannot $1: You have unstaged changes."
        err=1
    fi

    if ! git diff-index --cached --quiet --ignore-submodules HEAD --
    then
        if [ $err = 0 ]
        then
            echo >&2 "Cannot $1: Your index contains uncommitted changes."
        else
            echo >&2 "Additionally, your index contains uncommitted changes."
        fi
        err=1
    fi

    if [ `git cherry -v | wc -l` -ne -0 ]
    then
	echo >&2 "You have un-pushed changes"
	err=1
    fi

    if [ `git ls-files -o -d --exclude-standard | sed q | wc -l` -ne 0 ]
    then
	echo >&2 "You have untracked files"
	err=1
    fi

    if [ $err = 1 ]
    then
        exit 1
    fi
}

echo "$(pwd)"

echo "Pulling develop and verifying it is clean..."
git pull origin develop
require_clean_work_tree

echo "Verifying that master is clean..."
git checkout master
git pull origin master
require_clean_work_tree

echo "Calculating tags"
CURRENT_TAG=$(git describe --abbrev=0 --tags 2>/dev/null)
if [ ! -z "$CURRENT_TAG" ]
then
    echo "Current tag $CURRENT_TAG"
    NEW_TAG=$("$DIR"/semver.sh bump "$TYPE" "$CURRENT_TAG")
else
    echo "No current tag found"
    NEW_TAG="1.0.0"
fi

echo "New tag $NEW_TAG"

echo "Merging changes into master"
git merge origin/develop

git tag -a $NEW_TAG -m "$NEW_TAG"
git push origin $NEW_TAG
git push origin master

git checkout $NEW_TAG
