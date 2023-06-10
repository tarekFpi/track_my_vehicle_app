package com.singularity.trackmyvehicle.admin

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.Menu
import android.view.MenuItem
import com.singularity.trackmyvehicle.R
import com.singularity.trackmyvehicle.SwitchUserActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

class IAdminNavigation : AdminNavigation {
    override fun gotoSwitchUserScreen(context: Context) {
        context.startActivity(Intent(context, SwitchUserActivity::class.java))
        if (context is Activity)
            context.finish()
    }

    override fun shouldGotoSwitchUserScreen(item: MenuItem): Boolean {
        return item.itemId == R.id.navigation_switch_user
    }

    override fun hideSwitchUserMenuItem(menu: Menu) {
        menu.findItem(R.id.navigation_switch_user)?.isVisible = false
    }
}

@Module
abstract class AdminDimensionModule {

    @ContributesAndroidInjector
    internal abstract fun contributeSwitchUserActivity(): SwitchUserActivity

}
