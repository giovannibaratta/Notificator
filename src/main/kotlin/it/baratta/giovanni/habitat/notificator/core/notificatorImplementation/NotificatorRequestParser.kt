package it.baratta.giovanni.habitat.notificator.core.notificatorImplementation

import it.baratta.giovanni.habitat.notificator.api.NotificatorRequest

class NotificatorRequestParser {

    companion object {
        fun parse(byteArray : ByteArray) : NotificatorRequest{
            /*
            // leggo la lunghezza del nome del notificator (4 bytes)
            val strLenght = readStringLength()
            // leggo il nome del notificator
            val name = readString(strLenght)

            if(!NotificatorBinder.instance.isServiceAvailable(name))
                throw BadRequestException("Il servizio ${name} non è disponibile")

            // In base al nome del servizio leggo i parametri aggiunti,
            // Leggo i parametri aggiunti del notificator

            // Creo un oggetto NotificatorRequest da passare al ClientManager
            */
            TODO()
        }
    }

}