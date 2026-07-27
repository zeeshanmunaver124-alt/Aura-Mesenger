package com.example.data.models

import org.json.JSONArray
import org.json.JSONObject

data class PollOptionItem(
    val id: String,
    val text: String,
    val voterUserIds: List<String> = emptyList()
)

data class PollData(
    val question: String,
    val options: List<PollOptionItem>,
    val allowMultiple: Boolean = false,
    val isClosed: Boolean = false,
    val creatorName: String = ""
) {
    val totalVotes: Int get() = options.sumOf { it.voterUserIds.size }

    fun toJson(): String {
        val json = JSONObject()
        json.put("question", question)
        json.put("allowMultiple", allowMultiple)
        json.put("isClosed", isClosed)
        json.put("creatorName", creatorName)
        val optsArray = JSONArray()
        options.forEach { opt ->
            val optObj = JSONObject()
            optObj.put("id", opt.id)
            optObj.put("text", opt.text)
            val votersArray = JSONArray()
            opt.voterUserIds.forEach { votersArray.put(it) }
            optObj.put("voters", votersArray)
            optsArray.put(optObj)
        }
        json.put("options", optsArray)
        return json.toString()
    }

    companion object {
        fun fromJson(jsonStr: String?): PollData? {
            if (jsonStr.isNullOrBlank()) return null
            return try {
                val json = JSONObject(jsonStr)
                val question = json.optString("question", "")
                val allowMultiple = json.optBoolean("allowMultiple", false)
                val isClosed = json.optBoolean("isClosed", false)
                val creatorName = json.optString("creatorName", "")
                val optsArray = json.optJSONArray("options") ?: JSONArray()
                val options = mutableListOf<PollOptionItem>()
                for (i in 0 until optsArray.length()) {
                    val optObj = optsArray.getJSONObject(i)
                    val id = optObj.optString("id", "")
                    val text = optObj.optString("text", "")
                    val votersArr = optObj.optJSONArray("voters") ?: JSONArray()
                    val voters = mutableListOf<String>()
                    for (j in 0 until votersArr.length()) {
                        voters.add(votersArr.getString(j))
                    }
                    options.add(PollOptionItem(id, text, voters))
                }
                PollData(question, options, allowMultiple, isClosed, creatorName)
            } catch (e: Exception) {
                null
            }
        }
    }
}

data class EventData(
    val title: String,
    val description: String = "",
    val dateTime: String,
    val location: String = "",
    val organizerName: String = "",
    val goingUserIds: List<String> = emptyList(),
    val maybeUserIds: List<String> = emptyList(),
    val declinedUserIds: List<String> = emptyList()
) {
    fun toJson(): String {
        val json = JSONObject()
        json.put("title", title)
        json.put("description", description)
        json.put("dateTime", dateTime)
        json.put("location", location)
        json.put("organizerName", organizerName)

        val goingArr = JSONArray()
        goingUserIds.forEach { goingArr.put(it) }
        json.put("going", goingArr)

        val maybeArr = JSONArray()
        maybeUserIds.forEach { maybeArr.put(it) }
        json.put("maybe", maybeArr)

        val declinedArr = JSONArray()
        declinedUserIds.forEach { declinedArr.put(it) }
        json.put("declined", declinedArr)

        return json.toString()
    }

    companion object {
        fun fromJson(jsonStr: String?): EventData? {
            if (jsonStr.isNullOrBlank()) return null
            return try {
                val json = JSONObject(jsonStr)
                val title = json.optString("title", "")
                val description = json.optString("description", "")
                val dateTime = json.optString("dateTime", "")
                val location = json.optString("location", "")
                val organizerName = json.optString("organizerName", "")

                fun parseList(key: String): List<String> {
                    val arr = json.optJSONArray(key) ?: return emptyList()
                    val list = mutableListOf<String>()
                    for (i in 0 until arr.length()) {
                        list.add(arr.getString(i))
                    }
                    return list
                }

                EventData(
                    title = title,
                    description = description,
                    dateTime = dateTime,
                    location = location,
                    organizerName = organizerName,
                    goingUserIds = parseList("going"),
                    maybeUserIds = parseList("maybe"),
                    declinedUserIds = parseList("declined")
                )
            } catch (e: Exception) {
                null
            }
        }
    }
}
