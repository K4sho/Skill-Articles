package ru.skillbranch.skillarticles.data.adapters

import ru.skillbranch.skillarticles.data.local.User

/**
 * Класс позволяет конвертировать объект из Json и обратно
 */
class UserJsonAdapter() : JsonAdapter<User> {
    override fun fromJson(json: String): User? {
        TODO("Not yet implemented")
    }

    override fun toJson(obj: User?): String {
        TODO("Not yet implemented")
    }
}