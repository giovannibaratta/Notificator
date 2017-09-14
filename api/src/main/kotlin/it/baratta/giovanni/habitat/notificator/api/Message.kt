package it.baratta.giovanni.habitat.notificator.api

data class Message(val id : Int,
                   val source : String,
                   val jsonData : String,
                   val classData : String)