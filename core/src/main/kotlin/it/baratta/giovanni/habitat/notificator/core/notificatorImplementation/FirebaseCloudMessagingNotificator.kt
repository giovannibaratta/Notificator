package it.baratta.giovanni.habitat.notificator.core.notificatorImplementation

import it.baratta.giovanni.habitat.notificator.api.INotificator
import it.baratta.giovanni.habitat.notificator.api.Message
import it.baratta.giovanni.habitat.notificator.api.request.ConfigurationParams
import java.io.Serializable

class FirebaseCloudMessagingNotificator : INotificator {
    override fun initNotifcator(clientToken: String, params: ConfigurationParams): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun destroyNotificator(clientToken: String) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun notify(clientToken: String, message: Message) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override val notificatorName: String = "fcm"
}