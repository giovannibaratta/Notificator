package it.baratta.giovanni.habitat.notificator.api

data class ModuleRequest(val moduleName: String,
                         val params : ConfigurationParams) {
}