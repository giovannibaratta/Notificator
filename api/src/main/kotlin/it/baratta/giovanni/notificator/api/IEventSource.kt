package it.baratta.giovanni.notificator.api

import io.reactivex.Observable
import it.baratta.giovanni.notificator.api.request.ConfigurationParams

interface IEventSource{
    fun registerClient(clientToken : String, params: ConfigurationParams) : Observable<Message>
    fun unregisterClient(clientToken : String)
    val sourceName : String
    fun shutdown()
}