package it.baratta.giovanni.habitat.notificator.api

import java.io.Serializable

interface INotificator{
    fun notify(clientID : Int, payload : Serializable)
}