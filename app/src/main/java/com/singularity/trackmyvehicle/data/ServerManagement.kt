package com.singularity.trackmyvehicle.data

import android.content.SharedPreferences

enum class UserSource(val identifier: String) {
    VERSION_2("v2"), VERSION_3("v3"), NOT_DETERMINED("")

}

private val SP_USER_SOURCE = "SP_USER_SOURCE"

fun readUserSource(sp: SharedPreferences): UserSource {
    return when (sp.getString(SP_USER_SOURCE, "")) {
        UserSource.VERSION_2.identifier -> UserSource.VERSION_2
        UserSource.VERSION_3.identifier -> UserSource.VERSION_3
        else -> UserSource.NOT_DETERMINED

    }
}

