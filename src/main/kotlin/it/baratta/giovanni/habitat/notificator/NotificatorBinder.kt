package it.baratta.giovanni.habitat.notificator

import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.apache.logging.log4j.message.Message
import org.apache.logging.log4j.message.ParameterizedMessage
import org.apache.logging.log4j.message.SimpleMessage
import org.apache.logging.log4j.message.TimestampMessage
import org.apache.logging.log4j.util.MessageSupplier
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


    fun bindModule(serviceName: String, serviceClass: KClass<out INotificator>) : Boolean{
        if(isServiceAvailable(serviceName)){
            logger.error{SimpleMessage("Un servizio con il nome ${serviceName} è già presente")}
            return false
        }

        // controllo che ci sia un construttore vuoto
        val ctor = serviceClass.constructors.filter { it.parameters.size == 0 }.firstOrNull()
        if(ctor == null) {
            // logger throw IllegalArgumentException("Il modulo ${serviceName} non ha un construttore senza argomenti")
            return false
        }
        // effettuo il binding
        instances.put(serviceName, ctor.call())
        return true
    }
    /*init{
        moduleBinding.forEach{
            // controllo che ci sia un construttore vuoto
            val ctor = it.value.constructors.filter { it.parameters.size == 0 }.firstOrNull()
            if(ctor == null)
                throw IllegalArgumentException("Il modulo ${it.key} non ha un construttore senza argomenti")
            // lo invoco
            instances.put(it.key, ctor.call())
        }
    }*/

    fun getService(serviceName: String) : INotificator
        = instances[serviceName] ?: throw IllegalStateException("Il modulo ${serviceName} non è stato caricato")

    fun isServiceAvailable(name : String) : Boolean
        = instances.keys.contains(name)

}