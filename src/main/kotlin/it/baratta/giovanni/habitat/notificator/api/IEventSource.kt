package it.baratta.giovanni.habitat.notificator.api

import java.util.*

interface IEventSource{
    val sourceName : String
    fun registerClient(clientID : Int, rules : HashMap<String, String>)
    fun unregisterClient(clientID : Int)
}