SHELL := /bin/bash

.PHONY: py36_test py37_test

py36_test:
	source scripts/scala_classpath_for_python.sh && make -C python py36_test

py37_test:
	source scripts/scala_classpath_for_python.sh && make -C python py37_test
