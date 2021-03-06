package io.ivan.react.navigation.view

import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.fragment.NavHostFragment
import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.ReadableMap
import com.facebook.react.uimanager.PixelUtil
import io.ivan.react.navigation.model.Page
import io.ivan.react.navigation.utils.*
import io.ivan.react.navigation.view.model.RootViewModel
import java.util.*


class RNTabBarFragment : Fragment() {

    private lateinit var view: ViewGroup

    private val tabBarHeight = PixelUtil.toPixelFromDIP(56f).toInt()
    private val tabsId = mutableListOf<Int>()

    private val viewModel: RootViewModel by lazy { ViewModelProvider(requireActivity()).get(RootViewModel::class.java) }
    private val navHostFragment: NavHostFragment by lazy { createNavHostFragmentWithoutGraph() }
    private val tabBarContainerId by lazy { View.generateViewId() }
    private val contextContainerId by lazy { View.generateViewId() }

    private val navController: NavController
        get() = navHostFragment.navController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        childFragmentManager.beginTransaction()
            .setPrimaryNavigationFragment(navHostFragment)
            .add(contextContainerId, navHostFragment)
            .add(tabBarContainerId, createTabBarFragment())
            .commitNowAllowingStateLoss()

        viewModel.tabs?.pages?.first()?.let {
            val startDestination = buildDestination(it)
            navController.setStartDestination(startDestination)
            tabsId.add(startDestination.id)
        }
        viewModel.tabs?.pages?.listIterator(1)?.forEach {
            val destination = buildDestination(it)
            navController.graph.addDestinations(destination)
            tabsId.add(destination.id)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        if (!this::view.isInitialized) {
            view = with(inflater.context) {
                FrameLayout(this).also {
                    it.addView(createTabBarContainer(this))
                    it.addView(createContentContainer(this))
                }
            }
        }
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Store.reducer(ACTION_DISPATCH_SWITCH_TAB)?.observe(requireActivity(), Observer { state ->
            val data = state as ReadableMap
            val index = data.getInt("index")
            navController.navigate(tabsId[index])
        })
    }

    private fun createTabBarFragment(): RNFragment =
        RNFragment().apply {
            mainComponentName = viewModel.tabBarComponentName
            launchOptions = Bundle().also {
                it.putString("screenID", id.toString())
                it.putSerializable("tabs", pageOptionList())
            }
        }

    private fun createTabBarContainer(context: Context) =
        FrameLayout(context).apply {
            id = tabBarContainerId
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                tabBarHeight,
                Gravity.BOTTOM
            )
        }

    private fun createContentContainer(context: Context) =
        FrameLayout(context).apply {
            id = contextContainerId
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            ).apply {
                setMargins(0, 0, 0, tabBarHeight)
            }
        }

    private fun buildDestination(page: Page): NavDestination =
        buildDestination(requireContext(), childFragmentManager, page.rootName, Arguments.toBundle(page.options))

    private fun pageOptionList(): ArrayList<Bundle?> =
        (viewModel.tabs?.pages?.map { Arguments.toBundle(it.options) } ?: mutableListOf()) as ArrayList

}
