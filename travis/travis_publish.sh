#!/usr/bin/env bash

if [[ $TRAVIS_BRANCH == 'master' ]] && [ "$TRAVIS_PULL_REQUEST" = "false" ]; then
  source travis/extract.sh
  source travis/docker.sh
  sbt "+ publishSigned" \
      "mleap-xgboost-runtime/publishSigned" \
      "mleap-xgboost-spark/publishSigned" \
      "mleap-serving/docker:publish" \
      "mleap-spring-boot/docker:publish"
fi
