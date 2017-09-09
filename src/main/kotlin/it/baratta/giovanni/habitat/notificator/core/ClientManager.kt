package it.baratta.giovanni.habitat.notificator.core

import it.baratta.giovanni.habitat.notificator.api.NotificatorRequest
import it.baratta.giovanni.habitat.utils.errorAndThrow
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.message.SimpleMessage
import java.util.*
import kotlin.collections.HashMap

/**
 * Gestisce le registrazione dei clienti
 */
class ClientManager private constructor(){

    private val client = HashMap<String, NotificatorInitializer>()

    /**
     * Elabora la richiesta del cliente e restituisce un token in
     * caso di successo altrimenti eccezione.
     *
     * @param notificatorsRequest moduli per le notifiche da attivare per il cliente
     * @return Token univoco associato al cliente, da usare durante la deregistrazione o per le notifiche
     */
    fun registerClient(notificatorsRequest : List<NotificatorRequest>) : String {

        // generazione token univoco
        val clientToken = UUID.randomUUID().toString().replace("-","")
        val initializer : NotificatorInitializer

        try{
            // inizializzo tutti i moduli di notifiche specifici per il cliente
            initializer = NotificatorInitializer(clientToken,notificatorsRequest)
        }catch (exception : Exception){
            logger.errorAndThrow(IllegalStateException("Non è stato possibile inizializzare tutti i notificator."))
        }

        client.put(clientToken, initializer)
        logger.info{SimpleMessage("Rilascio il token ${clientToken}")}
        return clientToken
    }


    /**
     *  Rimuovo il cliente dal sistema
     */
    fun unregisterClient(clientToken : String){
        client[clientToken]?.unregisterClient()
        client.remove(clientToken)
    }

    companion object {
        val instance = ClientManager()
        private val logger = LogManager.getLogger(ClientManager::class)
        const val TOKEN_SIZE = 32
    }

}