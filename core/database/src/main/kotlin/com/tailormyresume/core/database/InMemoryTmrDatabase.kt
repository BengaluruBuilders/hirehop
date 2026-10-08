package com.tailormyresume.core.database

import android.content.Context
import androidx.room.Room

fun createInMemoryTmrDatabase(context: Context): TmrDatabase =
    Room.inMemoryDatabaseBuilder(context, TmrDatabase::class.java)
        .allowMainThreadQueries()
        .build()
