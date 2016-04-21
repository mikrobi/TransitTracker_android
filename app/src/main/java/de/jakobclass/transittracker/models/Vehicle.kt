package de.jakobclass.transittracker.models

import java.lang.ref.WeakReference
import java.util.*

interface VehicleDelegate {
    fun onVehiclePositionUpdate(vehicle: Vehicle)
}

data class Vehicle private constructor(val vehicleId: String, val type: VehicleType,
                                       val name: String, val destination: String) {
    var delegate: VehicleDelegate?
        get() = delegateReference.get()
        set(value) {
            delegateReference = WeakReference<VehicleDelegate>(value)
        }
    var position: Position
        get() = _position
        set(value) {
            val didChange = value != _position
            _position = value
            if (didChange) {
                delegate?.onVehiclePositionUpdate(this)
            }
        }
    var predictedPositions = LinkedList<Position>()

    private var delegateReference = WeakReference<VehicleDelegate>(null)
    private lateinit var _position: Position

    constructor(vehicleId: String, type: VehicleType, name: String, destination: String,
                position: Position): this(vehicleId, type, name, destination) {
        this._position = position
    }

    fun updatePosition() {
        predictedPositions.pollFirst()?.let { position = it }
    }
}