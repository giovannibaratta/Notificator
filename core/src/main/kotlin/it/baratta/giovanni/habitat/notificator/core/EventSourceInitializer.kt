package it.baratta.giovanni.habitat.notificator.core

import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import it.baratta.giovanni.habitat.notificator.api.InitializationException
import it.baratta.giovanni.habitat.notificator.api.ModuleRequest
import it.baratta.giovanni.habitat.notificator.core.network.BadRequestException
import it.baratta.giovanni.habitat.utils.errorAndThrow
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.message.SimpleMessage
import java.io.Serializable
import java.util.*
import java.util.concurrent.Semaphore
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

/**
 *
 */
class EventSourceInitializer(val clientToken : String,
                             val moduleRequest: List<ModuleRequest>) {


    private val binder = NotificatorBinder.instance
    /* l'oggetto non viene creato se non sono stati inizializzati tutti gli eventSource */
    private val lock = Semaphore(-moduleRequest.size + 1)
    /* array dei thread di configurazione in esecuzione */
    private val threadArray = ArrayList<Thread>(moduleRequest.size)

    private lateinit var collector : PublishSubject<Serializable>
    var event : Observable<Serializable>

    init{
        require(moduleRequest.size > 0)

        val observer = ArrayList<Observable<Serializable>>()

        moduleRequest.forEach {
            /* Controllo che l'eventSource sia caricato nel server */
            if(!binder.isEventSourceAvailable(it.moduleName)) {
                unregisterClient()
                logger.errorAndThrow(BadRequestException("Il servizio ${it.moduleName} non è disponibile"))
            }

            /* Creo un thread per ogni configurazione, ogni event source potrebbe essereguire sulla rete o sul disco */
            val thread = Thread{
                try {
                    observer.add(binder.getEventSourceModule(it.moduleName).registerClient(clientToken, it.params))
                    logger.debug{ SimpleMessage("Binding presso ${it.moduleName} per il client ${clientToken} completato.") }
                    positiveResponse()
                }catch (exception : InitializationException) {
                    unregisterClient()
                    logger.errorAndThrow(IllegalStateException("Il servizio ${it.moduleName} non è riuscito ad effettuare il setup."))
                }catch (exception : Exception){
                        // una delle richieste è fallita, posso terminare
                }
            }
            threadArray.add(thread)
            thread.start()
        }

        if(!lock.tryAcquire(DEFAULT_TIMEOUT, TimeUnit.MILLISECONDS)){
            unregisterClient()
            logger.errorAndThrow(TimeoutException("Timeout binding notificator"))
        }

        event = observer[0]
        for(i in 1.until(observer.size)){
            event = event.mergeWith(observer[i])
        }
    }

    private fun positiveResponse(){
        lock.release()
    }

    fun unregisterClient(){
        moduleRequest.forEach{
            binder.getEventSourceModule(it.moduleName).unregisterClient(clientToken)
        }
        threadArray.forEach{ it.interrupt() }
    }

    companion object {
        private val logger = LogManager.getLogger(NotificatorInitializer::class.java)
        const val DEFAULT_TIMEOUT = 60 *1000L
    }

}