package it.baratta.giovanni.habitat.notificator

import org.apache.logging.log4j.LogManager
import java.io.Serializable

public fun main(args : Array<String>){
        //setup logger
        // Carico le impostazioni dal file di configurazione

        // Carico i servizi nel NotificatorBinder
        NotificatorBinder.instance.bindModule("service",E::class)
        val logger = LogManager.getLogger()
    logger.info("ciao")
    logger.error("test")
    //NotificatorBinder.instance.bindModule("service",E::class)
    }

class E : INotificator{
    override fun notify(clientID: Int, payload: Serializable) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}