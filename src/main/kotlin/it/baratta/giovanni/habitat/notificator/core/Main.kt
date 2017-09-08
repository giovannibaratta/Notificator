package it.baratta.giovanni.habitat.notificator.core

import it.baratta.giovanni.habitat.notificator.core.network.tcp.RequestTCPSocket
import it.baratta.giovanni.habitat.notificator.core.notificatorImplementation.MqttNotificator
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.message.SimpleMessage

public fun main(args : Array<String>){

    val logger = LogManager.getLogger()
    logger.info{SimpleMessage("Avvio del servizio ...")}

    // Carico i servizi nel NotificatorBinder
    logger.info{SimpleMessage("Carico i moduli dei notificatori")}
    NotificatorBinder.instance.bindModule("service", MqttNotificator::class)

    // Avvio la socket dove ricevere le registrazioni
    RequestTCPSocket(/*Random().nextInt(50000)+1024*/2000).start()

    logger.info{SimpleMessage("Servizio avviato correttamente")}
}
