package it.baratta.giovanni.habitat.notificator.api.request

data class ModuleRequest(val moduleName: String,
                         val params : ConfigurationParams) {
}