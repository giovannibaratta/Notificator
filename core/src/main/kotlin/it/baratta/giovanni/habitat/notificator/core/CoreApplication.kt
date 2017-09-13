package it.baratta.giovanni.habitat.notificator.core

import javax.ws.rs.ApplicationPath
import it.baratta.giovanni.habitat.notificator.core.network.rest.RequestRestService
import javax.ws.rs.core.Application

@ApplicationPath("/")
class CoreApplication : Application() {

    override fun getSingletons(): MutableSet<Any> {
        val set = HashSet<Any>()
        set.add(RequestRestService())
        return set
    }
}