package it.baratta.giovanni.habitat.notificator.core

import it.baratta.giovanni.habitat.notificator.api.request.ModuleRequest
import it.baratta.giovanni.habitat.notificator.core.network.BadRequestException
import it.baratta.giovanni.habitat.utils.errorAndThrow
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.message.SimpleMessage
import java.util.*
import java.util.concurrent.Semaphore
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

/**
 * Ogni client è associato un [NotificatorInitializer]. Il [NotificatorInitializer] si occupa di inizializzare tutti
 * i notificatori presenti in [notificatorsRequest] utilizzando i parametri oppurtuni. Se non si riesce a configurare
 * anche solo un unico notificatore prima dello scadere del timeout viene lancita un'eccezione.
 * Il [notificatorInizializer] può eliminare tutte le sottoscrizioni dai Notificator.
 *
 * @param clientToken cliente a cui è associato
 * @param notificatorsRequest notificatori da inizializzare
 * @throws BadRequestException se uno dei notificatori da configurare non è disponibile
 * @throws TimeoutException se non si è riuscito a configurare tutti i notificatori prima del timeout
 */
class NotificatorInitializer(val clientToken : String,
        val notificatorsRequest : List<ModuleRequest>) {


    private val binder = NotificatorBinder.instance
    /* l'oggetto non viene creato se non sono stati inizializzati tutti i notificatori */
    private val lock = Semaphore(-notificatorsRequest.size + 1)
    /* array dei thread di configurazione in esecuzione */
    private val threadArray = ArrayList<Thread>(notificatorsRequest.size)

    init{

        notificatorsRequest.forEach {
            /* Controllo che il notificatore sia caricato nel server */
            if(!binder.isNotificatorAvailable(it.moduleName)) {
                unregisterClient()
                logger.errorAndThrow(BadRequestException("Il servizio ${it.moduleName} non è disponibile"))
            }

            /* Creo un thread per ogni configurazione, ogni notificatore potrebbe essereguire sulla rete o sul disco */
            val thread = Thread{
                try {
                    if (!binder.getNotificatorModule(it.moduleName).initNotifcator(clientToken, it.params)) {
                        unregisterClient()
                        logger.errorAndThrow(IllegalStateException("Il servizio ${it.moduleName} non è riuscito ad effettuare il setup."))
                    }
                    logger.debug{SimpleMessage("Binding presso ${it.moduleName} per il client ${clientToken} completato.")}
                    /* Se il notificatore è stato configurato correttamente lo segnalo */
                    positiveResponse()
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
    }

    private fun positiveResponse(){
        lock.release()
    }

    fun unregisterClient(){
        notificatorsRequest.forEach{
            binder.getNotificatorModule(it.moduleName).destroyNotificator(clientToken)
        }
        threadArray.forEach{
            it.interrupt() }
    }

    companion object {
        private val logger = LogManager.getLogger(NotificatorInitializer::class.java)
        const val DEFAULT_TIMEOUT = 60 *1000L
    }

}