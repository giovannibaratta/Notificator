package it.baratta.giovanni.habitat.notificator.core

import it.baratta.giovanni.habitat.notificator.core.network.rest.RequestRestService
import javax.ws.rs.ApplicationPath
import javax.ws.rs.core.Application

@ApplicationPath("/")
class Application : Application() {

    override fun getSingletons(): MutableSet<Any> {
        val set = HashSet<Any>()
        set.add(RequestRestService())
        return set
    }
}