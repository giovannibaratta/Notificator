package it.baratta.giovanni.notificator.api.request

data class RegistrationRequest(val eventSource : List<ModuleRequest>,
                               val notificatorModule : List<ModuleRequest>)