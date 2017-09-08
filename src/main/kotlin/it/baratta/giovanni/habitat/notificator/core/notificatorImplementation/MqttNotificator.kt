package it.baratta.giovanni.habitat.notificator.core.notificatorImplementation

import it.baratta.giovanni.habitat.notificator.api.INotificator
import java.io.Serializable

class MqttNotificator : INotificator {
    override fun notify(clientID: Int, payload: Serializable) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}