package it.baratta.giovanni.notificator.api

import it.baratta.giovanni.notificator.api.request.ModuleRequest

interface IClientManager {
    fun registerClient(eventSourceRequest : List<ModuleRequest>, notificatorsRequest : List<ModuleRequest>) : String
    fun unregisterClient(clientToken : String)
    fun registrationStatus(clientToken : String) : Pair<List<ModuleRequest>,List<ModuleRequest>>
}