package com.lightbend.coursegentools

import java.io.File

case class MasterAdmCmdOptions(masterRepo: File = new File("."),
                               multiJVM: Boolean = false,
                               regenBuildFile: Boolean = false,
                               duplicateExerciseInsertBeforeNr: Option[Int] = None,
                               deleteExerciseNr: Option[Int] = None,
                               renumberExercises: Boolean = false,
                               renumberExercisesBase: Int = 0,
                               renumberExercisesStep: Int = 1,
                               configurationFile: Option[String] = None,
                               checkMaster: Boolean = false,
                               addMasterCommands: Boolean = false,
                               useConfigureForProjects: Boolean = false,
                               testFile: Option[File] = None,
                               isADottyProject: Boolean = false,
                               autoReloadOnBuildDefChange: Boolean = true)

case class StudentifyCmdOptions(masterRepo: File = new File("."),
                                out: File = new File("."),
                                multiJVM: Boolean = false,
                                first: Option[String] = None,
                                last: Option[String] = None,
                                selectedFirst: Option[String] = None,
                                configurationFile: Option[String] = None,
                                useConfigureForProjects: Boolean = false,
                                initAsGitRepo: Boolean = false,
                                isADottyProject: Boolean = false,
                                autoReloadOnBuildDefChange: Boolean = true)

case class LinearizeCmdOptions(masterRepo: File = new File("."),
                               linearRepo: File = new File("."),
                               multiJVM: Boolean = false,
                               forceDeleteExistingDestinationFolder: Boolean = false,
                               configurationFile: Option[String] = None,
                               isADottyProject: Boolean = false,
                               autoReloadOnBuildDefChange: Boolean = true)

case class DeLinearizeCmdOptions(masterRepo: File = new File("."),
                                 linearRepo: File = new File("."),
                                 configurationFile: Option[String] = None)
