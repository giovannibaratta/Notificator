package it.baratta.giovanni.habitat.notificator.core

import it.baratta.giovanni.habitat.notificator.api.NotificatorRequest

class ClientManager private constructor(){

    /**
     * Elabora la richiesta del cliente e restituisce un token in
     * caso di successo altrimenti eccezione.
     *
     * @param request richiesta del cliente
     * @return Token univoco associato al cliente
     */
    fun registerClient(notificatorsRequest : List<NotificatorRequest>) : String {
        TODO()
    }

    fun unregisterClient(clientToken : String){
        TODO()
    }

    companion object {
        val instance = ClientManager()
    }

}