package com.jay.cartube

import androidx.car.app.AppManager
import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.model.Action
import androidx.car.app.model.ActionStrip
import androidx.car.app.model.Template
import androidx.car.app.navigation.model.NavigationTemplate
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner

/** Main car screen: full-screen YouTube surface with a small button strip on top. */
class BrowserScreen(carContext: CarContext) : Screen(carContext) {

    private val renderer = CarSurfaceRenderer(carContext)

    init {
        carContext.getCarService(AppManager::class.java).setSurfaceCallback(renderer)
        lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onDestroy(owner: LifecycleOwner) = renderer.release()
        })
    }

    override fun onGetTemplate(): Template {
        val buttons = ActionStrip.Builder()
            .addAction(
                Action.Builder().setTitle("Search")
                    .setOnClickListener { screenManager.push(SearchScreen(carContext)) }
                    .build()
            )
            .addAction(
                Action.Builder().setTitle("Back")
                    .setOnClickListener { CarBrowser.goBack() }
                    .build()
            )
            .addAction(
                Action.Builder().setTitle("Home")
                    .setOnClickListener { CarBrowser.home() }
                    .build()
            )
            .build()

        // The PAN button switches the car into drag mode so finger-scrolling reaches the page.
        val mapButtons = ActionStrip.Builder().addAction(Action.PAN).build()

        return NavigationTemplate.Builder()
            .setActionStrip(buttons)
            .setMapActionStrip(mapButtons)
            .setPanModeListener { }
            .build()
    }
}
