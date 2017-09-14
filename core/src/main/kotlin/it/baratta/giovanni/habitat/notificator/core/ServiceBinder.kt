package it.baratta.giovanni.habitat.notificator.core

import it.baratta.giovanni.habitat.notificator.api.IEventSource
import it.baratta.giovanni.habitat.notificator.api.INotificator
import it.baratta.giovanni.habitat.utils.errorAndThrow
import org.apache.logging.log4j.LogManager
import java.util.*

/**
 *  Gestisce i notificatori, tramite il moduleBinding si passano delle coppie composte
 * da <nomeLogico,Classe che realizza il servizio di Notificator> si istanziano i notificatori desiderati
 */
class ServiceBinder private constructor() {

    companion object {
        val instance = ServiceBinder()
        private val logger = LogManager.getLogger(ServiceBinder::class.java)
    }

    private val notificatorInstance = HashMap<String, INotificator>()
    private val eventSourceInstances = HashMap<String, IEventSource>()

    fun bindNotificatorModule(service : INotificator){
        if(isNotificatorAvailable(service.notificatorName))
            logger.errorAndThrow(IllegalStateException("Un servizio con il nome ${service.notificatorName} è già presente"))

        notificatorInstance.put(service.notificatorName, service)
    }

    fun getNotificatorModule(serviceName: String) : INotificator
        = notificatorInstance[serviceName] ?: logger.errorAndThrow(IllegalStateException("Il modulo ${serviceName} non è stato caricato"))

    fun isNotificatorAvailable(name : String) : Boolean
        = notificatorInstance.keys.contains(name)

    fun bindEventSourceModule(service: IEventSource){
        if(isEventSourceAvailable(service.sourceName))
            logger.errorAndThrow(IllegalStateException("Un servizio con il nome ${service.sourceName} è già presente"))

        eventSourceInstances.put(service.sourceName, service)
    }

    fun getEventSourceModule(serviceName: String) : IEventSource
            = eventSourceInstances[serviceName] ?: logger.errorAndThrow(IllegalStateException("Il modulo ${serviceName} non è stato caricato"))

    fun isEventSourceAvailable(name : String) : Boolean
            = eventSourceInstances.keys.contains(name)

}