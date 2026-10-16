package com.jay.cartube

import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.model.Action
import androidx.car.app.model.ItemList
import androidx.car.app.model.SearchTemplate
import androidx.car.app.model.Template

/** Uses the car's own keyboard/voice input, then loads YouTube search results. */
class SearchScreen(carContext: CarContext) : Screen(carContext) {

    override fun onGetTemplate(): Template =
        SearchTemplate.Builder(object : SearchTemplate.SearchCallback {
            override fun onSearchTextChanged(searchText: String) {}

            override fun onSearchSubmitted(searchText: String) {
                if (searchText.isNotBlank()) CarBrowser.search(searchText.trim())
                screenManager.pop()
            }
        })
            .setHeaderAction(Action.BACK)
            .setSearchHint("Search YouTube")
            .setShowKeyboardByDefault(true)
            .setItemList(ItemList.Builder().build())
            .build()
}
