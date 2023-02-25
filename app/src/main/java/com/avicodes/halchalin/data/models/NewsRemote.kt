package com.avicodes.halchalin.data.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class NewsRemote(
    val category: List<String>?,
    val content: String?,
    val country: List<String>?,
    val creator: List<String>?,
    val description: String?,
    val image_url: String?,
    val keywords: List<String>?,
    val language: String?,
    val link: String?,
    val pubDate: String?,
    val source_id: String?,
    val title: String?,
): Parcelable