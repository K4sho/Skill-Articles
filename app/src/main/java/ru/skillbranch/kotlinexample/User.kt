package ru.skillbranch.kotlinexample

import androidx.annotation.VisibleForTesting
import ru.skillbranch.kotlinexample.UserHolder.toNormalizedLogin
import java.math.BigInteger
import java.security.MessageDigest
import java.security.SecureRandom

class User private constructor(
    private val firstName: String,
    private val lastName: String?,
    email: String? = null,
    rawPhone: String? = null,
    meta: Map<String, Any>? = null
) {
    val userInfo: String

    // Полное имя
    private val fullName: String
        get() = listOfNotNull(firstName, lastName).joinToString(" ").capitalize()

    // Инициалы
    private val initials: String
        get() = listOfNotNull(firstName, lastName).map { it.first().toUpperCase() }.joinToString(" ")

    // Телефон
    private var phone: String? = null
        set(value) {
            // Удаляем все кроме чисел
            field = value?.replace("""[^+\d]""".toRegex(), "")
        }

    private var _login: String? = null
    var login: String
    set(value) {
        _login = value.toLowerCase()
    }
    get() = _login!!

    private var salt: String? = null

    private lateinit var passwordHash: String

    @VisibleForTesting(otherwise = VisibleForTesting.NONE)
    var accessCode: String? = null

    // For Email
    constructor(
        firstName: String,
        lastName: String?,
        email: String,
        password: String
    ) : this(firstName, lastName, email = email, meta = mapOf("auth" to "password")) {
        println("Secondary email constructor")
        passwordHash = encrypt(password)
    }

    // For Phone
    constructor(
        firstName: String,
        lastName: String?,
        rawPhone: String
    ) : this(firstName, lastName, rawPhone = rawPhone, meta = mapOf("auth" to "sms")) {
        println("Secondary phone consturctor")
        val code = generateAccessCode()
        passwordHash = encrypt(code)
        println("Phone passwordHash is ${passwordHash}")
        accessCode = code
        sendAccessCodeToUser(rawPhone, code)
    }

    init {
        println("First init block, primary constructor was called")

        check(firstName.isNotBlank()) { "First name must not be blank" }
        check(!email.isNullOrBlank() || !rawPhone.isNullOrBlank()) { "Email or Phone must not be null or blank" }

        phone = rawPhone?.toNormalizedLogin()
        login = if (email.isNullOrBlank()) {
            checkPhone(phone!!)
            phone!!
        } else {
            email
        }

        userInfo = """
            firstName: $firstName
            lastName: $lastName
            login: $login
            fullName: $fullName
            initials: $initials
            email: $email
            phone: $phone
            meta: $meta
        """.trimIndent()
    }

    fun checkPassword(password: String) = encrypt(password) == passwordHash.also {
        println("Checking passwordHash is $passwordHash")
    }

    // Отправка кода доступа на телефон пользователя
    fun sendAccessCodeToUser(rawPhone: String, code: String) {
        println("... sending access code: $code on $phone")
    }

    private fun encrypt(password: String): String {
        if (salt.isNullOrEmpty()) {
            salt = ByteArray(16).also { SecureRandom().nextBytes(it) }.toString()
        }
        println("Salt while encrypt: $salt")
        return salt.plus(password).md5()
    }

    private fun String.md5(): String {
        val md = MessageDigest.getInstance("MD5")
        val digest = md.digest(toByteArray())
        val hexString = BigInteger(1, digest).toString(16)
        return hexString.padStart(32, '0')
    }

    fun generateAccessCode(): String {
        val possible = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
        return StringBuilder().apply {
            repeat(6) {
                (possible.indices).random().also { index ->
                    append(possible[index])
                }
            }
        }.toString()
    }

    fun changePassword(oldPass: String, newPass: String) {
        if (checkPassword(oldPass)) passwordHash = encrypt(newPass)
        else throw IllegalAccessException("The entered password does not match the current password")
    }

    fun changeAccessCode(): Unit {
        val code = generateAccessCode()
        passwordHash = encrypt(code)
        accessCode = code
    }

    companion object Factory {
        fun makeUser(
            fullName: String,
            email: String? = null,
            password: String? = null,
            phone: String? = null
        ) : User {
            val (firstName, lastName) = fullName.fullNameToPair()
            return when {
                !phone.isNullOrBlank() -> User(firstName, lastName, phone)
                !email.isNullOrBlank() -> User(firstName, lastName, email, password = password!!)
                else -> throw IllegalArgumentException("Email or phone must be not null or blank")
            }
        }

        // Возвращает из полного имени - пару(имя, фамилия)
        private fun String.fullNameToPair(): Pair<String, String?> {
            return this.split(" ").filter { it.isNotBlank() }.run {
                when(size) {
                    1 -> first() to null
                    2 -> first() to last()
                    else -> throw IllegalArgumentException("Full name must contain only first name and last name, " +
                            "current split result ${this@fullNameToPair}")
                }
            }
        }
    }
    private fun checkPhone(phone: String): Boolean {
        return when {
            """^\+\d((\d{3})|(\(\d{3}\)))\d{3}[-]?\d{2}[-]?\d{2}$""".toRegex()
                    .containsMatchIn(phone) -> true
            else -> throw IllegalArgumentException("Enter a valid phone number starting with a + and containing 11 digits")
        }
    }

}