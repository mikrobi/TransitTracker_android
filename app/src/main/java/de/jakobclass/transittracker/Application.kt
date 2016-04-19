package de.jakobclass.transittracker

import de.jakobclass.transittracker.services.ApiService

class Application: android.app.Application() {
    val apiService = ApiService()
}