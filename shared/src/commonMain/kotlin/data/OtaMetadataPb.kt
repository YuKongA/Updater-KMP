package data

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class OtaMetadataPb(
    @ProtoNumber(1) val type: Int = 0,
    @ProtoNumber(2) val wipe: Boolean = false,
    @ProtoNumber(3) val downgrade: Boolean = false,
    @ProtoNumber(4) val propertyFiles: Map<String, String> = emptyMap(),
    @ProtoNumber(5) val precondition: DeviceState? = null,
    @ProtoNumber(6) val postcondition: DeviceState? = null,
    @ProtoNumber(7) val retrofitDynamicPartitions: Boolean = false,
    @ProtoNumber(8) val requiredCache: Long = 0L,
    @ProtoNumber(9) val splDowngrade: Boolean = false,
)

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class DeviceState(
    @ProtoNumber(1) val device: List<String> = emptyList(),
    @ProtoNumber(2) val build: List<String> = emptyList(),
    @ProtoNumber(3) val buildIncremental: String = "",
    @ProtoNumber(4) val timestamp: Long = 0L,
    @ProtoNumber(5) val sdkLevel: String = "",
    @ProtoNumber(6) val securityPatchLevel: String = "",
    @ProtoNumber(7) val partitionState: List<PartitionStatePb> = emptyList(),
)

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class PartitionStatePb(
    @ProtoNumber(1) val partitionName: String = "",
    @ProtoNumber(2) val device: List<String> = emptyList(),
    @ProtoNumber(3) val build: List<String> = emptyList(),
    @ProtoNumber(4) val version: String = "",
)
