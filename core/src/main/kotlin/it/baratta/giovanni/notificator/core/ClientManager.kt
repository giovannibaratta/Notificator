package it.baratta.giovanni.notificator.core

import io.reactivex.disposables.Disposable
import it.baratta.giovanni.notificator.api.IClientManager
import it.baratta.giovanni.notificator.api.Message
import it.baratta.giovanni.notificator.api.exceptions.ClientNotFoundException
import it.baratta.giovanni.notificator.api.exceptions.ClientRegistrationException
import it.baratta.giovanni.notificator.api.request.ModuleRequest
import it.baratta.giovanni.notificator.utils.errorAndThrow
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.message.SimpleMessage
import java.util.*
import kotlin.collections.HashMap

/**
 * Gestisce le registrazione dei clienti
 */
class ClientManager private constructor() : IClientManager {

    private val client = HashMap<String, Pair<EventSourceInitializer,NotificatorInitializer>>()
    private val clientSubscription = HashMap<String, Disposable>()


    override fun registerClient(eventSourceRequest: Set<ModuleRequest>, notificatorsRequest: Set<ModuleRequest>): String {

        // generazione token univoco
        val clientToken = UUID.randomUUID().toString().replace("-","")
        val notificatorInitializer : NotificatorInitializer?
        val eventSourceInitializer : EventSourceInitializer?

        val notificatorThread = object : Thread(){
            var initializer : NotificatorInitializer? = null

            override fun run() {
                // inizializzo tutti i moduli di notifiche specifici per il cliente
                initializer = NotificatorInitializer(clientToken,notificatorsRequest)
            }
        }

        val eventThread = object : Thread(){
            var initializer : EventSourceInitializer? = null

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

        if(notificatorInitializer == null){
            logger.errorAndThrow(ClientRegistrationException("uno dei gli notificator initializer non è valido"))
        }

        if(eventSourceInitializer == null){
            logger.errorAndThrow(ClientRegistrationException("uno dei gli event source initializer non è valido"))
        }

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
        logger.errorAndThrow(ClientRegistrationException("Uno dei thread configuratori non è terminato correttamente", throwable))
    }

    private fun notify(clientToken: String, message : Message){
        //logger.debug("Messagio ricevuto")

        val moduleList : List<ModuleRequest>
                = client[clientToken]?.second?.notificatorsRequest?.toList() ?: emptyList()

        for (i in 0.until(moduleList.size))
            ServiceBinder.instance
                    .getNotificatorModule(moduleList[i].moduleName)
                    .notify(clientToken, message)
    }

    /**
     *  Rimuovo il cliente dal sistema
     */
    override fun unregisterClient(clientToken : String){
        val clientToDelete = client[clientToken]
        client.remove(clientToken)
        clientSubscription[clientToken]?.dispose()
        clientSubscription.remove(clientToken)
        clientToDelete?.first?.unregisterClient()
        clientToDelete?.second?.unregisterClient()
    }

    override fun registrationStatus(clientToken: String): Pair<Set<ModuleRequest>, Set<ModuleRequest>> {
        val clientStatus = client[clientToken]
        if(clientStatus == null)
            throw ClientNotFoundException("il token $clientToken non è registrato nel sistema")
        return Pair(clientStatus.first.moduleRequest, clientStatus.second.notificatorsRequest)
    }

    companion object {
        val instance = ClientManager()
        private val logger = LogManager.getLogger(ClientManager::class.java)
        const val TOKEN_SIZE = 32
    }

}