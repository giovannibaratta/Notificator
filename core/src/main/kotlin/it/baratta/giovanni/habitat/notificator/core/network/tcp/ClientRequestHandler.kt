package it.baratta.giovanni.habitat.notificator.core.network.tcp

import com.google.gson.Gson
import it.baratta.giovanni.habitat.notificator.api.DataType
import it.baratta.giovanni.habitat.notificator.api.ModuleRequest
import org.apache.logging.log4j.LogManager
import java.io.IOException
import java.net.Socket
import java.nio.ByteBuffer
import it.baratta.giovanni.habitat.notificator.api.RequestCode
import it.baratta.giovanni.habitat.notificator.core.ClientManager
import it.baratta.giovanni.habitat.notificator.core.network.BadRequestException
import it.baratta.giovanni.habitat.utils.errorAndThrow
import java.nio.charset.Charset

/**
 * Riceve una socket in ingresso dalla quale recuperare i dati della
 * richiesta. Se la richiesta può essere soddisfatta invia al client
 * il token per poter effettuare la deregistrazione.
 *
 * STRUTTURA RICHIESTA :
 * 2 byte - Tipologia richiesta (shortint) - ex. REGISTER, UNREGISTER, ...
 *
 *      RICHIESTA DI TIPO REGISTER:
 *      1 byte - Tipologia dati trasferimento
 *      1 byte - Numero di moduli per gli eventSource
 *      1 byte - Numero di moduli per i notificator
 *
 *          DATI TI TIPO JSON:
 *          per ogni moduli di event source
 *              4 byte - Lunghezza dati modulo
 *              Lunghezza dati modulo byte - Modulo in formato JSON
 *
 *      RICHIESTA DI TIPO UNREGISTER:
 *      32 byte - TOKEN
 *
 * STRUTTURA RISPOSTA DI TIPO REGISTER:
 * 1 byte - ESITO RICHIESTA
 *
 *      ESITO POSITIVO :
 *      32 byte - Token
 *
 *      ESITO NEGATIVO :
 *      ----------------
 *
 *STRUTTURA RISPOSTA DI TIPO UNREGISTER
 * 1 byte - ESITO RICHIESTA
 *
 * @param clientSocket socket connessa con il cliente
 */
class ClientRequestHandler(private val clientSocket : Socket) : Thread() {

    private val inputStream = clientSocket.getInputStream()
    private val outputStream = clientSocket.getOutputStream()
    private val gson = Gson()

    override fun run() {
        if(inputStream == null
                || outputStream == null)
            logger.errorAndThrow(IllegalStateException("La socket ricevuta non è utilizzabile"))

        clientSocket.soTimeout = DEFAULT_TIMEOUT

        try{
            /* Leggo la tipologia di richiesta - 2 byte*/
            val requestCode = readShort("Tipologia Richiesta")
            logger.info("Inizio elaborazione per ${clientSocket.remoteSocketAddress}. Richiesta di tipo-> ${requestCode} ")

            when(requestCode.toInt()){
                RequestCode.REGISTER.ordinal -> register()
                RequestCode.UNREGISTER.ordinal -> unregister()
                else -> BadRequestException("Codice richiesta non valido")
            }

        }catch (exception : IOException){
            logger.error("Impossibile leggere dalla socket")
        }catch (exception : BadRequestException) {
            logger.error("Richiesta non valida [${exception.message}]")
        } finally {
            closeSocket()
        }

        logger.info("Connessione con ${clientSocket.remoteSocketAddress} terminata")
    }

    private fun register(){

        // leggo la tipologia di dati utilizzati per il trasferimento
        val dataType = readByte("Tipologia dati del trasferimento")
        // leggo il numero di eventi a cui collegarsi
        val eventSourceNumber = readByte("Numero di EventSource da utilizzare")
        // leggo il numero di sorgenti a cui collegarsi
        val notificatorNumber = readByte("Numero di Notificator da utilizzare")

        val notificatorRequest : List<ModuleRequest>
        val eventRequest : List<ModuleRequest>

        when(dataType) {
            DataType.RAW.ordinal -> throw BadRequestException("RAW non implementato")
            DataType.JSON.ordinal -> {
                // leggo i moduli in formato JSON
                val module = readJSON(eventSourceNumber, notificatorNumber)
                notificatorRequest = module.second
                eventRequest = module.first
            }
            else -> throw BadRequestException("DataType non supportato")
        }

        val clientToken = ClientManager.instance.registerClient(eventRequest.toList(),notificatorRequest.toList())
        // invio il token
        outputStream.write(clientToken.toByteArray(Charset.forName("UTF-8")))
    }

    private fun readJSON(esNumber : Int, ntNumer : Int) : Pair<List<ModuleRequest>, List<ModuleRequest>>{

        val eventRequest = ArrayList<ModuleRequest>(esNumber)
        val notificatorRequest  = ArrayList<ModuleRequest>(ntNumer)

        for (i in 0.until(esNumber)) {
            // leggo la lunghezza dei dati del modulo
            val size = readInt("Dimensione modulo ${i} eventSource")
            // leggo la porzioni di dati da parsare
            val buffer = inputStream.readBytes(size)
            // covnerto il modulo in formato JSON
            notificatorRequest.add(gson.fromJson(String(buffer), ModuleRequest::class.java))
        }

        for (i in 0.until(ntNumer)) {
            // leggo la lunghezza dei dati del modulo
            val size = readInt("Dimensione modulo ${i} notificator")
            // leggo la porzioni di dati da parsare
            val buffer = inputStream.readBytes(size)
            // covnerto il modulo in formato JSON
            notificatorRequest.add(gson.fromJson(String(buffer), ModuleRequest::class.java))
        }
        return Pair(eventRequest, notificatorRequest)
    }

    private fun unregister(){
        // leggi il token
        val clientToken = readString(ClientManager.TOKEN_SIZE)
        ClientManager.instance.unregisterClient(clientToken)
        outputStream.write(0)
    }

    private fun readString(size : Int) : String{
        require(size > 0)
        val buffer : ByteArray

        try{
            buffer = inputStream.readBytes(size)
        }catch (exception : IOException){
            logger.errorAndThrow(BadRequestException("Lunghezza stringa"))
        }

        return String(buffer)
    }

    private fun readInt(exceptionMsg : String = "Errore lettura int") : Int{
        val buffer : ByteArray

        try{
            buffer = inputStream.readBytes(4)
        }catch (exception : IOException){
            logger.errorAndThrow(BadRequestException(exceptionMsg))
        }

        return ByteBuffer.wrap(buffer).getInt()
    }

    private fun readByte(exceptionMsg : String = "Errore lettura short") : Int{
        try{
            return inputStream.read()
        }catch (exception : IOException){
            logger.errorAndThrow(BadRequestException(exceptionMsg))
        }
    }

    /**
     * Legge dallo stream 2 byte e li converte in short
     */
    private fun readShort(exceptionMsg : String = "Errore lettura short") : Short{
        val buffer : ByteArray

        try{
            buffer = inputStream.readBytes(2)
        }catch (exception : IOException){
            logger.errorAndThrow(BadRequestException(exceptionMsg))
        }

        return ByteBuffer.wrap(buffer).getShort(0)
    }

    private fun closeSocket(){
        try{
            clientSocket.close()
        }catch (exception : IOException){
            logger.error("Non è stato possibile chiudere la socket")
        }
    }

    companion object {
        private val logger = LogManager.getLogger(ClientRequestHandler::class.java)
        const val DEFAULT_TIMEOUT = 60 * 1000
    }
}