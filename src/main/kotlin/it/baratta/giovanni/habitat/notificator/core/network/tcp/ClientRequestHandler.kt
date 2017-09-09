package it.baratta.giovanni.habitat.notificator.core.network.tcp

import com.google.gson.Gson
import it.baratta.giovanni.habitat.notificator.api.DataType
import it.baratta.giovanni.habitat.notificator.api.NotificatorRequest
import it.baratta.giovanni.habitat.utils.errorAndThrow
import org.apache.logging.log4j.LogManager
import java.io.IOException
import java.net.Socket
import java.nio.ByteBuffer
import it.baratta.giovanni.habitat.notificator.api.RequestCode
import it.baratta.giovanni.habitat.notificator.core.ClientManager
import it.baratta.giovanni.habitat.notificator.core.network.BadRequestException
import java.nio.charset.Charset

/**
 * Riceve una socket in ingresso dalla quale prendere la richiesta del cliente.
 * Dopo aver ricevuto la richiesta la interpreta e esegue.
 *
 * STRUTTURA RICHIESTA :
 * 2 byte - Tipologia richiesta (shortint)
 *
 * @param clientSocket socket connessa con il cliente
 */
class ClientRequestHandler(private val clientSocket : Socket) : Thread() {

    val inputStream = clientSocket.getInputStream()
    val outputStream = clientSocket.getOutputStream()
    val gson = Gson()

    companion object {
        private val logger = LogManager.getLogger(ClientRequestHandler::class)
    }

    override fun run() {
        if(inputStream == null
                || outputStream == null)
            logger.errorAndThrow(IllegalStateException("La socket ricevuta non è utilizzabile"))

        clientSocket.soTimeout = 60*1000

        try{
            /* Leggo la tipologia di richiesta */
            val requestCode = readRequestCode()
            println("Tipologia richiesta -> ${requestCode} ")

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
        val notificatorRequest = ArrayList<NotificatorRequest>()

        // leggo il numero di sorgenti a cui collegarsi
        val notificatorNumber = readNotificatorNumber()
        // leggo la tipologia di trasferimento
        val dataType = inputStream.read()

        println("datatype " +dataType)

        when(dataType){
            DataType.RAW.ordinal -> throw BadRequestException("RAW non implementato")
            DataType.JSON.ordinal -> for(i in 0.until(notificatorNumber)){
                // leggo la lunghezza dei dati del modulo
                val size = readLength()
                // leggo la porzioni di dati da parsare
                val buffer = inputStream.readBytes(size)
                // leggo il module in formato JSON
                notificatorRequest.add(gson.fromJson(String(buffer), NotificatorRequest::class.java))
            }
            else -> throw BadRequestException("DataType non supportato")
        }

        val clientToken = ClientManager.instance.registerClient(notificatorRequest.toList())
        // invio il token
        outputStream.write(clientToken.toByteArray(Charset.forName("UTF-8")))
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

    private fun readLength() : Int{
        val buffer : ByteArray

        try{
            buffer = inputStream.readBytes(4)
        }catch (exception : IOException){
            logger.errorAndThrow(BadRequestException("Lettura lunghezza"))
        }

        return ByteBuffer.wrap(buffer).getInt()
    }

    private fun unregister(){
        // leggi il token
        val clientToken = readString(ClientManager.TOKEN_SIZE)
        ClientManager.instance.unregisterClient(clientToken)
        outputStream.write(0)
    }

    private fun readNotificatorNumber() : Int{
        try{
            return inputStream.read()
        }catch (exception : IOException){
            logger.errorAndThrow(BadRequestException("Numero di Notificator da utilizzare"))
        }
    }

    /**
     * Legge dallo stream 2 byte e li converte in short
     */
    private fun readRequestCode() : Short{
        val buffer : ByteArray

        try{
            buffer = inputStream.readBytes(2)
        }catch (exception : IOException){
            logger.errorAndThrow(BadRequestException("Tipologia Richiesta"))
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
}