#!/usr/bin/env bash

if [ "$TRAVIS_PULL_REQUEST" = "false" ]; then
  sed -i 's/https:\/\/github.com\//git@github.com:/' .git/config

  source travis/extract.sh
  source travis/docker.sh
  git config user.email "mleap@combust.ml"
  git config user.name "Combust, Inc."
  git checkout $TRAVIS_BRANCH

  if [ $# -eq 0 ]; then
    sbt "release with-defaults"
  else
    sbt "release $@"
  fi
else
  echo "Can only build releases on the master branch, cannot trigger with a pull request"
  exit 1
fi
