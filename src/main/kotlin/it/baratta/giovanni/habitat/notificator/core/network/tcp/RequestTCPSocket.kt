package it.baratta.giovanni.habitat.notificator.core.network.tcp

import it.baratta.giovanni.habitat.utils.errorAndThrow
import org.apache.logging.log4j.LogManager
import java.io.IOException
import java.lang.IllegalStateException
import java.net.ServerSocket
import java.net.Socket

/**
 * Apre una connessione TCP sulla porta [port] e resta in ascolto per le richieste dei clienti.
 * Ogni richiesta viene delegata al ClientRequestHandler che si occuperà di capire la tipologia di
 * richiesta ed soddisfarla
 *
 * @param port porta sulla quale aprire la connessione tcp
 */
class RequestTCPSocket(private val port : Int) : Thread(){

    companion object {
        private val logger = LogManager.getLogger(RequestTCPSocket::class)
    }

    override fun run() {
        // apro la connessione sulla quale i clienti possono connettersi
        val socket : ServerSocket

        try{
            socket = ServerSocket(port)
            socket.reuseAddress = true
        }catch (exception : Exception){
            logger.errorAndThrow(IllegalStateException("Non sono riuscito ad aprire la socket.",exception))
        }

        logger.info("Ho aperto la socket sulla porta ${port}")
        while(true) {
            var clientSocket : Socket

            /* possibile update, creo un pool di thread, accetto tutti ma
                non creo immediatamente un nuovo thread, aspetto che se ne liberi uno
             */

            try{
                clientSocket = socket.accept()
                logger.info("Ho ricevuto una richiesta. Apro un thread")
                // delego ogni richiesta al ClientRequestHandler
                ClientRequestHandler(clientSocket).start()
            }catch (exception : IOException){
                logger.error("Errore durante l'accept")
            }
        }

        socket.close()
    }

}