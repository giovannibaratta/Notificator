package it.baratta.giovanni.notificator.api

import io.reactivex.Observable
import it.baratta.giovanni.notificator.api.request.ConfigurationParams

/**
 * Rappresenta una sorgente dati. Ogni implementazione di una sorgente dati
 * può gestire autonomamente i client registrati. Quando una sorgente dati non
 * è più necessaria viene invocato lo [shutdown]. Da quel momento in poi la
 * sorgente non può essere utilizzata.
 */
interface IEventSource{

    /**
     * Registra il client presso il modulo. Dal parametro [params] è possibile recuperare i
     * parametri di configurazione inviati dal client.
     * In questo metodo bisogna configurare il modulo per lo specifico cliente.
     *
     * @param clientToken token del client che si vuole registrare
     * @param parmas parametri specificati dal cliente per effettuare la registrazione
     * @return Observable sulla quale la sorgente invia i messaggi per il client
     * @throws InitializationException se si verifica un errore durante l'inizializzazione
     */
    fun initClient(clientToken: String, params: ConfigurationParams): Observable<Message>

    /**
     * Il client con il token [clientToken] non ha più bisogno di utilizzare la sorgente.
     * In questo metodo bisogna fare il cleanup del modulo per lo specifico cliente.
     */
    fun releaseClient(clientToken: String)

    /**
     * Nome logico della sorgente
     */
    val sourceName : String

    /**
     * La sorgente dati non è più necessaria. Bisogna rilasciare tutte
     * le risorse utilizzate.
     *
     * @throws ShutdownException
     */
    fun shutdown()
}