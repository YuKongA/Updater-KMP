package data

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonIgnoreUnknownKeys
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.addJsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray

object RomInfoHelper {
    @Serializable
    @JsonIgnoreUnknownKeys
    @OptIn(ExperimentalSerializationApi::class)
    data class RomInfo(
        @SerialName("AuthResult") val authResult: Int? = null,
        @SerialName("CurrentRom") val currentRom: Rom? = null,
        @SerialName("LatestRom") val latestRom: Rom? = null,
        @SerialName("IncrementRom") val incrementRom: Rom? = null,
        @SerialName("CrossRom") val crossRom: Rom? = null,
        @SerialName("Icon") val icon: Map<String, String>? = null,
        @SerialName("FileMirror") val fileMirror: FileMirror? = null,
        @SerialName("GentleNotice") val gentleNotice: GentleNotice? = null,
    )

    @Serializable
    @JsonIgnoreUnknownKeys
    @OptIn(ExperimentalSerializationApi::class)
    data class Rom(
        val bigversion: String? = null,
        val branch: String? = null,
        @Serializable(with = ChangelogSerializer::class)
        val changelog: HashMap<String, List<ChangelogItem>>? = null,
        val codebase: String? = null,
        val device: String? = null,
        val filename: String? = null,
        val filesize: String? = null,
        val md5: String? = null,
        val name: String? = null,
        val osbigversion: String? = null,
        val type: String? = null,
        val version: String? = null,
        val isBeta: Int = 0,
        val isGov: Int = 0,
    )

    @Serializable
    data class ChangelogItem(
        val txt: String,
        val image: List<ChangelogImage>? = null
    )

    @Serializable
    data class ChangelogImage(
        val path: String,
        val h: String,
        val w: String
    )

    @Serializable
    data class FileMirror(
        val icon: String,
        val image: String,
        val video: String,
        val headimage: String,
    )

    @Serializable
    data class GentleNotice(
        val text: String,
    )

    object ChangelogSerializer : KSerializer<HashMap<String, List<ChangelogItem>>> {
        override val descriptor: SerialDescriptor = buildClassSerialDescriptor("Changelog")

        override fun deserialize(decoder: Decoder): HashMap<String, List<ChangelogItem>> {
            val jsonElement = decoder.decodeSerializableValue(JsonElement.serializer())
            val result = HashMap<String, List<ChangelogItem>>()

            if (jsonElement !is JsonObject) return result

            jsonElement.forEach { (key, value) ->
                val items = when {
                    // 情况 1: { "txt": ["item1", "item2"] } - 旧格式
                    value is JsonObject && value.containsKey("txt") -> {
                        val txtElement = value["txt"]
                        when (txtElement) {
                            is JsonArray -> {
                                txtElement.mapNotNull {
                                    (it as? JsonPrimitive)?.content?.let { text ->
                                        ChangelogItem(text, null)
                                    }
                                }
                            }

                            else -> emptyList()
                        }
                    }
                    // 情况 2: [{ "txt": "item1", "image": [...] }, ...] - 新格式
                    value is JsonArray -> {
                        value.mapNotNull { element ->
                            try {
                                Json.decodeFromJsonElement<ChangelogItem>(element)
                            } catch (e: Exception) {
                                null
                            }
                        }
                    }

                    else -> emptyList()
                }
                if (items.isNotEmpty()) {
                    result[key] = items
                }
            }

            return result
        }

        override fun serialize(encoder: Encoder, value: HashMap<String, List<ChangelogItem>>) {
            val jsonObject = buildJsonObject {
                value.forEach { (key, items) ->
                    putJsonArray(key) {
                        items.forEach { item ->
                            addJsonObject {
                                put("txt", item.txt)
                                item.image?.let { images ->
                                    putJsonArray("image") {
                                        images.forEach { img ->
                                            addJsonObject {
                                                put("path", img.path)
                                                put("h", img.h)
                                                put("w", img.w)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            encoder.encodeSerializableValue(JsonObject.serializer(), jsonObject)
        }
    }
}