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
	sed -i "/\b\(0m\|info\|DEBUG\|Processing\|Panic\|sbt.TrapExitSecurityException\)\b/d" $1
	success "Cleanup complete"
}

for i in `seq 0 57`; do
	echo "--------------------"
	info "Generating Phase 1 Java ASTs: ${i}"

	if [[ i -lt 10 ]]; then
		{
			sbt "runxtc -printPhase1AST src/test/java/inputs/test00${i}/Test00${i}.java" || true
		} > src/test/java/inputs/test00${i}/Test00${i}.ast 2>&1
		success "Generated Phase 1 Java ASTs: ${i}"

		# Removes debug and info lines
		cleanup src/test/java/inputs/test00${i}/Test00${i}.ast
	else
        {
            sbt "runxtc -printPhase1AST src/test/java/inputs/test0${i}/Test0${i}.java" || true
        } > src/test/java/inputs/test0${i}/Test0${i}.ast 2>&1
        success "Generated Phase 1 Java ASTs: ${i}"

        # Removes debug and info lines
        cleanup src/test/java/inputs/test0${i}/Test0${i}.ast
	fi
	echo "--------------------"
done
