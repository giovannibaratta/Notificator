package it.baratta.giovanni.habitat.notificator.core.eventSourceImplementation

import io.reactivex.Observable
import it.baratta.giovanni.habitat.notificator.api.request.ConfigurationParams
import it.baratta.giovanni.habitat.notificator.api.IEventSource
import it.baratta.giovanni.habitat.notificator.api.Message
import java.io.Serializable

class SEPASource : IEventSource {

    companion object {
        val sourceName = "SEPA"
    }

    override fun registerClient(clientToken: String, params: ConfigurationParams): Observable<Message> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun unregisterClient(clientToken: String) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override val sourceName: String = "sepa"
}