#!/usr/bin/env bash

# If this fails because you have changes that are not part of you commit then run this first: git stash save --keep-index --include-untracked
files_to_lint=$(git diff --cached --name-only --diff-filter=ACM | grep "\.java$")
if [ -n "$files_to_lint" ]; then
  ./gradlew check -x test
  if [ $? -ne 0 ]; then
    echo "" || true
    echo "This has run checks against all changes in the repository - to just run against the changes that are being committed run this first (and pop after): git stash save --keep-index --include-untracked" || true
    exit 0  # Change to 1 when we are happy to fail the build
  fi
fi

exit 0
