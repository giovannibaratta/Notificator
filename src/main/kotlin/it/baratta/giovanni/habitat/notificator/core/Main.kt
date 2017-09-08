package it.baratta.giovanni.habitat.notificator.core

import it.baratta.giovanni.habitat.notificator.core.network.tcp.RequestTCPSocket
import it.baratta.giovanni.habitat.notificator.core.notificatorImplementation.MqttNotificator
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.message.SimpleMessage
import java.util.*

fun main(args : Array<String>){

    val logger = LogManager.getLogger()
    logger.info{SimpleMessage("Avvio del servizio ...")}

    // Carico i servizi nel NotificatorBinder
    logger.info{SimpleMessage("Carico i moduli dei notificatori")}
    NotificatorBinder.instance.bindModule("mqtt", MqttNotificator::class)

    // Avvio la socket dove ricevere le registrazioni
    RequestTCPSocket(2000).start()
    //RequestTCPSocket(Random().nextInt(50000)+1024).start()

    logger.info{SimpleMessage("Servizio avviato correttamente")}
}
