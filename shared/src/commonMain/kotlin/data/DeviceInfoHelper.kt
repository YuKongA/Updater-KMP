package data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.Serializable
import utils.DeviceListUtils

object DeviceInfoHelper {
    @Serializable
    data class Device(
        val deviceName: String,
        val deviceCodeName: String,
        val deviceCode: String,
    )

    @Serializable
    data class RemoteDevices(
        val devices: List<Device>,
        val version: String,
    )

    data class Android(
        val androidVersionCode: String,
        val androidLetterCode: String,
    )

    data class Region(
        val regionCodeName: String,
        val regionCode: String,
        val regionName: String = regionCode,
    )

    data class Carrier(
        val carrierName: String,
        val carrierCode: String,
        val regionAppend: String = "",
    )

    /**
     * List of Xiaomi devices.
     *
     * For auto-completion of device designators and system version suffixes.
     *
     */
    private val embeddedDeviceList = listOf(
        Device("Xiaomi 15S Pro", "dijun", "OD"),
        Device("Xiaomi 15", "dada", "OC"),
        Device("Xiaomi 15 Pro", "haotian", "OB"),
        Device("Xiaomi 15 Ultra", "xuanyuan", "OA"),
    )

    /**
     * Current device list
     */
    private var currentDeviceList: List<Device> = embeddedDeviceList

    /**
     * Update the current device list with remote or embedded data based
     */
    suspend fun updateDeviceList() {
        currentDeviceList = DeviceListUtils.getDeviceList(embeddedDeviceList)
        rebuildMappings()
    }

    /**
     * Rebuild all mappings when device list is updated
     */
    private fun rebuildMappings() {
        _deviceNameToDeviceCodeName = currentDeviceList.associateBy({ it.deviceName }, { it.deviceCodeName })
        _deviceCodeNameToDeviceName = currentDeviceList.associateBy({ it.deviceCodeName }, { it.deviceName })
        _deviceNames = currentDeviceList.map { it.deviceName }
        _codeNames = currentDeviceList.map { it.deviceCodeName }
        _deviceNamesFlow.value = _deviceNames
        _codeNamesFlow.value = _codeNames
    }

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
        androidK,
    )


    private val CN = Region("", "CN", "Default (CN)")
    private val GL = Region("_global", "MI", "GL (MI)")
    private val EEA = Region("_eea_global", "EU", "EEA (EU)")
    private val CL = Region("_cl_global", "CL")
    private val GT = Region("_gt_global", "GT")
    private val ID = Region("_id_global", "ID")
    private val IN = Region("_in_global", "IN")
    private val JP = Region("_jp_global", "JP")
    private val KR = Region("_kr_global", "KR")
    private val LM = Region("_lm_global", "LM")
    private val MX = Region("_mx_global", "MX")
    private val RU = Region("_ru_global", "RU")
    private val TR = Region("_tr_global", "TR")
    private val TW = Region("_tw_global", "TW")
    private val ZA = Region("_za_global", "ZA")

    private val regionList = listOf(CN, GL, EEA, CL, GT, ID, IN, JP, KR, LM, MX, RU, TR, TW, ZA)

    private val XM = Carrier("Default (Xiaomi)", "XM")
    private val DM = Carrier("MiStore (Demo)", "DM")
    private val DC = Carrier("DeviceLockController", "DC", "_dc")
    private val AT = Carrier("AT&T", "AT", "_at")
    private val BY = Carrier("Bouygues", "BY", "_by")
    private val CR = Carrier("Claro", "CR", "_cr")
    private val EN = Carrier("Entel", "EN", "_en")
    private val HG = Carrier("3HK", "HG", "_hg")
    private val KD = Carrier("KDDI", "KD", "_kd")
    private val MS = Carrier("Movistar", "MS", "_ms")
    private val MT = Carrier("MTN", "MT", "_mt")
    private val OR = Carrier("Orange", "OR", "_or")
    private val SB = Carrier("SoftBank", "SB", "_ti")
    private val SF = Carrier("Altice France", "SF", "_sf")
    private val TF = Carrier("Telefónica", "TF", "_tf")
    private val TG = Carrier("Tigo", "TG", "_tg")
    private val TM = Carrier("TIM", "TI", "_tm")
    private val VC = Carrier("Vodacom", "VC", "_vc")
    private val VF = Carrier("Vodafone", "VF", "_vf")

    private val carrierList = listOf(XM, DM, DC, AT, BY, CR, EN, HG, KD, MS, MT, OR, SB, SF, TF, TG, TM, VC, VF)

    private var _deviceNameToDeviceCodeName = currentDeviceList.associateBy({ it.deviceName }, { it.deviceCodeName })
    private var _deviceCodeNameToDeviceName = currentDeviceList.associateBy({ it.deviceCodeName }, { it.deviceName })
    private var _deviceNames = currentDeviceList.map { it.deviceName }
    private var _codeNames = currentDeviceList.map { it.deviceCodeName }

    private val regionNameToRegionCode = regionList.associateBy({ it.regionName }, { it.regionCode })
    private val regionNameToRegionCodeName = regionList.associateBy({ it.regionName }, { it.regionCodeName })
    private val carrierNameToCarrierCode = carrierList.associateBy({ it.carrierName }, { it.carrierCode })
    private val carrierNameToCarrierCodeName = carrierList.associateBy({ it.carrierName }, { it.regionAppend })
    private val androidVersionCodeToAndroidLetterCode = androidList.associateBy { it.androidVersionCode }

    private val _deviceNamesFlow = MutableStateFlow(_deviceNames)
    val deviceNamesFlow = _deviceNamesFlow.asStateFlow()

    private val _codeNamesFlow = MutableStateFlow(_codeNames)
    val codeNamesFlow = _codeNamesFlow.asStateFlow()

    val regionNames = regionList.map { it.regionName }
    val carrierNames = carrierList.map { it.carrierName }
    val androidVersions = androidList.map { it.androidVersionCode }

    fun codeName(deviceName: String): String = _deviceNameToDeviceCodeName[deviceName] ?: ""
    fun deviceName(deviceCodeName: String): String = _deviceCodeNameToDeviceName[deviceCodeName] ?: ""
    fun regionCode(regionName: String): String = regionNameToRegionCode[regionName] ?: ""
    fun carrierCode(carrierName: String): String = carrierNameToCarrierCode[carrierName] ?: ""
    fun regionCodeName(regionName: String): String = regionNameToRegionCodeName[regionName] ?: ""
    fun carrierCodeName(carrierName: String): String = carrierNameToCarrierCodeName[carrierName] ?: ""

    fun deviceCode(androidVersionCode: String, codeName: String, regionCode: String, carrierCode: String): String {
        val android = androidVersionCodeToAndroidLetterCode[androidVersionCode] ?: return ""
        val device = currentDeviceList.find { it.deviceCodeName == codeName } ?: return ""
        return "${android.androidLetterCode}${device.deviceCode}${regionCode}${carrierCode}"
    }
}
