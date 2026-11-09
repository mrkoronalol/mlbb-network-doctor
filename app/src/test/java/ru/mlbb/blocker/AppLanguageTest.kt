package ru.mlbb.blocker

import org.junit.Assert.assertEquals
import org.junit.Test
import ru.mlbb.blocker.data.AppLanguage

class AppLanguageTest {

    @Test
    fun exposesSystemAndFourLanguages() {
        assertEquals(
            listOf("system", "ru", "en", "id", "fil"),
            AppLanguage.tags(),
        )
    }

    @Test
    fun hasFolderBackedTranslations() {
        // values-en / values-id / values-fil существуют; русский — базовая папка.
        assertEquals(listOf("en", "id", "fil"), AppLanguage.TRANSLATED)
        for (tag in AppLanguage.SUPPORTED) {
            assertEquals(true, tag in AppLanguage.TRANSLATED || tag == "ru")
        }
    }

    @Test
    fun displayNamesAreHumanReadable() {
        assertEquals("Русский", AppLanguage.displayName("ru"))
        assertEquals("English", AppLanguage.displayName("en"))
        assertEquals("Bahasa Indonesia", AppLanguage.displayName("id"))
        assertEquals("Filipino", AppLanguage.displayName("fil"))
        assertEquals("Системный", AppLanguage.displayName(AppLanguage.SYSTEM))
    }

    @Test
    fun unsupportedTagFallsBackToSystem() {
        assertEquals(null, AppLanguage.localeFor("de"))
        assertEquals(null, AppLanguage.localeFor(AppLanguage.SYSTEM))
        assertEquals("id", AppLanguage.localeFor("id")?.language)
    }
}
