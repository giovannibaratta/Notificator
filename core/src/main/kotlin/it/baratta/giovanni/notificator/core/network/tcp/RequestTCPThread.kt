package it.baratta.giovanni.notificator.core.network.tcp

import it.baratta.giovanni.notificator.api.RequestHandler
import it.baratta.giovanni.notificator.utils.fatalAndThrow
import org.apache.logging.log4j.LogManager
import java.io.IOException
import java.lang.IllegalStateException
import java.net.ServerSocket
import java.net.Socket
import java.net.SocketException

/**
 * Apre una connessione TCP sulla porta [port] e resta in ascolto per le richieste dei clienti.
 * Ogni richiesta viene delegata al ClientRequestHandler che si occuperà di capire la tipologia di
 * richiesta ed soddisfarla
 *
 * @param port porta sulla quale aprire la connessione tcp
 */
class RequestTCPThread(private val port : Int) : Thread(), RequestHandler {

    private lateinit var socket : ServerSocket

    override fun run() {
        // apro la connessione sulla quale i clienti possono connettersi
        try{
            socket = ServerSocket(port)
            socket.reuseAddress = true
        }catch (exception : Exception){
            logger.fatalAndThrow(IllegalStateException("Non sono riuscito ad aprire la socket per le richieste.",exception))
        }

        var end = false

        logger.info("Ho aperto la socket sulla porta ${port}")
        while(!end) {
            var clientSocket : Socket

            /* possibile update, creo un pool di thread, accetto tutti ma
                non creo immediatamente un nuovo thread, aspetto che se ne liberi uno
             */
            try{
                clientSocket = socket.accept()
                // delego ogni richiesta al ClientRequestHandler
                ClientRequestHandler(clientSocket).start()
            }catch (socketException : SocketException){
                end = true
            }catch (exception : IOException){
                logger.error("Errore durante l'accept")
            }
        }
    }

    override fun shutdown(){
        try {
            socket.close()
        }catch (exception : IOException){
            logger.error("Errore durante la chiusura della socket per le richieste")
        }
        logger.info("Ho chiusto la socket per le richieste")
    }

    companion object {
        private val logger = LogManager.getLogger(RequestTCPThread::class.java)
    }
}