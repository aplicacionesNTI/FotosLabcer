package com.itevebasa.fotoslabcer.modelos

data class PaginarRequest(
    val area: String,
    val page: String,
    val reg_mostrar: Int,
    val user_id: Int
)
