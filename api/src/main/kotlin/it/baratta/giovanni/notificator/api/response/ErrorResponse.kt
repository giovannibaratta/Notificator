package it.baratta.giovanni.notificator.api.response

data class ErrorResponse(val errorMsg : String) : IResponse{
    override val error: Boolean = true

    override val className: String
            = ErrorResponse::class.java.canonicalName
}