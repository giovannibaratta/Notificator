package it.baratta.giovanni.notificator.api

/**
 * Raprresenta un gestore di richieste dei client.
 */

interface RequestHandler {
    /**
     * Viene invocato quando il gestore non è più necessario e bisogna
     * effettuare il cleanup delle risorse.
     */
    fun shutdown()
}