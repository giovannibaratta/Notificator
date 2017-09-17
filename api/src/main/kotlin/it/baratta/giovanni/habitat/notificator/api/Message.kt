package it.baratta.giovanni.habitat.notificator.api

import com.google.gson.Gson

data class Message private constructor(val id : Long,
                   val source : String,
                   val jsonData : String,
                   val classData : String){

    companion object {
        fun build(source : IEventSource, id : Long, data : Any) : Message
            = Message(id,
                source.sourceName,
                gson.toJson(data),
                data::class.qualifiedName ?: throw IllegalArgumentException("data non ha qualified class name"))

        private val gson = Gson()
    }
}