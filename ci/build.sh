#!/usr/bin/env bash

set -o errexit
set -o pipefail
set -o nounset
set -x

__dir="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
__root="$(cd "$(dirname "${__dir}")" && pwd)"
__file="${__dir}/$(basename "${BASH_SOURCE[0]}")"
__base="$(basename ${__file} .sh)"

JAVA_OPTS="-Dfile.encoding=utf-8 -Dsbt.ivy.home=\"${__root}/.ivy2\""

ls -lh ~/.ssh
ssh-keygen -y -f ~/.ssh/id_rsa || true

function log {
  echo $1
}

function err {
  echo $1 >&2
}

function branchname {  set +o nounset
  if [ -n "${DRONE_BRANCH}" ]; then
    basename "${DRONE_BRANCH}"
  else
    git reflog HEAD | grep 'checkout:' | head -1 | rev | cut -d' ' -f1 | rev
  fi
  set -o nounset
}

function githash {
  git rev-parse --short HEAD
}

#
# Run tests
#
function run_dev {
  version=$(githash)
  sbt $JAVA_OPTS "set every version := \"${version}\"" 'clean' 'coverage' 'test' 'coverageReport'
  sbt $JAVA_OPTS "set every version := \"${version}\"" 'coveralls'
  sbt $JAVA_OPTS "set every version := \"${version}\"" 'clean' 'project root' 'dist'
}

cd "${__root}"
branch=$(branchname)

java -version

if [ true ]; then
  log "Running dev CI script for branch: ${branch}"
  run_dev
fi
