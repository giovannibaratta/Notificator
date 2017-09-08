package it.baratta.giovanni.habitat.notificator.api

import java.io.Serializable
import kotlin.reflect.KClass

interface INotificator{
    fun initNotifcator(clientToken : String, params : NotificatorParams) : Boolean
    fun destroyNotificator(clientToken : String)
    fun notify(clientToken: String, payload : Serializable)
}