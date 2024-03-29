package com.s4a4.core.model

data class ItemAnimeModel(
    val previewImageUrl:String,
    val nameRu:String,
    val nameEng:String,
    val genre:List<String>,
    val ordinal:Int? = null,
    var voice:MutableList<VoiceModel>
)