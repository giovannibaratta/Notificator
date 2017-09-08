package it.baratta.giovanni.habitat.notificator.core

import it.baratta.giovanni.habitat.notificator.api.INotificator
import it.baratta.giovanni.habitat.utils.errorAndThrow
import org.apache.logging.log4j.LogManager
import java.util.*
import kotlin.reflect.KClass

/* Gestisce i notificatori, tramite il moduleBinding si passano delle coppie composte
 * da <nomeLogico,Classe che realizza il servizio di Notificator> si istanziano i notificatori desiderati  */
class NotificatorBinder private constructor() {

    companion object {
        val instance = NotificatorBinder()
        private val logger = LogManager.getLogger(NotificatorBinder::class)
    }

    private val instances = HashMap<String, INotificator>()


    fun bindModule(serviceName: String, serviceClass: KClass<out INotificator>){
        if(isServiceAvailable(serviceName))
            logger.errorAndThrow(IllegalStateException("Un servizio con il nome ${serviceName} è già presente"))

        // controllo che ci sia un construttore vuoto
        val ctor = serviceClass.constructors.filter { it.parameters.size == 0 }.firstOrNull()
        if(ctor == null)
            logger.errorAndThrow(IllegalArgumentException("Il modulo ${serviceName} non ha un construttore senza argomenti"))

        // effettuo il binding
        instances.put(serviceName, ctor.call())
    }

    fun getService(serviceName: String) : INotificator
        = instances[serviceName] ?: logger.errorAndThrow(IllegalStateException("Il modulo ${serviceName} non è stato caricato"))

    fun isServiceAvailable(name : String) : Boolean
        = instances.keys.contains(name)

}