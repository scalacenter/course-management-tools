package coursemgmttools.admin

/** Copyright 2022 - Eric Loots - eric.loots@gmail.com / Trevor Burton-McCreadie - trevor@thinkmorestupidless.com
  *
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
  * the License. You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
  * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *
  * See the License for the specific language governing permissions and limitations under the License.
  */

import caseapp.core.app.CommandsEntryPoint
import coursemgmttools.admin.command.{
  Delinearize,
  DuplicateInsertBefore,
  Linearize,
  New,
  RenumberExercises,
  Studentify,
  Version
}

object Main extends CommandsEntryPoint:
  override def progName = "cmta"
  override def commands = Seq(
    New.command,
    Delinearize.command,
    DuplicateInsertBefore.command,
    Linearize.command,
    RenumberExercises.command,
    Studentify.command,
    Version.command)
