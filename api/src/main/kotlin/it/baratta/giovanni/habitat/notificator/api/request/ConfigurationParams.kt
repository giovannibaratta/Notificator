package it.baratta.giovanni.habitat.notificator.api.request

class ConfigurationParams(paramsMap : HashMap<String, String>? = null) {

    private val map = HashMap<String, String>()

    init {
        if(paramsMap != null)
            paramsMap.forEach{ map.put(it.key, it.value) }
    }

    fun setParam(key : String, value : String){
        map.put(key, value)
    }

    fun getParam(key : String) : String?{
        return map[key]
    }
}