package it.baratta.giovanni.habitat.notificator.api

enum class RequestCode(val code : Short){
        REGISTER(0x00),
        UNREGISTER(0x01)
}