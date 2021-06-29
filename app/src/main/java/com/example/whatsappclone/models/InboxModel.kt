package com.example.whatsappclone.models

import java.util.*

class InboxModel (val msg: String, var from: String, var name: String,
                  var image: String, var count: Int, val time: Date = Date()){

    constructor(): this("", "", "", "", 0, Date())
}