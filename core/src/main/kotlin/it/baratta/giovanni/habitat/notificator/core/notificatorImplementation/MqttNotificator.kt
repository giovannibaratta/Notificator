package it.baratta.giovanni.habitat.notificator.core.notificatorImplementation

import it.baratta.giovanni.habitat.notificator.api.INotificator
import it.baratta.giovanni.habitat.notificator.api.request.ConfigurationParams
import it.baratta.giovanni.habitat.utils.errorAndThrow
import org.apache.commons.lang3.SerializationUtils
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.message.SimpleMessage
import org.eclipse.paho.client.mqttv3.MqttClient
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import java.io.Serializable
import java.util.*
import java.util.concurrent.Semaphore
import kotlin.collections.HashMap
import kotlin.collections.HashSet

/**
 *  Inoltra le richieste verso l'unica istanza di MqttNotificator
 *  */
class MqttNotificatorAdapter() : INotificator {

    companion object {
        val notificator = MqttNotificator.instance
    }

    override fun initNotifcator(clientToken: String, params: ConfigurationParams): Boolean
        = notificator.initNotifcator(clientToken, params)

    override fun destroyNotificator(clientToken: String)
        = notificator.destroyNotificator(clientToken)

    override fun notify(clientToken: String, payload: Serializable)
        = notificator.notify(clientToken, payload)
}

/**
 * Crea dei thread per gestire le connessione verso dei broker mqtt.
 * Ogni cliente dispone di un proprio thread.
 */
class MqttNotificator private constructor(): INotificator {

    /* clienti registrati per le notifiche */
    private val clientThread = HashMap<String, MqttConnectionHandler>()

    private val creatingClient = HashSet<String>()
    private val lock = Object()

    /**
     * Crea un nuovo thread per il cliente. Utilizza [params] per le impostazioni della
     * connessione.
     * @return true se è il thread è stato creato senza errori, false altrimenti.
     */
    override fun initNotifcator(clientToken: String, params: ConfigurationParams): Boolean {

        val server = params.getParam("server")
        if(server == null)
            return false

        val topic = params.getParam("topic")
        if(topic == null)
            return false

        synchronized(lock){
            logger.debug("registazione ${creatingClient.contains(clientToken)} - ${this}")
            if(creatingClient.contains(clientToken))
                return false
            creatingClient.add(clientToken)
        }

        val thread : MqttConnectionHandler

        try{
            // creo il thread per gestire la connessione. Nel costruttore viene eseuita la connessione,
            // se fallisce restituisco false
            thread = MqttConnectionHandler(server, topic, 1)
        }catch (exception : Exception){
            logger.error("Non sono riuscito a connettermi al server ${server}")
            creatingClient.remove(clientToken)
            return false
        }

        thread.start()
        clientThread.put(clientToken, thread)
        return true
    }

    /**
     * Chiudo la connessione e il thread relativo al cliente. Non lancia eccezione se il
     * cliente non è registrato.
     */
    override fun destroyNotificator(clientToken: String) {
        logger.debug("Destory notificator ${clientToken}")
        clientThread[clientToken]?.closeConnection()
        clientThread.remove(clientToken)
        creatingClient.remove(clientToken)
    }

    /**
     * Invia i dati al mqtt broker utilizzando il thread del cliente
     */
    override fun notify(clientToken: String, payload: Serializable) {
        if(!clientThread.containsKey(clientToken))
            logger.errorAndThrow(IllegalStateException("Non è registrato nessun cliente con il token ${clientToken}"))
        clientThread[clientToken]?.notify(payload)
    }

    companion object {
        val instance = MqttNotificator()
        private val logger = LogManager.getLogger(MqttNotificator::class.java)
    }

    /**
     * Apre la connessione verso il broker e permette l'invio dei messaggi.
     * Se non riesce a connettersi lancia eccezione
     *
     * @param server, server nel formato <protocollo://ip:porta>
     * @param topic topic su quale inviare i messaggi
     * @param qos qos da utilizzare per l'inivio
     */
    private class MqttConnectionHandler(server : String,
                                        private val topic : String,
                                        private val qos : Int = 0) : Thread(){
        // se = true, il thread può terminare
        private var end = false
        // coda dei messaggi
        private val queue : Queue<Serializable> = LinkedList<Serializable>()
        // attesa nel caso non ci siano messaggi da inviare
        private val sempahore = Semaphore(0)
        private val mqttClient : MqttClient

        init {

            val options = MqttConnectOptions()
            options.isAutomaticReconnect = true
            options.keepAliveInterval = 240


            mqttClient = MqttClient(server,"MqttNotificatorClient${Random().nextLong()}")
            mqttClient.timeToWait = 50 * 1000
            mqttClient.connect()
        }

        override fun run() {

            while(!end) {
                // attendo l'arrivo di un notifica
                sempahore.acquire()

                // inivio le notifiche
                while(queue.size > 0){
                    logger.debug{SimpleMessage("Inivio  notifica sul topic ${topic}")}
                    mqttClient.publish(topic, SerializationUtils.serialize(queue.remove()),2,false)
                }
            }

            mqttClient.disconnect()
            mqttClient.close()
            logger.debug{SimpleMessage("Disconnessione client")}
        }

        /**
         *  Accodo una notifica
         */
        fun notify(payload: Serializable){
            queue.add(payload)
            sempahore.release()
        }

        fun closeConnection(){
            logger.debug("Fermo la connessione sul thread ${id}")
            end = true
            sempahore.release()
        }

    }

}