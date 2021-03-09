package com.hemanth.videocall.model

data class Chat(
    val sender: String,
    val receiver: String,
    val message: String
) {
    constructor(): this("", "", "")
}
