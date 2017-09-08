package it.baratta.giovanni.habitat.notificator.core.notificatorImplementation

import it.baratta.giovanni.habitat.notificator.api.INotificator
import it.baratta.giovanni.habitat.notificator.api.NotificatorParams
import it.baratta.giovanni.habitat.utils.errorAndThrow
import org.apache.logging.log4j.LogManager
import java.io.Serializable
import java.util.*

class MqttNotificator private constructor(): INotificator {

    private val clientThread = HashMap<String, MqttConnectionHandler>()

    override fun initNotifcator(clientToken: String, params: NotificatorParams): Boolean {
        if(clientThread.containsKey(clientToken))
            return false

        val server = params.getParam("server")
        if(server == null)
            return false

        val topic = params.getParam("topic")
        if(topic == null)
            return false

        val thread = MqttConnectionHandler(server, topic, 1)
        thread.start()
        clientThread.put(clientToken, thread)
        return true
    }

    override fun destroyNotificator(clientToken: String) {
        clientThread[clientToken]?.interrupt()
        clientThread.remove(clientToken)
    }

    override fun notify(clientToken: String, payload: Serializable) {
        if(!clientThread.containsKey(clientToken))
            logger.errorAndThrow(IllegalStateException("Non è registrato nessun cliente con il token ${clientToken}"))
        clientThread[clientToken]?.notify(payload)
    }

    companion object {
        val instance = MqttNotificator()
        val logger = LogManager.getLogger(MqttNotificator::class)
    }

    private class MqttConnectionHandler(private val server : String,
                                        private val topic : String,
                                        private val qos : Int = 0) : Thread(){
        var end = false
        var stack = Stack<Serializable>()

        override fun run() {
            while(!end) {
                try {
                    sleep(Long.MAX_VALUE)
                }catch (exception : InterruptedException){

                }
                // inivio le notifiche
                while(!stack.empty()){
                    TODO()
                }
            }
        }

        fun notify(payload: Serializable){
            interrupt()
            stack.push(payload)
        }

    }

}