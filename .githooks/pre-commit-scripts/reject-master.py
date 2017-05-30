#!/usr/bin/env python

# This hook rejects any commits made directly to the master branch, so as to prevent those that do have the
# ability to push to that branch from doing so accidentally. It also serves as an example of a hook written
# in Python rather than as a shell script.

import subprocess
import sys

current_branch = subprocess.check_output("git rev-parse --abbrev-ref HEAD", shell=True).strip()

if current_branch == b'master':
  # The control codes below are to output bold text on a red background
  print('\x1b[1;37;41m'
  + 'Commit rejected as master branch checked-out. If you really want to commit, use the --no-verify flag.'
  + '\x1b[0m')
  sys.exit(1) # reject the commit

sys.exit(0) # allow the commit to continue
