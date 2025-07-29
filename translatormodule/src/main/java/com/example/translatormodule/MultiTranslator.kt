package com.example.translatormodule

import android.content.Context
import android.util.Log
import com.google.mlkit.nl.translate.*
import com.google.android.gms.tasks.Task
import kotlinx.coroutines.*
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

object LanguageSet {
    val allLanguages = mapOf(
        "English" to TranslateLanguage.ENGLISH,
        "Hindi" to TranslateLanguage.HINDI,
        "Arabic" to TranslateLanguage.ARABIC,
        "Spanish" to TranslateLanguage.SPANISH,
        "Bengali" to TranslateLanguage.BENGALI,
        "French" to TranslateLanguage.FRENCH
    )
}

data class TranslationResult(
    val originalText: String,
    val selectedLanguages: List<String>,
    val translations: Map<String, String>
)

class Translator(private val context: Context) {

    private suspend fun translateText(input: String, targetLangCode: String): String = withContext(Dispatchers.IO) {
        val options = TranslatorOptions.Builder()
            .setSourceLanguage(TranslateLanguage.ENGLISH)
            .setTargetLanguage(targetLangCode)
            .build()

        val client = Translation.getClient(options)

        try {
            client.downloadModelIfNeeded().await()
            val result = client.translate(input).awaitString()
            client.close()
            result
        } catch (e: Exception) {
            client.close()
            "Error"
        }
    }

    suspend fun translateToSelected(
        input: String,
        selectedLanguages: List<String>
    ): TranslationResult {
        val resultMap = mutableMapOf<String, String>()
        var hasResult = false

        for (lang in selectedLanguages) {
            val langCode = LanguageSet.allLanguages[lang] ?: continue
            val translated = translateText(input, langCode)
            if (translated.isNotBlank() && translated != "Error") {
                hasResult = true
            }

            resultMap[lang] = translated
        }

        if (!hasResult) {
           Log.e("hasResult-no", "no result")
        }

        return TranslationResult(
            originalText = input,
            selectedLanguages = selectedLanguages,
            translations = resultMap
        )
    }
}

suspend fun Task<Void>.await() = suspendCoroutine<Unit> { cont ->
    addOnSuccessListener { cont.resume(Unit) }
    addOnFailureListener { cont.resumeWithException(it) }
}

suspend fun Task<String>.awaitString() = suspendCoroutine<String> { cont ->
    addOnSuccessListener { cont.resume(it) }
    addOnFailureListener { cont.resumeWithException(it) }
}