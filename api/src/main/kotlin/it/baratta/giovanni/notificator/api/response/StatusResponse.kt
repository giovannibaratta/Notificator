package it.baratta.giovanni.notificator.api.response

import it.baratta.giovanni.notificator.api.request.ModuleRequest

data class StatusResponse(val token : String,
                          val registered: Boolean,
                          val eventModule: Set<ModuleRequest>,
                          val notificatorModule: Set<ModuleRequest>) : IResponse {

    override val error: Boolean = false
    override val className: String
            = StatusResponse::class.java.canonicalName
}