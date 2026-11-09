package ru.mlbb.blocker.data

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import java.util.Locale

/**
 * Принудительная локаль для всего приложения: оборачивает базовый Context так,
 * что `stringResource(...)` в Compose берёт нужный `values-<lang>/strings.xml`.
 *
 * Использование: в `Activity` переопределить `attachBaseContext` и вызвать
 * `LanguageWrapper.wrap(base)`.
 */
object LanguageWrapper {

    fun wrap(base: Context): Context {
        val tag = AppLanguage.currentTag(base)
        val locale = AppLanguage.localeFor(tag) ?: return base
        Locale.setDefault(locale)
        val configuration = Configuration(base.resources.configuration)
        configuration.setLocale(locale)
        configuration.setLayoutDirection(locale)
        return base.createConfigurationContext(configuration)
    }

    /** Строка из нужной локали для мест, где Compose недоступен (оверлей, уведомления). */
    @SuppressLint("DiscouragedApi")
    fun getString(context: Context, resId: Int): String {
        val tag = AppLanguage.currentTag(context)
        val locale = AppLanguage.localeFor(tag) ?: return context.getString(resId)
        val config = Configuration(context.resources.configuration).apply {
            setLocale(locale)
        }
        val resources = Resources(context.assets, context.resources.displayMetrics, config)
        resources.updateConfiguration(config, context.resources.displayMetrics)
        return resources.getString(resId)
    }
}
