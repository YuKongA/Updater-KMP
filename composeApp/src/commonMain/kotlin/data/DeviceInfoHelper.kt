package data

object DeviceInfoHelper {

    data class Android(
        val androidVersionCode: String,
        val androidLetterCode: String
    )

    data class Region(
        val regionCodeName: String,
        val regionCode: String,
        val regionName: String = regionCode
    )

    data class Device(
        val deviceName: String,
        val deviceCodeName: String,
        val deviceCode: String
    )

    /**
     * List of Xiaomi devices.
     *
     * For auto-completion of device designators and system version suffixes.
     *
     */
    private val deviceList = listOf(
        Device("Redmi K30 4G", "phoenix", "GH"),
        Device("POCO X2 4G", "phoenixin", "GH"),
        Device("Redmi A4 5G / POCO C75 5G", "warm", "GV"),
        Device("Redmi A5 / POCO C71", "serenity", "GW"),
        Device("Redmi K30 / K30i", "picasso", "GI"),
        Device("Redmi Pad SE 8.7 4G", "spark", "HY"),
        Device("Redmi Pad SE 8.7 Wi-Fi", "flare", "HX"),
        Device("Xiaomi 10", "umi", "JB"),
        Device("Xiaomi 10 Pro", "cmi", "JA"),
        Device("Redmi Note 9 Pro", "joyeuse", "JZ"),
        Device("Redmi Note 9 Pro (India) / Note 9S / Note 10 Lite", "curtana", "JW"),
        Device("Redmi Note 9 Pro Max", "excalibur", "JX"),
        Device("Redmi K30 Pro", "lmi", "JK"),
        Device("Xiaomi 10 Lite", "monet", "JI"),
        Device("Xiaomi 10 Lite (China)", "vangogh", "JV"),
        Device("Redmi Note 9 / 10X 4G", "merlin", "JO"),
        Device("Redmi 10X", "atom", "JH"),
        Device("Redmi 10X Pro", "bomb", "JL"),
        Device("Xiaomi Note 10 Lite ", "toco", "FN"),
        Device("Redmi 9 / 9 Prime", "lancelot", "JC"),
        Device("POCO M2 Pro", "gram", "JP"),
        Device("Redmi K30 Ultra", "cezanne", "JN"),
        Device("Xiaomi 10 Ultra", "cas", "JJ"),
        Device("POCO X3 NFC", "surya", "JG"),
        Device("Xiaomi 10T / 10T Pro / Redmi K30S Ultra", "apollo", "JD"),
        Device("POCO M3", "citrus", "JF"),
        Device("Redmi 9T / 9 Power / Note 9 4G", "lime", "JQ"),
        Device("Redmi Note 9", "cannon", "JE"),
        Device("Xiaomi 10T Lite / 10i / Redmi Note 9 Pro", "gauguin", "JS"),
        Device("Redmi Note 9T", "cannong", "JE"),
        Device("Xiaomi 11", "venus", "KB"),
        Device("Redmi K40 / POCO F3", "alioth", "KH"),
        Device("Xiaomi 11i / Redmi K40 Pro/Pro+", "haydn", "KK"),
        Device("Redmi Note 10", "mojito", "KG"),
        Device("Redmi Note 10S / POCO M5s", "rosemary", "KL"),
        Device("Redmi Note 10 (Global) / Note 10T / POCO M3 Pro", "camellian", "KS"),
        Device("Redmi Note 10 Pro", "sweet", "KF"),
        Device("Redmi Note 12 Pro 4G", "sweet_k6a", "HG"),
        Device("Redmi Note 10 Pro (India) / Pro Max", "sweetin", "KF"),
        Device("Xiaomi 10S", "thyme", "GA"),
        Device("POCO X3 Pro", "vayu", "JU"),
        Device("Xiaomi 11 Lite 4G", "courbet", "KQ"),
        Device("Xiaomi 11 Ultra / Pro", "star", "KA"),
        Device("Xiaomi 11 Lite", "renoir", "KI"),
        Device("Xiaomi MIX Fold", "cetus", "JT"),
        Device("Redmi K40 Gaming / POCO F3 GT", "ares", "KJ"),
        Device("Redmi Note 8 (2021)", "biloba", "CU"),
        Device("Redmi Note 10 / Note 11SE / Note 10T / POCO M3 Pro", "camellia", "KS"),
        Device("Redmi Note 10 Pro (China) / POCO X3 GT", "chopin", "KP"),
        Device("Xiaomi Pad 5", "nabu", "KX"),
        Device("Xiaomi Pad 5 Pro Wi-Fi", "elish", "KY"),
        Device("Xiaomi Pad 5 Pro 5G", "enuma", "KZ"),
        Device("Xiaomi Pad 5 Pro 12.4", "dagu", "LZ"),
        Device("Xiaomi MIX 4", "odin", "KM"),
        Device("Redmi 10 / 10 Prime / Note 11 4G", "selene", "KU"),
        Device("Xiaomi 11T Pro", "vili", "KD"),
        Device("Xiaomi 11T", "agate", "KW"),
        Device("Xiaomi 11 LE / 11 Lite NE", "lisa", "KO"),
        Device("Xiaomi Civi", "mona", "KV"),
        Device("Redmi Note 11 / Note 11T", "evergo", "GB"),
        Device("Xiaomi 11i / Redmi Note 11 Pro/Pro+", "pissarro", "KT"),
        Device("POCO M4 Pro", "evergreen", "GB"),
        Device("Xiaomi 12", "cupid", "LC"),
        Device("Xiaomi 12X", "psyche", "LD"),
        Device("Xiaomi 12 Pro", "zeus", "LB"),
        Device("Redmi Note 11S 4G / POCO M4 Pro 4G", "fleur", "KE"),
        Device("Redmi Note 11S", "opal", "GL"),
        Device("Redmi Note 11E Pro / Note 11 Pro / POCO X4 Pro", "veux", "KC"),
        Device("Redmi Note 11", "spes", "GC"),
        Device("Redmi Note 11 NFC", "spesn", "GK"),
        Device("Redmi K50G / POCO F4 GT", "ingres", "LJ"),
        Device("Redmi 10 / 11 Prime / Note 11E / POCO M4", "light", "LS"),
        Device("Redmi Note 11R", "lightcm", "LS"),
        Device("Redmi Note 11 Pro 4G", "viva", "GD"),
        Device("Redmi Note 11 Pro 4G (India)", "vida", "GD"),
        Device("Redmi K40S / POCO F4", "munch", "LM"),
        Device("Redmi K50 Pro", "matisse", "LK"),
        Device("Redmi K50", "rubens", "LN"),
        Device("Redmi 10C", "fog", "GE"),
        Device("Redmi 9A / 9i / 9AT / 10A", "dandelion", "CD"),
        Device("Redmi Note 10T", "lilac", "KN"),
        Device("Xiaomi Civi 1S", "zijin", "LP"),
        Device("Redmi Note 11T Pro/Pro+ / POCO X4 GT / Redmi K50i", "xaga", "LO"),
        Device("POCO C40", "frost", "GF"),
        Device("Xiaomi 12 Lite", "taoyao", "LI"),
        Device("Xiaomi 12 Pro Dimensity", "daumier", "LG"),
        Device("Xiaomi 12S Pro", "unicorn", "LE"),
        Device("Xiaomi 12S", "mayfly", "LT"),
        Device("Xiaomi 12S Ultra", "thor", "LA"),
        Device("Xiaomi MIX Fold 2", "zizhan", "LR"),
        Device("Xiaomi 12T Pro / Redmi K50 Ultra", "diting", "LF"),
        Device("Redmi 11 Prime 4G / POCO M5", "rock", "LU"),
        Device("Redmi A1 / POCO C50", "ice", "GM"),
        Device("Xiaomi 13 Lite / Civi 2 ", "ziyi", "LL"),
        Device("Redmi Pad", "yunluo", "LY"),
        Device("Xiaomi 12T", "plato", "LQ"),
        Device("Redmi Note 12 Pro", "ruby", "MO"),
        Device("Redmi Note 12 / Note 12R Pro", "sunstone", "MQ"),
        Device("POCO X5", "moonstone", "MP"),
        Device("Xiaomi 13", "fuxi", "MC"),
        Device("Xiaomi 13 Pro", "nuwa", "MB"),
        Device("Redmi K60 / POCO F5 Pro", "mondrian", "MN"),
        Device("Redmi K60 Pro", "socrates", "MK"),
        Device("Redmi K60E", "rembrandt", "MM"),
        Device("Redmi Note 12 Pro Speed / POCO X5 Pro", "redwood", "MS"),
        Device("Redmi 12C / POCO C55", "earth", "CV"),
        Device("Redmi A2/A2+ / POCO C51", "water", "GO"),
        Device("Redmi A3", "blue", "GR"),
        Device("Redmi A3x", "klein", "GS"),
        Device("Redmi Note 12 Turbo / POCO F5", "marble", "MR"),
        Device("Redmi Note 12 4G", "tapas", "MT"),
        Device("Redmi Note 12 4G NFC", "topaz", "MG"),
        Device("Xiaomi Pad 6", "pipa", "MZ"),
        Device("Xiaomi Pad 6 Pro", "liuqin", "MY"),
        Device("Xiaomi 13 Ultra", "ishtar", "MA"),
        Device("Redmi Note 12S", "sea", "HZ"),
        Device("Xiaomi Civi 3", "yuechu", "MI"),
        Device("Redmi Note 12T Pro", "pearl", "LH"),
        Device("Redmi 12", "fire", "MX"),
        Device("Redmi Note 12 / Note 12R / POCO M6 Pro", "sky", "MW"),
        Device("Xiaomi Pad 6 Max 14", "yudi", "MH"),
        Device("Xiaomi 13T Pro / Redmi K60 Ultra", "corot", "ML"),
        Device("Xiaomi MIX Fold 3", "babylon", "MV"),
        Device("Redmi Pad SE", "xun", "MU"),
        Device("Redmi Note 13 4G", "sapphire", "NG"),
        Device("Redmi Note 13 NFC", "sapphiren", "NH"),
        Device("Redmi Note 13 Pro+", "zircon", "NO"),
        Device("Redmi Note 13 / 13R Pro / POCO X6 Neo", "gold", "NQ"),
        Device("Redmi Note 13 Pro / POCO X6", "garnet", "NR"),
        Device("Redmi Note 13 Pro (Global)", "emerald", "NF"),
        Device("Redmi Note 14 4G", "tanzanite", "OG"),
        Device("Redmi Note 14 / POCO M7 Pro", "beryl", "OQ"),
        Device("Redmi Note 14S", "emerald_r", "FO"),
        Device("Redmi Note 14 Pro 4G", "obsidian", "OF"),
        Device("Redmi Note 14 Pro / POCO X7", "malachite", "OO"),
        Device("Redmi Note 14 Pro+", "amethyst", "OP"),
        Device("Redmi Turbo 3 / POCO F6", "peridot", "NP"),
        Device("REDMI Turbo 4 / POCO X7 Pro", "rodin", "OJ"),
        Device("REDMI Turbo 4 Pro / POCO F7", "onyx", "OL"),
        Device("Xiaomi 13T", "aristotle", "MF"),
        Device("Xiaomi 14", "houji", "NC"),
        Device("Xiaomi 14 Pro", "shennong", "NB"),
        Device("Xiaomi 14 Pro Ti Satellite", "shennong_t", "ND"),
        Device("Xiaomi 14 Civi / Xiaomi Civi 4 Pro", "chenfeng", "NJ"),
        Device("Xiaomi 14T", "degas", "NE"),
        Device("Redmi 13 / POCO M6 4G", "moon", "NT"),
        Device("Redmi 13C / POCO C65 (India)", "gale", "GP"),
        Device("Redmi 13C (China) / 13R / POCO M6", "air", "GQ"),
        Device("Redmi 14C", "lake", "GT"),
        Device("Redmi 14R 5G", "flame", "GU"),
        Device("Redmi K70 / POCO F6 Pro", "vermeer", "NK"),
        Device("Redmi K70 Pro", "manet", "NM"),
        Device("Redmi K70E / POCO X6 Pro", "duchamp", "NL"),
        Device("REDMI K80", "zorn", "OK"),
        Device("REDMI K80 Pro", "miro", "OM"),
        Device("Xiaomi 14 Ultra", "aurora", "NA"),
        Device("Xiaomi Pad 6S Pro", "sheng", "NX"),
        Device("Redmi Pad Pro Wi-Fi", "dizi", "NS"),
        Device("Redmi Pad Pro 5G", "ruan", "FS"),
        Device("Redmi Note 13R / POCO M6 Plus", "breeze", "NU"),
        Device("Redmi K70 Ultra / Xiaomi 14T Pro", "rothko", "NN"),
        Device("Xiaomi MIX Fold 4", "goku", "NV"),
        Device("Xiaomi MIX Flip", "ruyi", "NI"),
        Device("Xiaomi 15", "dada", "OC"),
        Device("Xiaomi 15 Pro", "haotian", "OB"),
        Device("Xiaomi 15 Ultra", "xuanyuan", "OA"),
        Device("Xiaomi Pad 7", "uke", "OZ"),
        Device("Xiaomi Pad 7 Pro", "muyu", "OY"),
    )

    private const val XIAOMI = "XM"

    private val androidW = Android("16.0", "W")
    private val androidV = Android("15.0", "V")
    private val androidU = Android("14.0", "U")
    private val androidT = Android("13.0", "T")
    private val androidS = Android("12.0", "S")
    private val androidR = Android("11.0", "R")
    private val androidQ = Android("10.0", "Q")
    private val androidP = Android("9.0", "P")
    private val androidOMr1 = Android("8.1", "O")
    private val androidO = Android("8.0", "O")
    private val androidNMr1 = Android("7.1", "N")
    private val androidN = Android("7.0", "N")
    private val androidM = Android("6.0", "M")
    private val androidLMr1 = Android("5.1", "L")
    private val androidL = Android("5.0", "L")
    private val androidK = Android("4.4", "K")

    private val androidList = listOf(
        androidW,
        androidV,
        androidU,
        androidT,
        androidS,
        androidR,
        androidQ,
        androidP,
        androidOMr1,
        androidO,
        androidNMr1,
        androidN,
        androidM,
        androidLMr1,
        androidL,
        androidK
    )

    private val CN = Region("", "CN")
    private val GL = Region("_global", "MI", "GL")
    private val EEA = Region("_eea_global", "EU", "EEA")
    private val RU = Region("_ru_global", "RU")
    private val TW = Region("_tw_global", "TW")
    private val ID = Region("_id_global", "ID")
    private val TR = Region("_tr_global", "TR")
    private val IN = Region("_in_global", "IN")
    private val JP = Region("_jp_global", "JP")
    private val KR = Region("_kr_global", "KR")

    private val regionList = listOf(CN, GL, EEA, RU, TW, ID, TR, IN, JP, KR)

    private val deviceNameToDeviceCodeName = deviceList.associateBy({ it.deviceName }, { it.deviceCodeName })
    private val deviceCodeNameToDeviceName = deviceList.associateBy({ it.deviceCodeName }, { it.deviceName })
    private val regionNameToRegionCode = regionList.associateBy({ it.regionName }, { it.regionCode })
    private val regionNameToRegionCodeName = regionList.associateBy({ it.regionName }, { it.regionCodeName })
    private val androidVersionCodeToAndroidLetterCode = androidList.associateBy { it.androidVersionCode }

    val deviceNames = deviceList.map { it.deviceName }
    val codeNames = deviceList.map { it.deviceCodeName }
    val regionNames = regionList.map { it.regionName }
    val androidVersions = androidList.map { it.androidVersionCode }

    fun codeName(deviceName: String): String = deviceNameToDeviceCodeName[deviceName] ?: ""
    fun deviceName(deviceCodeName: String): String = deviceCodeNameToDeviceName[deviceCodeName] ?: ""
    fun regionCode(regionName: String): String = regionNameToRegionCode[regionName] ?: ""
    fun regionCodeName(regionName: String): String = regionNameToRegionCodeName[regionName] ?: ""


    fun deviceCode(androidVersionCode: String, codeName: String, regionCode: String): String {
        val android = androidVersionCodeToAndroidLetterCode[androidVersionCode] ?: return ""
        val device = deviceList.find { it.deviceCodeName == codeName } ?: return ""
        return "${android.androidLetterCode}${device.deviceCode}${regionCode}$XIAOMI"
    }
}
