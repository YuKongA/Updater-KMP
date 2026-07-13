package data.repository

import data.OtaMetadataPb

interface OtaMetadataFetcher {
    suspend fun getOtaMetadata(url: String): OtaMetadataPb?
}
