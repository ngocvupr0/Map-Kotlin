package com.abc.demomapkotlin.drawDirection

interface DirectionFinderListener {
    abstract fun onDirectionFinderStart()
    abstract fun onDirectionFinderSuccess(routes: List<Route>)
}