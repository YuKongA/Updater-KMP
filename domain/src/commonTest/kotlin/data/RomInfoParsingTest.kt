package data

import data.mapper.RomInfoMapper
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class RomInfoParsingTest {
    @Test
    fun os4FormattingFieldsPreserveTextImagesAndOrder() {
        val response = Json.decodeFromString<RomInfoHelper.RomInfo>(
            """
            {
              "LatestRom": {
                "bigversion": "0", "osbigversion": "4.0",
                "version": "OS4.0.2.0.XPBCNXM",
                "changelog": {
                  "基础体验": [
                    {"txt": "降负载、控内存，久用也流畅", "subTitle": 1,
                     "image": [{"path": "OS4.0/header.png", "h": "600", "w": "1110"}]},
                    {"txt": " "},
                    {"txt": "新增 负载精算技术", "bold": 1},
                    {"txt": "优化 系统负载调度", "list": 1},
                    {"txt": "优化 跨设备通知", "list": 1, "subTitle": 1}
                  ],
                  "其他": [{"txt": "更多内容", "list": 1}]
                }
              },
              "FileMirror": {"icon": "", "image": "https://example.com/", "video": "", "headimage": ""}
            }
            """.trimIndent()
        )
        val rom = assertNotNull(response.latestRom)
        val groups = assertNotNull(rom.changelog)
        assertEquals(listOf("基础体验", "其他"), groups.keys.toList())
        assertEquals(
            listOf("降负载、控内存，久用也流畅", " ", "新增 负载精算技术", "优化 系统负载调度", "优化 跨设备通知"),
            groups.getValue("基础体验").map { it.txt },
        )
        val (info, _, images) = RomInfoMapper.mapRom(response, rom)
        assertTrue(info.changelog.contains("新增 负载精算技术\n优化 系统负载调度\n优化 跨设备通知"))
        assertEquals("https://example.com/OS4.0/header.png", images.first().imageUrl)
        assertEquals(1110, images.first().imageWidth)
        assertEquals(600, images.first().imageHeight)
        assertEquals(listOf(false, false, true, false, false, false), images.map { it.isBold })
        assertEquals(listOf(false, false, false, true, true, true), images.map { it.isListItem })
        val roundTrip = Json.decodeFromString<RomInfoHelper.Rom>(Json.encodeToString(rom))
        assertEquals(1, assertNotNull(roundTrip.changelog).getValue("基础体验")[2].bold)
        assertEquals(1, assertNotNull(roundTrip.changelog).getValue("基础体验")[3].list)
    }

    @Test
    fun legacyTextArraysRemainSupported() {
        val rom = Json.decodeFromString<RomInfoHelper.Rom>(
            """{"changelog":{"系统":{"txt":["优化 系统稳定性","修复 已知问题"]},"安全":{"txt":["更新 安全补丁"]}}}"""
        )
        val groups = assertNotNull(rom.changelog)
        assertEquals(listOf("系统", "安全"), groups.keys.toList())
        assertEquals(listOf("优化 系统稳定性", "修复 已知问题"), groups.getValue("系统").map { it.txt })
        assertEquals("更新 安全补丁", groups.getValue("安全").single().txt)
        assertTrue(groups.values.flatten().all { it.bold == 0 })
        assertTrue(groups.values.flatten().all { it.list == 0 })
    }

    @Test
    fun os3ImageCardsRemainSupported() {
        val rom = Json.decodeFromString<RomInfoHelper.Rom>(
            """
            {"changelog":{"系统":[
              {"txt":"优化 动画","image":[{"path":"OS3.0/item.png","h":"600","w":"1110"}],"package":"com.example.app","versionCode":"123"},
              {"txt":"修复 已知问题"}
            ]}}
            """.trimIndent()
        )
        val items = assertNotNull(rom.changelog).getValue("系统")
        assertEquals(listOf("优化 动画", "修复 已知问题"), items.map { it.txt })
        assertEquals("OS3.0/item.png", items.first().image?.single()?.path)
        assertEquals("com.example.app", items.first().packageName)
        assertEquals("123", items.first().versionCode)
        assertTrue(items.all { it.bold == 0 })
        assertTrue(items.all { it.list == 0 })
    }

    @Test
    fun xmsCardsWithAdditionalFieldsRemainVisible() {
        val response = Json.decodeFromString<RomInfoHelper.RomInfo>(
            """
            {"xmsUpdateInfo":{"hasXmsUpdate":1,"changeLog":{"应用":[
              {"txt":"优化 应用体验","bold":1,"list":1,"packageName":"com.example.app","versionName":"1.0","video":["intro.mp4"],"cover":["cover.png"]}
            ]}}}
            """.trimIndent()
        )
        val xms = assertNotNull(response.xmsUpdateInfo)
        assertEquals("优化 应用体验", assertNotNull(xms.changeLog).getValue("应用").single().txt)
        assertTrue(RomInfoMapper.mapXmsInfo(xms, "").changelogText.contains("优化 应用体验"))
        assertTrue(RomInfoMapper.mapXmsInfo(xms, "").changelogItems.single().isBold)
        assertTrue(RomInfoMapper.mapXmsInfo(xms, "").changelogItems.single().isListItem)
    }
}
