package de.jakobclass.transittracker

import de.jakobclass.transittracker.services.StopService

class Application: android.app.Application() {
    val stopService = StopService()
}