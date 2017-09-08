package it.baratta.giovanni.habitat.notificator.core.eventSourceImplementation

import it.baratta.giovanni.habitat.notificator.api.IEventSource
import java.util.HashMap

class TCPSource : IEventSource{

    companion object {
        val sourceName = "TCP"
    }

    override val sourceName: String = TCPSource.sourceName

    override fun registerClient(clientID: Int, rules: HashMap<String, String>) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun unregisterClient(clientID: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}