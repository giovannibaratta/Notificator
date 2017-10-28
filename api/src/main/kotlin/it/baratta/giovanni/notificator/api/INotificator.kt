package it.baratta.giovanni.notificator.api

import it.baratta.giovanni.notificator.api.request.ConfigurationParams

/**
 * Rappresenta un sistema di notifica. Ogni implementazione rappresenta una tecnologia
 * differente. Ogni implementazione può gestire autonomamente i client registrati.
 */
interface INotificator{

    /**
     * Registra il client presso il modulo. Dal parametro [params] è possibile recuperare i
     * parametri di configurazione inviati dal client.
     * In questo metodo bisogna configurare il modulo per lo specifico cliente.
     *
     * @param clientToken token del client che si vuole registrare
     * @param parmas parametri specificati dal cliente per effettuare la registrazione
     * @throws InitializationException se si verifica un errore durante l'inizializzazione
     */
    fun initClient(clientToken: String, params: ConfigurationParams)

    /**
     * Rilascia le risorse associate al client con il token [clientToken]
     * @param clientToken token del client da rimuovere dal server
     */
    fun releaseClient(clientToken: String)

    /**
     * Invia un messagio al client con il token [clientToken].
     *
     * @param clientToken client a cui inviare i dati
     * @param message dati da inviare al client
     * @throws ClientNotFoundException se il token non è associato a nessun client
     */
    fun notify(clientToken: String, message: Message)

    /**
     * Nome logico del notificator
     */
    val notificatorName : String
}