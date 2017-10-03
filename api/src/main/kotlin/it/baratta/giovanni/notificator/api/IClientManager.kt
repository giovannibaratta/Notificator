package it.baratta.giovanni.notificator.api

import it.baratta.giovanni.notificator.api.request.ModuleRequest

/**
 * Rappresenta un gestore dei client. Il gestore deve inizializzare i moduli
 * specificati dal cliente e rilasciare un token univoco in caso di successo.
 * Inoltre deve effettuare il cleanup delle risorse quando un client chiede di
 * cancellare la registrazione.
 *
 * L'implementazione deve essere thread safe.
 */
interface IClientManager {

    /**
     * Inizializza i moduli contenuti in [eventSourceRequest] e [notificatorsRequest].
     * Entrambi i set devono contenere almeno un elemento.
     *
     * @param eventSourceRequest set di moduli da utilizzare come sorgente dati
     * @param notificatorsRequest set di moduli da utilizzare come notificatori
     * @return Token univoco associato al cliente, da usare durante la deregistrazione o per le notifiche
     * @throws ClientRegistrationException se si verifica un errore durante la registrazione
     */
    fun registerClient(eventSourceRequest: Set<ModuleRequest>, notificatorsRequest: Set<ModuleRequest>): String

    /**
     * Elimina il client dal sistema rilasciando le risorse a lui riservate.
     * Se il token non è presente non viene lanciata nessuna eccezione
     * @param clientToken token del client da rimuovere
     */
    fun unregisterClient(clientToken : String)

    /**
     * Verifica se il token [clientToken] è registrato nel sistema.
     * Se presente restituisce una lista dei moduli ancora attivi.
     * Se non presente lancia eccezione.
     *
     * @param clientToken token del client da controllare
     * @return Coppia contenente due set, il primo si riferisce ai moduli sorgente dati,
     * il secondo ai notificator.
     * @throws ClientNotFoundException se [clientToken] non è associato a nessun client.
     */
    fun registrationStatus(clientToken: String): Pair<Set<ModuleRequest>, Set<ModuleRequest>>
}