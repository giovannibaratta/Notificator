package it.baratta.giovanni.notificator.core.notificatorImplementation

import com.google.gson.Gson
import it.baratta.giovanni.notificator.api.INotificator
import it.baratta.giovanni.notificator.api.Message
import it.baratta.giovanni.notificator.api.exceptions.InitializationException
import it.baratta.giovanni.notificator.api.request.ConfigurationParams
import it.baratta.giovanni.notificator.utils.errorAndThrow
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.message.SimpleMessage
import org.eclipse.paho.client.mqttv3.MqttClient
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import java.util.*
import java.util.concurrent.Semaphore
import kotlin.collections.HashMap
import kotlin.collections.HashSet

/**
 * Crea dei thread per gestire le connessione verso dei broker mqtt.
 * Ogni cliente dispone di un proprio thread.
 */
class MqttNotificator private constructor(): INotificator {

    /* clienti registrati per le notifiche */
    private val clientThread = HashMap<String, MqttConnectionHandler>()

    override val notificatorName: String = "mqtt"

    private val creatingClient = HashSet<String>()
    private val lock = Object()

    /**
     * Crea un nuovo thread per il cliente. Utilizza [params] per le impostazioni della
     * connessione.
     */
    override fun initClient(clientToken: String, params: ConfigurationParams) {

        val server = params.getParam("server")
        if(server == null)
            throw InitializationException("Server non presente nei parametri")

        val topic = params.getParam("topic")
        if(topic == null)
            throw InitializationException("topic non presente nei parametri")

        synchronized(lock){
            logger.debug("registazione ${creatingClient.contains(clientToken)} - ${this}")
            if(creatingClient.contains(clientToken))
                throw InitializationException("token già registrato")
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
            throw InitializationException("Errore durante la connesioen al server $server", exception)
        }

        thread.start()
        clientThread.put(clientToken, thread)
    }

    /**
     * Chiudo la connessione e il thread relativo al cliente. Non lancia eccezione se il
     * cliente non è registrato.
     */
    override fun releaseClient(clientToken: String) {
        logger.debug("Destory notificator ${clientToken}")
        clientThread[clientToken]?.closeConnection()
        clientThread.remove(clientToken)
        creatingClient.remove(clientToken)
    }

    /**
     * Invia i dati al mqtt broker utilizzando il thread del cliente
     */
    override fun notify(clientToken: String, message: Message) {
        if(!clientThread.containsKey(clientToken))
            logger.errorAndThrow(IllegalStateException("Non è registrato nessun cliente con il token ${clientToken}"))
        clientThread[clientToken]?.notify(message)
    }

    companion object {
        val instance = MqttNotificator()
        private val logger = LogManager.getLogger(MqttNotificator::class.java)
        private val gson = Gson()
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
        private val queue : Queue<Message> = LinkedList<Message>()
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
                    mqttClient.publish(topic, gson.toJson(queue.remove()).toByteArray(),2,false)
                }
            }

            mqttClient.disconnect()
            mqttClient.close()
            logger.debug{SimpleMessage("Disconnessione client")}
        }

        /**
         *  Accodo una notifica
         */
        fun notify(message : Message){
            queue.add(message)
            sempahore.release()
        }

        fun closeConnection(){
            logger.debug("Fermo la connessione sul thread ${id}")
            end = true
            sempahore.release()
        }

    }

}