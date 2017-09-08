package it.baratta.giovanni.habitat.notificator.api

data class NotificatorRequest(val notificatorName : String,
                         val params : NotificatorParams) {
}