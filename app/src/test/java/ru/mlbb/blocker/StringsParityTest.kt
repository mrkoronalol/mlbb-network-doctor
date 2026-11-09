package ru.mlbb.blocker

import java.io.File
import javax.xml.parsers.DocumentBuilderFactory
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Паритет ключей локализованных ресурсов. Падает, если в `values-en` / `values-id` /
 * `values-fil` появился ключ, которого нет в базовой `values/strings.xml`, или наоборот
 * удалили перевод существующего ключа.
 */
class StringsParityTest {

    private fun keys(dir: String): Set<String> {
        val file = File(dir, "strings.xml")
        if (!file.exists()) return emptySet()
        val document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file)
        val nodes = document.getElementsByTagName("string")
        return (0 until nodes.length).map { index ->
            nodes.item(index).attributes.getNamedItem("name").nodeValue
        }.toSet()
    }

    @Test
    fun translatedKeysExistInBase() {
        val base = keys("src/main/res/values")
        assertTrue("base strings.xml is empty", base.isNotEmpty())

        for (lang in listOf("en", "id", "fil")) {
            val translated = keys("src/main/res/values-$lang")
            assertTrue("values-$lang is missing", translated.isNotEmpty())
            val unknown = translated - base
            assertTrue("values-$lang has keys missing in base: $unknown", unknown.isEmpty())
        }
    }

    @Test
    fun everyBaseKeyIsTranslated() {
        val base = keys("src/main/res/values")
        for (lang in listOf("en", "id", "fil")) {
            val translated = keys("src/main/res/values-$lang")
            val missing = base - translated
            assertTrue("values-$lang misses ${missing.size} keys: $missing", missing.isEmpty())
        }
    }

    @Test
    fun coreUiKeysAreTranslated() {        val core = setOf(
            "settings_tab",
            "home_tab",
            "match_tab",
            "diagnostics_title",
            "phase_title",
            "filter_mode_title",
            "settings_language_title",
            "status_running",
        )
        for (lang in listOf("en", "id", "fil")) {
            val translated = keys("src/main/res/values-$lang")
            val missing = core - translated
            assertTrue("values-$lang misses core keys: $missing", missing.isEmpty())
        }
    }
}
