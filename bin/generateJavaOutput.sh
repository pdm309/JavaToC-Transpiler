#!/bin/bash

set -e

###
# Helper functions
##
function info () {
	printf "\r  [ \033[00;34m..\033[0m ] %s\n" "$1"
}
function user () {
	printf "\r  [ \033[0;33m??\033[0m ] %s " "$1"
}
function success () {
	printf "\r\033[2K  [ \033[00;32mOK\033[0m ] %s\n" "$1"
}
function warn () {
  printf "\r\033[2K  [\033[0;31mWARN\033[0m] %s\n" "$1"
}
function fail () {
  printf "\r\033[2K  [\033[0;31mFAIL\033[0m] %s\n" "$1"
  echo ''
  exit 1
}

for i in `seq 0 57`; do
	echo "--------------------"
	info "Running and capturing input: ${i}"
	if [[ i -lt 10 ]]; then
		# Compiles the java files
		info "Compiling java input: ${i}"
		javac src/test/java/inputs/test00${i}/*.java -d src/test/java/
		success "Java input: ${i} compiled"

		# Executes and pipes the output
		{
			java -cp src/test/java/ inputs/test00${i}/Test00${i} || true
		} > src/test/java/inputs/test00${i}/Test00${i}.output 2>&1
	else
		# Compiles the java files
		info "Compiling java input: ${i}"
		javac src/test/java/inputs/test0${i}/*.java -d src/test/java/
		success "Java input: ${i} compiled"

		# Executes and pipes the output
		{
			java -cp src/test/java/ inputs/test0${i}/Test0${i} || true
		} > src/test/java/inputs/test0${i}/Test0${i}.output 2>&1
	fi
	echo "--------------------"
done
