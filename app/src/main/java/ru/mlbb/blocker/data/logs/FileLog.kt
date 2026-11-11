package ru.mlbb.blocker.data.logs

import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Постоянный файловый лог приложения.
 *
 * Пишет ВСЕГДА, без переключателя: события сервиса, соединения, фазы, блокировки,
 * ошибки. Файл дописывается постранично и ротируется по размеру, чтобы не забить
 * память телефона.
 *
 * Формат строки: `2026-11-11T22:31:05.123Z  [tag]  сообщение`.
 */
object FileLog {

    const val FILE_NAME = "doctor.log"
    const val MAX_BYTES = 2L * 1024 * 1024 // 2 МБ
    private const val KEEP_FILES = 3

    @Volatile
    private var dir: File? = null

    @Volatile
    private var stream: OutputStreamWriter? = null

    private val stamp = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.US)

    /** Вызывается из Application.onCreate один раз. */
    @Synchronized
    fun initialize(context: android.content.Context) {
        if (dir != null) return
        val logsDir = File(context.getExternalFilesDir(null) ?: context.filesDir, "logs").apply { mkdirs() }
        dir = logsDir
        open(File(logsDir, FILE_NAME))
    }

    val directory: File? get() = dir

    val file: File? get() = dir?.let { File(it, FILE_NAME) }

    @Synchronized
    fun write(tag: String, message: String) {
        val writer = stream ?: return
        val line = "${stamp.format(Date())}  [$tag]  ${message.replace('\n', ' ')}\n"
        runCatching {
            writer.write(line)
            writer.flush()
        }
        rotateIfNeeded()
    }

    @Synchronized
    fun info(message: String) = write("app", message)

    @Synchronized
    fun error(message: String) = write("error", message)

    /** Читает хвост журнала (последние [maxBytes] байт) для экспорта. */
    fun readTail(maxBytes: Int = 512 * 1024): String {
        val current = file ?: return ""
        if (!current.exists()) return ""
        return runCatching {
            val length = current.length()
            val from = (length - maxBytes).coerceAtLeast(0)
            java.io.RandomAccessFile(current, "r").use { raf ->
                raf.seek(from)
                val buffer = ByteArray((length - from).toInt())
                raf.readFully(buffer)
                String(buffer, Charsets.UTF_8)
            }
        }.getOrDefault("")
    }

    /** Полный путь к файлу для шаринга через FileProvider. */
    fun shareableFile(): File? = file?.takeIf { it.exists() }

    private fun open(target: File) {
        runCatching { stream?.close() }
        stream = null
        runCatching {
            FileOutputStream(target, true).let { out ->
                stream = OutputStreamWriter(out, Charsets.UTF_8)
            }
        }
    }

    private fun rotateIfNeeded() {
        val current = file ?: return
        if (current.length() < MAX_BYTES) return
        runCatching {
            stream?.close()
            stream = null
            // doctor.2.log ← doctor.1.log ← doctor.log
            for (index in KEEP_FILES - 1 downTo 1) {
                val source = File(dir!!, if (index == 1) FILE_NAME else "$FILE_NAME.${index - 1}")
                val target = File(dir!!, "$FILE_NAME.$index")
                if (source.exists()) {
                    if (target.exists()) target.delete()
                    source.renameTo(target)
                }
            }
        }
        open(current)
    }
}
