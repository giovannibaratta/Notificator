package it.baratta.giovanni.habitat.notificator.api.request

data class RegistrationRequest(val eventSource : List<ModuleRequest>,
                               val notificatorModule : List<ModuleRequest>)