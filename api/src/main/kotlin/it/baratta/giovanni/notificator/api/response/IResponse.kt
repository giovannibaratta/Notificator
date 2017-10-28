package it.baratta.giovanni.notificator.api.response

/**
 * Riposta generica a seguito di una richiesta da
 * parte di un client.
 */
interface IResponse {
    /**
     * True se la riposta rappresenta un errore.
     * False se la risposta non deriva da un errore.
     */
    val error : Boolean

    /**
     * Nome completo della classe della risposta. Utile
     * per facilitare il parsing.
     */
    val className : String
}