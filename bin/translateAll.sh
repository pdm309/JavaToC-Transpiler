#!/usr/bin/env bash

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
function cleanup () {
	# $1 = input file path
	info "Running cleanup"
	sed -i "/\b\(0m\|info\)\b/d" $1
	success "Cleanup complete"
}

for i in `seq 0 50`; do
	echo "--------------------"
	info "Translating input: ${i}"

	if [[ i -lt 10 ]]; then
		sbt "runxtc -translate src/test/java/inputs/test00${i}/Test00${i}.java" &> /dev/null
		{
			sbt "cpp" || true
		} > src/test/java/inputs/test00${i}/Test00${i}.current_output 2>&1
		success "Translated input: ${i}"

		# Removes debug and info lines
		cleanup src/test/java/inputs/test00${i}/Test00${i}.current_output

		info "Diffing against correct output file"
		if diff src/test/java/inputs/test00${i}/Test00${i}.current_output src/test/java/inputs/test00${i}/Test00${i}.output ; then
			success "Output is correct for input: ${i}"
		else
			warn "Output is not correct for input: ${i}"
		fi
	else
		sbt "runxtc -translate src/test/java/inputs/test0${i}/Test0${i}.java" &> /dev/null
		{
			sbt "cpp" || true
		} > src/test/java/inputs/test0${i}/Test0${i}.current_output 2>&1
		success "Translated input: ${i}"

		# Removes debug and info lines
		cleanup src/test/java/inputs/test0${i}/Test0${i}.current_output

		info "Diffing against correct output file"
		if diff src/test/java/inputs/test0${i}/Test0${i}.current_output src/test/java/inputs/test0${i}/Test0${i}.output ; then
            success "Output is correct for input: ${i}"
        else
            warn "Output is not correct for input: ${i}"
        fi
	fi
	echo "--------------------"
done
