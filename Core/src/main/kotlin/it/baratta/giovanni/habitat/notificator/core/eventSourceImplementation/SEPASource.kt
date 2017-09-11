package it.baratta.giovanni.habitat.notificator.core.eventSourceImplementation

import io.reactivex.Observable
import it.baratta.giovanni.habitat.api.ConfigurationParams
import it.baratta.giovanni.habitat.api.IEventSource
import java.io.Serializable

class SEPASource : IEventSource{

    companion object {
        val sourceName = "SEPA"
    }

    override fun registerClient(clientToken: String, params: ConfigurationParams): Observable<Serializable> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun unregisterClient(clientToken: String) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}