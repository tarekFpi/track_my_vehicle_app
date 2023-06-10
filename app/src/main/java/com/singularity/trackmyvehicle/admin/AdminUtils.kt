package com.singularity.trackmyvehicle.admin

import android.content.Context
import android.view.Menu
import android.view.MenuItem


interface AdminNavigation {
    fun gotoSwitchUserScreen(context: Context)
    fun shouldGotoSwitchUserScreen(item: MenuItem) : Boolean
    fun hideSwitchUserMenuItem(menu: Menu)
}
