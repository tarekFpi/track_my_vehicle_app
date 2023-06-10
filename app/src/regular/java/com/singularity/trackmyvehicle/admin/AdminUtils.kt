package com.singularity.trackmyvehicle.admin

import android.content.Context
import android.view.Menu
import android.view.MenuItem
import dagger.Module


class IAdminNavigation : AdminNavigation {
    override fun gotoSwitchUserScreen(context: Context) {
    }

    override fun shouldGotoSwitchUserScreen(item: MenuItem): Boolean {
        return false
    }

    override fun hideSwitchUserMenuItem(menu: Menu) {

    }
}

@Module
abstract class AdminDimensionModule {

}
