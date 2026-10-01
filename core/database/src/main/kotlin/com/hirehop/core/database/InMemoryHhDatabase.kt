package com.hirehop.core.database

import android.content.Context
import androidx.room.Room

fun createInMemoryHhDatabase(context: Context): HhDatabase =
    Room.inMemoryDatabaseBuilder(context, HhDatabase::class.java)
        .allowMainThreadQueries()
        .build()
