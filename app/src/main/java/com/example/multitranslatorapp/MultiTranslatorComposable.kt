package com.example.multitranslatorapp


import android.app.Activity
import android.content.Intent
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.example.speechtotextmodule.buildSpeechRecognizerIntent
import com.example.translatormodule.LanguageSet
import com.example.translatormodule.TranslationResult
import com.example.translatormodule.Translator
import kotlinx.coroutines.launch
import java.util.*

@Composable
fun TranslatorScreen() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val inputText = remember { mutableStateOf("") }
    val selectedLanguages = remember { mutableStateListOf<String>() }
    val result = remember { mutableStateOf<TranslationResult?>(null) }
    var errorText by remember { mutableStateOf<String?>(null) }

    val allLanguages = LanguageSet.allLanguages.keys.toList()

    val speechResultLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { speechResult ->
        if (speechResult.resultCode == Activity.RESULT_OK) {
            val data = speechResult.data
            val spokenText =
                data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()
            spokenText?.let {
                inputText.value = it
                errorText = null
            }
        }
    }

    fun startSpeechToText() {
        val intent = buildSpeechRecognizerIntent()
        speechResultLauncher.launch(intent)
    }


    Column(modifier = Modifier.padding(16.dp)) {

        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = inputText.value,
                onValueChange = {
                    inputText.value = it
                    errorText = null
                },
                label = { Text("Enter or Speak text") },
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done)
            )

            Spacer(modifier = Modifier.height(8.dp))

            IconButton(onClick = { startSpeechToText() }) {
                Image(
                    painter = painterResource(R.drawable.mic),
                    contentDescription = "mic",
                    Modifier.size(30.dp)
                )
            }
        }


        Spacer(modifier = Modifier.height(16.dp))

        Text("Select languages:", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(4.dp))

        allLanguages.forEach { lang ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = selectedLanguages.contains(lang),
                    onCheckedChange = {
                        if (it) selectedLanguages.add(lang) else selectedLanguages.remove(lang)
                        errorText = null
                    }
                )
                Text(lang)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            if (inputText.value.isBlank()) {
                errorText = "Please enter or speak some text."
                return@Button
            }
            if (selectedLanguages.isEmpty()) {
                errorText = "Please select at least one language."
                return@Button
            }

            coroutineScope.launch {
                val translator = Translator(context)
                result.value = translator.translateToSelected(inputText.value, selectedLanguages)
            }
        }) {
            Text("Translate")
        }

        errorText?.let {
            Spacer(modifier = Modifier.height(8.dp))
            Text(it, color = Color.Red)
        }

        result.value?.let {
            Spacer(modifier = Modifier.height(16.dp))
            Text("Translations:", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(4.dp))
            it.translations.forEach { (lang, translatedText) ->
                Text("$lang: $translatedText")
            }
        }
    }
}



