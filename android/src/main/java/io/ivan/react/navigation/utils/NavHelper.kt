package io.ivan.react.navigation.utils

import android.content.Context
import android.os.Bundle
import androidx.core.view.ViewCompat
import androidx.fragment.app.FragmentManager
import androidx.navigation.*
import androidx.navigation.fragment.FragmentNavigator
import androidx.navigation.fragment.NavHostFragment
import io.ivan.react.navigation.R
import io.ivan.react.navigation.view.ARG_COMPONENT_NAME
import io.ivan.react.navigation.view.ARG_LAUNCH_OPTIONS
import io.ivan.react.navigation.view.RNFragment

fun createNavHostFragmentWithoutGraph() = NavHostFragment.create(0)

fun NavController.setStartDestination(startDestination: NavDestination?) {
    startDestination ?: return

    graph = NavGraphNavigator(navigatorProvider).createDestination().also {
        it.addDestination(startDestination)
        it.startDestination = startDestination.id
    }
}

fun buildDestination(context: Context, fm: FragmentManager, destinationName: String, options: Bundle?): NavDestination {
    return FragmentNavigator(context, fm, R.id.content).createDestination().apply {
        val viewId = ViewCompat.generateViewId()
        id = viewId
        className = RNFragment::class.java.name
        addArgument(ARG_COMPONENT_NAME, NavArgumentBuilder().let { arg ->
            arg.defaultValue = destinationName
            arg.build()
        })
        addArgument(ARG_LAUNCH_OPTIONS, NavArgumentBuilder().let { arg ->
            arg.defaultValue = (options ?: Bundle()).also {
                it.putString("screenID", viewId.toString())
            }
            arg.build()
        })
    }
}

val anim_slide_in_right_out_right: AnimBuilder.() -> Unit = {
    enter = R.anim.navigation_slide_in_right
    popExit = R.anim.navigation_slide_out_right
}

val anim_top_enter_top_exit: AnimBuilder.() -> Unit = {
    enter = R.anim.navigation_top_enter
    exit = android.R.anim.fade_out
    popEnter = android.R.anim.fade_in
    popExit = R.anim.navigation_top_exit
}
