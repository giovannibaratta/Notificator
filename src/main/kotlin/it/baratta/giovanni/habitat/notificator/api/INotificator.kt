package it.baratta.giovanni.habitat.notificator.api

import java.io.Serializable

interface INotificator{
    /**
     * Inizializza il notificatore per lo specifico cliente
     * @param params parametri con cui effettuare l'inizializzazione
     * */
    fun initNotifcator(clientToken : String, params : ConfigurationParams) : Boolean

    /**
     * Ripulisce il notificatore dai dati del cliente
     */
    fun destroyNotificator(clientToken : String)

    /**
     * Invia i dati al cliente indicato
     * @param clientToken cliente a cui inviare i dati
     * @param payload dati da inviare
     */
    fun notify(clientToken: String, payload : Serializable)
}