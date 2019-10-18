# Copyright 2018 Google LLC
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

import click
import git
import os

from fireci import ci_command


@click.option(
    '--forbid-new-files',
    '-n',
    help='Checks if latest PR added files with specified extensions.',
    multiple=True,
    required=True,
    type=str,
)
@ci_command()
def git_check(forbid_new_files):
  repo = git.Repo('.')
  top = repo.commit(repo.head.log()[-1].newhexsha)
  previous = repo.commit(repo.head.log()[-2].newhexsha)
  diffs = previous.diff(top)

  matching_added_files = [
      x.a_path for x in previous.diff(top) if x.new_file and any(
          ext
          for ext in forbid_new_files if os.path.splitext(x.a_path)[1] == ext)
  ]
  if matching_added_files:
    raise click.ClickException(
        "Adding new groovy files is strongly discouraged, please use Java instead. Violating files:\n{}"
        .format('\n'.join(matching_added_files)))
