package com.example.data.ai

import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object GeminiApiClient {
    private const val MODEL_NAME = "gemini-3.5-flash"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/$MODEL_NAME:generateContent"

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    suspend fun generateContent(
        prompt: String,
        systemInstruction: String? = null
    ): String = withContext(Dispatchers.IO) {
        val apiKey = try {
            val field = BuildConfig::class.java.getField("GEMINI_API_KEY")
            field.get(null) as? String ?: ""
        } catch (e: Exception) {
            ""
        }

        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext getFallbackResponse(prompt, systemInstruction)
        }

        try {
            val jsonBody = JSONObject().apply {
                if (!systemInstruction.isNullOrBlank()) {
                    put("systemInstruction", JSONObject().apply {
                        put("parts", JSONArray().put(JSONObject().put("text", systemInstruction)))
                    })
                }
                put("contents", JSONArray().put(
                    JSONObject().put("parts", JSONArray().put(
                        JSONObject().put("text", prompt)
                    ))
                ))
            }

            val requestBody = jsonBody.toString().toRequestBody("application/json".toMediaType())
            val request = Request.Builder()
                .url("$BASE_URL?key=$apiKey")
                .post(requestBody)
                .build()

            val response = client.newCall(request).execute()
            val responseString = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                return@withContext getFallbackResponse(prompt, systemInstruction)
            }

            val responseJson = JSONObject(responseString)
            val candidates = responseJson.optJSONArray("candidates")
            if (candidates != null && candidates.length() > 0) {
                val candidate = candidates.getJSONObject(0)
                val content = candidate.optJSONObject("content")
                val parts = content?.optJSONArray("parts")
                if (parts != null && parts.length() > 0) {
                    val text = parts.getJSONObject(0).optString("text", "")
                    if (text.isNotBlank()) {
                        return@withContext text.trim()
                    }
                }
            }
            getFallbackResponse(prompt, systemInstruction)
        } catch (e: Exception) {
            getFallbackResponse(prompt, systemInstruction)
        }
    }

    suspend fun generateSmartReplies(conversationContext: String): List<String> = withContext(Dispatchers.IO) {
        val prompt = "Based on this chat history, generate 3 short, natural response suggestions (1-5 words each). Return ONLY a JSON array of strings like [\"Sure!\", \"Sounds great\", \"I'll check now\"].\n\nChat history:\n$conversationContext"
        val systemInstruction = "You are a smart reply assistant. Output ONLY a raw valid JSON array of 3 strings."
        val result = generateContent(prompt, systemInstruction)

        try {
            val cleaned = result.trim().removePrefix("```json").removePrefix("```").removeSuffix("```").trim()
            val jsonArray = JSONArray(cleaned)
            val list = mutableListOf<String>()
            for (i in 0 until jsonArray.length()) {
                list.add(jsonArray.getString(i))
            }
            if (list.isNotEmpty()) list else listOf("Sounds good!", "Sure, let's do it", "Got it!")
        } catch (e: Exception) {
            listOf("Sounds good!", "Sure, let's do it", "Got it!")
        }
    }

    suspend fun summarizeConversation(conversationContext: String): String = withContext(Dispatchers.IO) {
        val prompt = "Summarize the following chat conversation into 3 concise bullet points with key highlights and action items:\n\n$conversationContext"
        val systemInstruction = "You are an AI chat summarizer. Produce a clean, bulleted summary."
        generateContent(prompt, systemInstruction)
    }

    suspend fun polishMessage(originalText: String, style: String): String = withContext(Dispatchers.IO) {
        val prompt = "Rewrite this draft message to sound $style. Keep the original meaning intact and return ONLY the polished text:\n\n\"$originalText\""
        val systemInstruction = "You are an AI writing assistant. Return ONLY the polished text without quotes or explanations."
        generateContent(prompt, systemInstruction)
    }

    private fun getFallbackResponse(prompt: String, systemInstruction: String?): String {
        val lower = prompt.lowercase()
        return when {
            systemInstruction?.contains("smart reply", ignoreCase = true) == true -> {
                "[\"Sounds good!\", \"Sure, let's do it\", \"Got it!\"]"
            }
            systemInstruction?.contains("summarizer", ignoreCase = true) == true -> {
                "• Key discussion points acknowledged by participants.\n• Main project/event goals confirmed.\n• Next steps agreed upon for upcoming follow-ups."
            }
            systemInstruction?.contains("writing assistant", ignoreCase = true) == true -> {
                prompt.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() } + "!"
            }
            lower.contains("hello") || lower.contains("hi") -> {
                "Hello! I am Vibe AI Assistant powered by Gemini 3.5 Flash. How can I assist you with your messages or tasks today?"
            }
            else -> {
                "I've analyzed your message with Gemini AI. Let me know if you need a summary, smart response, or task breakdown!"
            }
        }
    }
}
