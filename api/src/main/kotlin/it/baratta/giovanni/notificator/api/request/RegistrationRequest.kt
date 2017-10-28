package it.baratta.giovanni.notificator.api.request

data class RegistrationRequest(val eventSource: Set<ModuleRequest>,
                               val notificatorModule: Set<ModuleRequest>)