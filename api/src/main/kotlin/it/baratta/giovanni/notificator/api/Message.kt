package it.baratta.giovanni.notificator.api

import com.google.gson.Gson

/**
 * Wrapper che include i dati effettivi da inviare ad un client
 * più metadati per facilitare il parsing. La coppia <id,source>
 * identifica univocamente un messaggio. Messaggi proveniente da
 * sorgenti differenti possono avere lo stesso id.
 *
 * @param id id univoco del messaggio. Serve per identificare il messaggio.
 * @param source nome della sorgente che ha generato il messaggio
 * @param jsonData dati da inviare in formato json
 * @param classData nome completo della classe dei dati (comprende anche il package)
 */
data class Message private constructor(val id : Long,
                                       val source : String,
                                       val jsonData : String,
                                       val classData : String){

    companion object {

        /**
         * Funzione di supporto per creare un messaggio.
         * @param source sorgente che ha generato il messaggio
         * @param id id univoco del messaggio per la sorgente [soruce]
         * @param data dati da inviare al client
         * @throws IllegalArgumentException se la classe di [data] non ha un nome
         */
        fun build(source : IEventSource, id : Long, data : Any) : Message
                = Message(id,
                source.sourceName,
                gson.toJson(data),
                data::class.qualifiedName ?: throw IllegalArgumentException("data non ha qualified class name"))

        private val gson = Gson()
    }
}