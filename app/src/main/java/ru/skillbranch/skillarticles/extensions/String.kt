package ru.skillbranch.skillarticles.extensions

//Поиск подстрок в строке с помощью меанизмов регулярных выражений
// может занимать длительное время. Ниже приложил свою реализацию,
// не такую простую, но возможно более быструю. Также, если есть желание,
// можно почитать про алгоритм Бойера-Мура.
fun String?.indexesOf(substr: String, ignoreCase:Boolean = true): List<Int> {
    if (substr.isEmpty()) {
        return emptyList()
    }
    return this?.let {
        val regex = if (ignoreCase) Regex(substr, RegexOption.IGNORE_CASE) else Regex(substr)
        regex.findAll(this).map { it.range.first }.toList()
    } ?: emptyList()
}

fun String?.indexesOfV2(needle: String, ignoreCase: Boolean = true): List<Int> {

    val indexes = mutableListOf<Int>()

    if (this.isNullOrEmpty() || needle.isEmpty()) return indexes

    var currentIdx = 0

    while (currentIdx > -1) {
        currentIdx = indexOf(needle, currentIdx, ignoreCase)
        if (currentIdx > -1) {
            indexes.add(currentIdx)
            currentIdx += needle.length
        }
    }

    return indexes
}