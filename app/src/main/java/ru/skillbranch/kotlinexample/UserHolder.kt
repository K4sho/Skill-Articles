package ru.skillbranch.kotlinexample

import androidx.annotation.VisibleForTesting

object UserHolder {
    private val map = mutableMapOf<String, User>()

    // Регистрация пользователя
    fun registerUser(fullName: String, email: String, password: String): User {
        return User.makeUser(fullName, email = email, password = password).also {
            user -> if (map.containsKey(user.login)) {
                throw IllegalArgumentException("A user with this email already exists")
            } else {
                map[user.login] = user
            }
        }
    }

    // Реализуй метод registerUserByPhone(fullName: String, rawPhone: String)
    fun registerUserByPhone(fullName: String, rawPhone: String): User {
        return User.makeUser(fullName = fullName, phone = rawPhone).also { user ->
            map[user.login]
                    ?.let { throw IllegalArgumentException("Enter a valid phone number starting with a + and containing 11 digits") }
                    ?: let { map[user.login] = user }
        }
    }

    fun loginUser(login: String, password: String): String? =
            map[login.trim()]?.let {
                if (it.checkPassword(password)) it.userInfo
                else null
            }

    // Запрос нового кода авторизации по номеру телефона
    fun requestAccessCode(login: String) : Unit {
        map[login]?.changeAccessCode()
    }

    @VisibleForTesting(otherwise = VisibleForTesting.NONE)
    fun clearHolder() {
        map.clear()
    }

    fun String.toNormalizedLogin(): String {
        return replace(" ", "").replace("(", "").replace(")", "").replace("-", "").trim()
    }
}