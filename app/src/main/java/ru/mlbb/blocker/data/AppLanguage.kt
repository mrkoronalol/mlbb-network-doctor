package ru.mlbb.blocker.data

import android.content.Context
import java.util.Locale

/**
 * Язык интерфейса: системный или принудительный.
 *
 * Значение хранится в [BlocklistStore.language] ("system", "en", "ru", "id", "fil")
 * и применяется пересозданием Activity через [LanguageWrapper].
 */
object AppLanguage {

    const val SYSTEM = "system"

    /** Поддерживаемые языки в порядке переключателя. */
    val SUPPORTED = listOf("ru", "en", "id", "fil")

    /** Языки, у которых есть папка values-<lang>. */
    val TRANSLATED = listOf("en", "id", "fil")

    fun tags(): List<String> = listOf(SYSTEM) + SUPPORTED

    fun displayName(tag: String): String = when (tag) {
        SYSTEM -> "Системный"
        "ru" -> "Русский"
        "en" -> "English"
        "id" -> "Bahasa Indonesia"
        "fil" -> "Filipino"
        else -> tag
    }

    fun currentTag(context: Context): String {
        val stored = BlocklistStore(context).language
        return stored.takeIf { it == SYSTEM || it in SUPPORTED } ?: SYSTEM
    }

    fun set(context: Context, tag: String) {
        val normalized = tag.takeIf { it == SYSTEM || it in SUPPORTED } ?: SYSTEM
        BlocklistStore(context).language = normalized
    }

    /**
     * Локаль, которую нужно применить к контексту: null — системная.
     * Для непереведённых мест (логи, оверлей) возвращаем null, чтобы осталась
     * базовая русская или системная локализация.
     */
    fun localeFor(tag: String): Locale? = when (tag) {
        SYSTEM -> null
        in TRANSLATED -> Locale.forLanguageTag(tag)
        else -> null
    }

    /** Системная локаль в поддерживаемом виде (для подписи «Системный»). */
    fun systemTag(): String {
        val current = Locale.getDefault().language
        return if (current in SUPPORTED) current else "en"
    }
}
