package it.baratta.giovanni.habitat.notificator.api

data class ConfigurationParams(val params : HashMap<String, String>) {

    fun setParam(key : String, value : String){
        params.put(key, value)
    }

    fun getParam(key : String) : String?{
        require(params.keys.contains(key))
        return params[key]
    }

}