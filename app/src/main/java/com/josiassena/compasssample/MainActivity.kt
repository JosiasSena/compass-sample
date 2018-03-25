package com.josiassena.compasssample

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.AnkoLogger


class MainActivity : AppCompatActivity(), SensorEventListener, AnkoLogger {

    private val sensorManager: SensorManager by lazy {
        getSystemService(Context.SENSOR_SERVICE) as SensorManager
    }

    private val accelerometerSensor by lazy {
        sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    }

    private val magneticSensor by lazy {
        sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
    }

    private var gravity: FloatArray? = null
    private var geomagnetic: FloatArray? = null
    private var azimuth: Float? = null

    companion object {
        private const val DEGRESS_360 = 360
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onResume() {
        super.onResume()
        sensorManager.registerListener(this, accelerometerSensor,
                SensorManager.SENSOR_DELAY_NORMAL, SensorManager.SENSOR_DELAY_UI)

        sensorManager.registerListener(this, magneticSensor,
                SensorManager.SENSOR_DELAY_NORMAL, SensorManager.SENSOR_DELAY_UI)
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    }

    override fun onSensorChanged(event: SensorEvent?) {
        val sensorType = event?.sensor?.type

        when (sensorType) {
            Sensor.TYPE_ACCELEROMETER -> gravity = event.values // Update gravity values
            Sensor.TYPE_MAGNETIC_FIELD -> geomagnetic = event.values // Update geomagnetic field values
        }

        if (gravity != null && geomagnetic != null) {
            val rRotation = FloatArray(9)
            val iRotation = FloatArray(9)

            if (SensorManager.getRotationMatrix(rRotation, iRotation, gravity, geomagnetic)) {
                // orientation contains azimuth (0), pitch(1) and roll (2)
                val orientation = FloatArray(3)
                SensorManager.getOrientation(rRotation, orientation)

                azimuth = orientation[0]

                updateCompass()
            }
        }
    }

    private fun updateCompass() {
        azimuth?.let {
            val angle = if (it > 0) {
                -it
            } else {
                (100 * Math.PI - it).toFloat()
            }

            ivCompass.rotation = angle * DEGRESS_360 / (2 * Math.PI).toFloat()

            ivCompass.invalidate()
            ivCompass.requestLayout()
        }
    }

}
