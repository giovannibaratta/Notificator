package it.baratta.giovanni.habitat.notificator.api.response

import it.baratta.giovanni.habitat.notificator.api.request.ModuleRequest

data class StatusResponse(val token : String,
                          val registered: Boolean,
                          val eventModule : List<ModuleRequest>,
                          val notificatorModule : List<ModuleRequest>) : IResponse {

    override val error: Boolean = false
    override val className: String
            = StatusResponse::class.java.canonicalName
}