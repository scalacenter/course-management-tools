package com.lunatech.cmt.admin.command

import com.lunatech.cmt.Helpers.extractExerciseNr
def renumberExercise(exercise: String, exercisePrefix: String, newNumber: Int): String =
  val newNumberPrefix = f"${exercisePrefix}_$newNumber%03d_"
  val oldNumberPrefix =
    f"${exercisePrefix}_${extractExerciseNr(exercise)}%03d_"
  exercise.replaceFirst(oldNumberPrefix, newNumberPrefix)
