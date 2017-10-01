package it.baratta.giovanni.notificator.core.network.tcp

import java.io.InputStream

class TCPHelper {

    companion object
}

fun InputStream.readBytes(len : Int) : ByteArray{

    val buffer = ByteArray(len)
    var count = 0

    while(count < len)
        count += this.read(buffer, count, len - count)

    return buffer
}