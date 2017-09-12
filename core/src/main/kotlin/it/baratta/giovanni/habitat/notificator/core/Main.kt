package it.baratta.giovanni.habitat.notificator.core

import it.baratta.giovanni.habitat.notificator.api.ConfigurationParams
import it.baratta.giovanni.habitat.notificator.api.ModuleRequest
import it.baratta.giovanni.habitat.notificator.api.RequestHandler
import it.baratta.giovanni.habitat.notificator.core.eventSourceImplementation.MockSourceAdapter
import it.baratta.giovanni.habitat.notificator.core.network.tcp.RequestTCPSocket
import it.baratta.giovanni.habitat.notificator.core.notificatorImplementation.MqttNotificatorAdapter
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.message.SimpleMessage
import java.util.*
import kotlin.collections.HashMap

private val logger =  LogManager.getLogger()

fun main(args: Array<String>) {

    val shutdownThread = ShutdownThread()

    Runtime.getRuntime().addShutdownHook(shutdownThread)

    logger.info { SimpleMessage("Avvio del servizio ...") }

    NotificatorBinder.instance.bindEventSourceModule("mock", MockSourceAdapter::class)
    NotificatorBinder.instance.bindNotificatorModule("mqtt", MqttNotificatorAdapter::class)

    val threadException = Thread.UncaughtExceptionHandler{th, thowable ->  }
    // Avvio la socket dove ricevere le registrazioni
    //val requestThread = RequestTCPSocket(2000).start()
    val requestThread = RequestTCPSocket(2000 /*Random().nextInt(50000)+1024*/)
    shutdownThread.addThread(requestThread)
    requestThread.start()

    /* TEST
    /* Client 1*/
    val notificatorParms = HashMap<String, String>()
    notificatorParms.put("server", "tcp://192.168.0.5:1883")
    notificatorParms.put("topic", "IoT")
    val params = ConfigurationParams(notificatorParms)
    val notificator = ModuleRequest("mqtt", params)

    val eventSourceParms = HashMap<String, String>()
    val evparams = ConfigurationParams(eventSourceParms)
    val source = ModuleRequest("mock", evparams)

    try {
        val token = ClientManager.instance.registerClient(listOf(source),
                listOf(notificator))
    } catch (exception: Exception) {
        logger.error("eccezzione", exception)
    }
    */

    requestThread.join()
    logger.info { SimpleMessage("Servizio avviato correttamente") }
}


private class ShutdownThread() : Thread(){

    private val shutdownList = ArrayList<RequestHandler>()

    fun addThread(thread : RequestHandler){
        shutdownList.add(thread)
    }

    override fun run() {
        shutdownList.forEach {
            logger.info("Termino il thread ${it}")
            it.shutdown()
        }
    }
}

