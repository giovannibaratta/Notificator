package it.baratta.giovanni.habitat.notificator.core

import it.baratta.giovanni.habitat.notificator.api.NotificatorParams
import it.baratta.giovanni.habitat.notificator.api.NotificatorRequest
import it.baratta.giovanni.habitat.notificator.core.network.tcp.RequestTCPSocket
import it.baratta.giovanni.habitat.notificator.core.notificatorImplementation.MqttNotificator
import it.baratta.giovanni.habitat.notificator.core.notificatorImplementation.MqttNotificatorAdapter
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.message.SimpleMessage
import java.lang.Thread.sleep
import java.util.*
import kotlin.collections.HashMap
import kotlin.concurrent.thread

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
    NotificatorBinder.instance.bindModule("mqtt", MqttNotificatorAdapter::class)

    /* Client 1*/
    val parms = HashMap<String, String>()
    parms.put("server", "tcp://192.168.0.5:1883")
    parms.put("topic", "IoT")
    val params = NotificatorParams(parms)
    val notificator = NotificatorRequest("mqtt", params)

    /*
    try{
        val token = ClientManager.instance.registerClient(listOf(notificator))
    }catch (exception : Exception){

    }
    */


    /*  Client 2 */
    val parms2 = HashMap<String, String>()
    parms2.put("server", "tcp://192.168.0.10:1883")
    parms2.put("topic", "IoT2")
    val params2 = NotificatorParams(parms2)
    val notificator2 = NotificatorRequest("mqtt", params2)
    try{
        val token = ClientManager.instance.registerClient(listOf(notificator,notificator2))
    }catch (exception : Exception){

    }


    //th1(token, 2).start()
    //th1(token2, 1).start()


    logger.info { SimpleMessage("Servizio avviato correttamente") }
}

class th1(private val token : String, private val count : Int) : Thread(){
    override fun run() {
        for(i in 0.until(count)){
            MqttNotificator.instance.notify(token, "Msg${i}")
            sleep(500L+Random().nextInt(1000))
        }
        MqttNotificator.instance.destroyNotificator(token)
    }
}

