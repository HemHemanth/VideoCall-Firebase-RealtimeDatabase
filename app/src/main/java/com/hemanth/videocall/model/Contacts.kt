package com.hemanth.videocall.model

data class Contacts(
    var name: String,
    var status: String,
    var image: String,
    var uid: String
) {
    constructor(): this("", "", "", "")
}
