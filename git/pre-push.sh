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
function resetStash () {
	# Puts back the stuff that wasn't committed
	info "Git Stash: Putting back temporarily stashed files"
	git stash pop -q
}

# Makes sure that the script doesn't run on stuff outside of the committed repo
info "Stashing non-committed files temporarily"
git stash -q --keep-index

# Formats both C++ and Java source files
info "Formatting: Checking all Java & C++ code for formatting mistakes"
if [[ $(sbt format | grep "Formatted") == "" ]]; then
	success "Formatting: All code is correctly formatted"
else
	resetStash
	fail "Formatting: Code formatting differences were found, please re-add the formatted files"
fi

# Runs through all tests
info "Testing: Running all unit tests"
if sbt test ; then
	success "Testing: All unit tests pass"
else
	resetStash
	fail "Testing: Some unit tests failed, or were incorrectly made. To commit anyways, use \"git commit --no-verify -m 'COMMIT_MESSAGE_HERE'\""
fi

# Everything's good, resets the stash and completes the commit
resetStash

exit 0
