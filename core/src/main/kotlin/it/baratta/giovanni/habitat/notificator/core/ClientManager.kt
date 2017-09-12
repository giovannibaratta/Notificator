package it.baratta.giovanni.habitat.notificator.core

import io.reactivex.disposables.Disposable
import it.baratta.giovanni.habitat.notificator.api.InitializationException
import it.baratta.giovanni.habitat.notificator.api.ModuleRequest
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.message.SimpleMessage
import java.io.Serializable
import java.util.*
import kotlin.collections.HashMap
import it.baratta.giovanni.habitat.utils.*

/**
 * Gestisce le registrazione dei clienti
 */
class ClientManager private constructor(){

    private val client = HashMap<String, Pair<EventSourceInitializer,NotificatorInitializer>>()
    private val clientSubscription = HashMap<String, Disposable>()

    /**
     * Elabora la richiesta del cliente e restituisce un token in
     * caso di successo altrimenti eccezione.
     *
     * @param notificatorsRequest moduli per le notifiche da attivare per il cliente
     * @return Token univoco associato al cliente, da usare durante la deregistrazione o per le notifiche
     */
    fun registerClient(eventSourceRequest : List<ModuleRequest>, notificatorsRequest : List<ModuleRequest>) : String {

        // generazione token univoco
        val clientToken = UUID.randomUUID().toString().replace("-","")
        val notificatorInitializer : NotificatorInitializer
        val eventSourceInitializer : EventSourceInitializer

        val notificatorThread = object : Thread(){
            lateinit var initializer : NotificatorInitializer

            override fun run() {
                // inizializzo tutti i moduli di notifiche specifici per il cliente
                initializer = NotificatorInitializer(clientToken,notificatorsRequest)
            }
        }

        val eventThread = object : Thread(){
            lateinit var initializer : EventSourceInitializer

            override fun run() {
                // inizializzo tutti i moduli di notifiche specifici per il cliente
                initializer = EventSourceInitializer(clientToken,eventSourceRequest)
            }
        }

        notificatorThread.setUncaughtExceptionHandler(this::exceptionCollector)
        eventThread.setUncaughtExceptionHandler(this::exceptionCollector)

        notificatorThread.start()
        eventThread.start()

        notificatorThread.join()
        eventThread.join()

        notificatorInitializer = notificatorThread.initializer
        eventSourceInitializer = eventThread.initializer

        client.put(clientToken, Pair(eventSourceInitializer,notificatorInitializer))

        clientSubscription.put(clientToken,
                eventSourceInitializer.event.subscribe(
                        {notify(clientToken,it)}, // onNext
                        {unregisterClient(clientToken)}, // onError
                        {unregisterClient(clientToken)})) // onComplete

        logger.info{SimpleMessage("Rilascio il token ${clientToken}")}
        return clientToken
    }

    private fun exceptionCollector(thread: Thread, throwable: Throwable) : Nothing{
        logger.errorAndThrow(InitializationException("Uno dei thread configuratori non è terminato correttamente"))
    }

    private fun notify(clientToken: String, data : Serializable){
        logger.debug("Messagio ricevuto")

        val moduleList : List<ModuleRequest>
                = client[clientToken]?.second?.notificatorsRequest ?: throw IllegalStateException("Moduli non trovato")

        for (i in 0.until(moduleList.size))
            NotificatorBinder.instance
                    .getNotificatorModule(moduleList[i].moduleName)
                    .notify(clientToken, data)
    }


    /**
     *  Rimuovo il cliente dal sistema
     */
    fun unregisterClient(clientToken : String){
        clientSubscription[clientToken]?.dispose()
        clientSubscription.remove(clientToken)
        client[clientToken]?.first?.unregisterClient()
        client[clientToken]?.second?.unregisterClient()
        client.remove(clientToken)
    }

    companion object {
        val instance = ClientManager()
        private val logger = LogManager.getLogger(ClientManager::class.java)
        const val TOKEN_SIZE = 32
    }

}