package com.edutechnologic.industrialbadger.content.data

import com.edutechnologic.industrialbadger.content.contract.ContentContract
import ktx.sovereign.database.entity.Content
import ktx.sovereign.database.entity.Volume

sealed class ContentModuleState(
        val context: ContentContract.Ctx
) {
    data class Index (
            val volumes: List<Volume>,
            val content: List<Content>,
            val isSearching: Boolean = false
    ) : ContentModuleState(ContentContract.Ctx.INDEX)
    data class Training (
            val volumes: List<Volume>,
            val content: List<Content>,
            val isSearching: Boolean = false,
            val query: String? = null
    ) : ContentModuleState(ContentContract.Ctx.TRAINING)
    data class Details (
            val volume: Volume,
            val content: List<Content>,
            val index: Int = 0
    ) : ContentModuleState(ContentContract.Ctx.DETAILS)
    data class Error (val message: String) : ContentModuleState(ContentContract.Ctx.ERROR)
}