package it.baratta.giovanni.habitat.notificator.api.response

data class RegistrationResponse(val token : String) : IResponse {
    override val error: Boolean = false
}