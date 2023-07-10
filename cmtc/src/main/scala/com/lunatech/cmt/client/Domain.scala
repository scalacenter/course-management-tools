package com.lunatech.cmt.client

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
object Domain:
  final case class ExerciseId(value: String)
  object ExerciseId:
    val default: ExerciseId = ExerciseId("")

  final case class ForceMoveToExercise(forceMove: Boolean)

  final case class TemplatePath(value: String)
  object TemplatePath:
    val default: TemplatePath = TemplatePath("")

end Domain
