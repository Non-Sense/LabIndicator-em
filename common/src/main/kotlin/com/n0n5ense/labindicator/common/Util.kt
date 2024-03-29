package com.n0n5ense.labindicator.common

inline fun <T> T.runIf(predicate: (T) -> Boolean, block: () -> Unit) {
    if(predicate(this))
        block()
}

const val ChannelIdKeyName = "channelId"
const val ButtonMessageIdKeyName = "buttonMessageId"
const val ButtonChannelIdKeyName = "buttonChannelId"