package it.baratta.giovanni.habitat.notificator.api

import io.reactivex.Observable
import it.baratta.giovanni.habitat.notificator.api.request.ConfigurationParams
import java.io.Serializable

interface IEventSource{
    fun registerClient(clientToken : String, params: ConfigurationParams) : Observable<Serializable>
    fun unregisterClient(clientToken : String)
}