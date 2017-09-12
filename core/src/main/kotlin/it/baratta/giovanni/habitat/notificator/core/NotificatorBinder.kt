package it.baratta.giovanni.habitat.notificator.core

import it.baratta.giovanni.habitat.notificator.api.IEventSource
import it.baratta.giovanni.habitat.notificator.api.INotificator
import it.baratta.giovanni.habitat.utils.errorAndThrow
import org.apache.logging.log4j.LogManager
import java.util.*
import kotlin.reflect.KClass

/**
 *  Gestisce i notificatori, tramite il moduleBinding si passano delle coppie composte
 * da <nomeLogico,Classe che realizza il servizio di Notificator> si istanziano i notificatori desiderati
 */
class NotificatorBinder private constructor() {

    companion object {
        val instance = NotificatorBinder()
        private val logger = LogManager.getLogger(NotificatorBinder::class.java)
    }

    private val notificatorInstance = HashMap<String, INotificator>()
    private val eventSourceInstances = HashMap<String, IEventSource>()


    fun bindNotificatorModule(serviceName: String, serviceClass: KClass<out INotificator>){
        if(isNotificatorAvailable(serviceName))
            logger.errorAndThrow(IllegalStateException("Un servizio con il nome ${serviceName} è già presente"))

        // controllo che ci sia un construttore vuoto
        val ctor = serviceClass.constructors.filter { it.parameters.size == 0 }.firstOrNull()
        if(ctor == null)
            logger.errorAndThrow(IllegalArgumentException("Il modulo ${serviceName} non ha un construttore senza argomenti"))

        // effettuo il binding
        notificatorInstance.put(serviceName, ctor.call())
    }

    fun getNotificatorModule(serviceName: String) : INotificator
        = notificatorInstance[serviceName] ?: logger.errorAndThrow(IllegalStateException("Il modulo ${serviceName} non è stato caricato"))

    fun isNotificatorAvailable(name : String) : Boolean
        = notificatorInstance.keys.contains(name)

    fun bindEventSourceModule(serviceName: String, serviceClass: KClass<out IEventSource>){
        if(isEventSourceAvailable(serviceName))
            logger.errorAndThrow(IllegalStateException("Un servizio con il nome ${serviceName} è già presente"))

        // controllo che ci sia un construttore vuoto
        val ctor = serviceClass.constructors.filter { it.parameters.size == 0 }.firstOrNull()
        if(ctor == null)
            logger.errorAndThrow(IllegalArgumentException("Il modulo ${serviceName} non ha un construttore senza argomenti"))

        // effettuo il binding
        eventSourceInstances.put(serviceName, ctor.call())
    }

    fun getEventSourceModule(serviceName: String) : IEventSource
            = eventSourceInstances[serviceName] ?: logger.errorAndThrow(IllegalStateException("Il modulo ${serviceName} non è stato caricato"))

    fun isEventSourceAvailable(name : String) : Boolean
            = eventSourceInstances.keys.contains(name)

}