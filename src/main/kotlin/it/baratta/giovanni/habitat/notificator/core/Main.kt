package it.baratta.giovanni.habitat.notificator.core

import it.baratta.giovanni.habitat.notificator.api.ConfigurationParams
import it.baratta.giovanni.habitat.notificator.api.ModuleRequest
import it.baratta.giovanni.habitat.notificator.core.eventSourceImplementation.MockSource
import it.baratta.giovanni.habitat.notificator.core.eventSourceImplementation.MockSourceAdapter
import it.baratta.giovanni.habitat.notificator.core.notificatorImplementation.MqttNotificator
import it.baratta.giovanni.habitat.notificator.core.notificatorImplementation.MqttNotificatorAdapter
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.message.SimpleMessage
import java.lang.Thread.sleep
import java.util.*
import kotlin.collections.HashMap

fun main(args : Array<String>) {

    val logger = LogManager.getLogger()
    logger.info { SimpleMessage("Avvio del servizio ...") }

    /* DA SCOMMENTARE
    // Carico i servizi nel NotificatorBinder
    logger.info{SimpleMessage("Carico i moduli dei notificatori")}


    // Avvio la socket dove ricevere le registrazioni
    RequestTCPSocket(2000).start()
    //RequestTCPSocket(Random().nextInt(50000)+1024).start()
    */
    NotificatorBinder.instance.bindEventSourceModule("mock", MockSourceAdapter::class)
    NotificatorBinder.instance.bindNotificatorModule("mqtt", MqttNotificatorAdapter::class)

    /* Client 1*/
    val notificatorParms = HashMap<String, String>()
    notificatorParms.put("server", "tcp://192.168.0.5:1883")
    notificatorParms.put("topic", "IoT")
    val params = ConfigurationParams(notificatorParms)
    val notificator = ModuleRequest("mqtt", params)

    val eventSourceParms = HashMap<String, String>()
    val evparams = ConfigurationParams(eventSourceParms)
    val source = ModuleRequest("mock", evparams)

    try{
        val token = ClientManager.instance.registerClient(listOf(source),
                listOf(notificator))
    }catch (exception : Exception){
        logger.error("eccezzione", exception)
    }


    /*
    /*  Client 2 */
    val parms2 = HashMap<String, String>()
    parms2.put("server", "tcp://192.168.0.5:1883")
    parms2.put("topic", "IoT2")
    val params2 = ConfigurationParams(parms2)
    val notificator2 = ModuleRequest("mqtt", params2)
    try{
        val token = ClientManager.instance.registerClient(listOf(notificator2))
    }catch (exception : Exception){
        println("Exception")
    }

    */

    logger.info { SimpleMessage("Servizio avviato correttamente") }
}

