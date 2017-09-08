package it.baratta.giovanni.habitat.notificator

import java.io.Serializable

interface INotificator{
    fun notify(clientID : Int, payload : Serializable)
}