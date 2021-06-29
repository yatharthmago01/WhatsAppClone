package com.example.whatsappclone.models

import android.content.Context
import com.example.whatsappclone.utils.formatAsHeader
import java.util.*

interface ChatEvent {
    val sentAt: Date
}

class Message(val msg: String, val senderId: String, val msgId: String, val type: String = "TEXT",
               var status: Int = 1, var liked: Boolean = false, override val sentAt: Date = Date()
): ChatEvent{
    constructor(): this("", "", "", "", 1, false, Date())
}

data class DateHeader(override val sentAt: Date = Date(), val context: Context): ChatEvent{
    val date: String = sentAt.formatAsHeader(context)
}