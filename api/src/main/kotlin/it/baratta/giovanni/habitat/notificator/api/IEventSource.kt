package it.baratta.giovanni.habitat.notificator.api

import io.reactivex.Observable
import java.io.Serializable
import java.util.*

interface IEventSource{
    fun registerClient(clientToken : String, params: ConfigurationParams) : Observable<Serializable>
    fun unregisterClient(clientToken : String)
}