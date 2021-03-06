package io.ivan.react.navigation

import com.facebook.react.bridge.*
import io.ivan.react.navigation.model.*
import io.ivan.react.navigation.utils.*


class NavigationBridge(reactContext: ReactApplicationContext) : ReactContextBaseJavaModule(reactContext) {

    override fun getName(): String {
        return "ALCNavigationBridge"
    }

    @ReactMethod
    fun registerReactComponent(componentName: String, componentOptions: ReadableMap) {
        Store.dispatch(ACTION_REGISTER_REACT_COMPONENT, componentName to componentOptions)
    }

    @ReactMethod
    fun setRoot(data: ReadableMap) {
        parseRoot(data)?.let { root ->
            Store.dispatch(ACTION_SET_ROOT, root)
        }
    }

    @ReactMethod
    fun currentRoute(promise: Promise) {
        Store.dispatch(ACTION_CURRENT_ROUTE, promise)
    }

    @ReactMethod
    fun setStyle(style: ReadableMap) {
    }

    @ReactMethod
    fun setTabBadge(badge: ReadableArray) {
    }

    @ReactMethod
    fun dispatch(screenID: String, action: String, component: String?, options: ReadableMap?) {
        when (action) {
            "push" -> Store.dispatch(ACTION_DISPATCH_PUSH, requirePage(component, options))
            "present" -> Store.dispatch(ACTION_DISPATCH_PRESENT, requirePage(component, options))
            "popToRoot" -> Store.dispatch(ACTION_DISPATCH_POP_TO_ROOT)
            "pop" -> Store.dispatch(ACTION_DISPATCH_POP)
            "dismiss" -> Store.dispatch(ACTION_DISPATCH_DISMISS)
            "popPages" -> Store.dispatch(ACTION_DISPATCH_POP_PAGES, options)
            "switchTab" -> Store.dispatch(ACTION_DISPATCH_SWITCH_TAB, options)
            else -> throw RNNavigationException("action error")
        }
    }


    @ReactMethod
    fun setResult(data: ReadableMap) {
        Store.dispatch(ACTION_SET_RESULT, data)
    }

    @ReactMethod
    fun signalFirstRenderComplete(screenID: String) {
    }

    private fun parseRoot(root: ReadableMap?): Root? {
        val rootMap = root?.getMap("root")?.takeIf { it.toHashMap().size > 0 }
            ?: throw RNNavigationException("setRoot must be only one parameter")
        return with(rootMap) {
            when {
                hasKey("tabs") -> {
                    parseTabs(this).let { Tabs(RootType.TABS, it!!.first, it.second) }
                }
                hasKey("stack") -> {
                    parseStack(this)?.let { Screen(RootType.STACK, it) }
                }
                hasKey("screen") -> {
                    parseNameInScreen(this)?.let { Screen(RootType.SCREEN, it) }
                }
                else -> throw RNNavigationException("setRoot parameter error")
            }
        }
    }

    private fun parseNameInScreen(root: ReadableMap?): Page? {
        return root?.getMap("screen")?.getString("moduleName")?.let {
            Page(it)
        }
    }

    private fun parseStack(root: ReadableMap?): Page? {
        return root?.getMap("stack")?.let {
            parseRootInStack(it)
        }
    }

    private fun parseRootInStack(stack: ReadableMap?): Page? {
        val root = stack?.getMap("root")
        val options = stack?.getMap("options")
        return parseNameInScreen(root)?.copy(options = options)
    }

    private fun parseTabs(root: ReadableMap?): Pair<List<Page>, ReadableMap?>? {
        val tabs = root?.getMap("tabs")
        val stacks = tabs?.getArray("children")
        val options = tabs?.getMap("options")
        stacks ?: throw RNNavigationException("setRoot parameter error, children is undefined")

        val pages = mutableListOf<Page>()
        for (index in 0 until stacks.size()) {
            val stack = stacks.getMap(index)?.getMap("stack")
            parseRootInStack(stack)?.let {
                pages.add(it)
            }
        }
        return pages to options
    }

    private fun requirePage(component: String?, options: ReadableMap?) =
        component?.let { Page(it, options) } ?: throw RNNavigationException("componentName is null")

}
