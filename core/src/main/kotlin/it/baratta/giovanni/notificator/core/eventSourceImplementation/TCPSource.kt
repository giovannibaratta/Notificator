package it.baratta.giovanni.notificator.core.eventSourceImplementation

import io.reactivex.Observable
import it.baratta.giovanni.notificator.api.IEventSource
import it.baratta.giovanni.notificator.api.Message
import it.baratta.giovanni.notificator.api.request.ConfigurationParams

class TCPSource : IEventSource {

    override fun shutdown() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun initClient(clientToken: String, params: ConfigurationParams): Observable<Message> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun releaseClient(clientToken: String) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override val sourceName: String = "tcp"
}