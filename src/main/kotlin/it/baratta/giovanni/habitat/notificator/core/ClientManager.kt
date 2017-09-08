package it.baratta.giovanni.habitat.notificator.core

import it.baratta.giovanni.habitat.notificator.core.network.BadRequestException
import it.baratta.giovanni.habitat.notificator.api.NotificatorRequest
import it.baratta.giovanni.habitat.utils.errorAndThrow
import org.apache.logging.log4j.LogManager
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class ClientManager private constructor(){

    private val binder = NotificatorBinder.instance
    /* notificari alla quale è registrato il cliente */
    private val client = HashMap<String, ArrayList<String>>()

    /**
     * Elabora la richiesta del cliente e restituisce un token in
     * caso di successo altrimenti eccezione.
     *
     * @param notificatorsRequest moduli per le notifiche da attivare per il cliente
     * @return Token univoco associato al cliente
     */
    fun registerClient(notificatorsRequest : List<NotificatorRequest>) : String {
        // genera un nuovo token
        val clientToken = UUID.randomUUID().toString().replace("-","")
        client.put(clientToken, ArrayList<String>())

        notificatorsRequest.forEach {
            if(!binder.isServiceAvailable(it.notificatorName)) {
                unregisterClient(clientToken)
                logger.errorAndThrow(BadRequestException("Il servizio ${it.notificatorName} non è disponibile"))
            }
            if(!binder.getService(it.notificatorName).initNotifcator(clientToken,it.params)){
                unregisterClient(clientToken)
                logger.errorAndThrow(IllegalStateException("Il servizio ${it.notificatorName} non è riuscito ad effettuare il setup"))
            }
            client[clientToken]?.add(it.notificatorName)
        }

        return clientToken
    }


    fun unregisterClient(clientToken : String){
        client[clientToken]?.forEach{
            binder.getService(it).destroyNotificator(clientToken)
        }
        client.remove(clientToken)
    }

    companion object {
        val instance = ClientManager()
        private val logger = LogManager.getLogger(ClientManager::class)
        const val TOKEN_SIZE = 32
    }

}