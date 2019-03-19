package com.abc.demomapkotlin.drawDirection

class Duration {
    lateinit var text: String
    var value: Int = 0

    constructor(text: String, value: Int) {
        this.text = text
        this.value = value
    }
}