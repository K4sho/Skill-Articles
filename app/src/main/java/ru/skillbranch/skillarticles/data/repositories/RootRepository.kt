package ru.skillbranch.skillarticles.data.repositories

import androidx.lifecycle.LiveData
import ru.skillbranch.skillarticles.data.PrefManager

interface IRootRepository {
    fun isAuth(): LiveData<Boolean>
}

class RootRepository(private val prefs: PrefManager = PrefManager()): IRootRepository {
    override fun isAuth(): LiveData<Boolean> = prefs.isAuth
}