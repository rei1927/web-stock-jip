package com.example

import android.app.Application
import com.example.data.PropertyDatabase
import com.example.data.PropertyRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

class JayaImperialApp : Application() {
    val applicationScope = CoroutineScope(SupervisorJob())

    val database by lazy { PropertyDatabase.getDatabase(this, applicationScope) }
    val repository by lazy { PropertyRepository(database.propertyDao()) }
}
