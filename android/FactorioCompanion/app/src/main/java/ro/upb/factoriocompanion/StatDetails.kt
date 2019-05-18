package ro.upb.factoriocompanion

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.graphics.Color
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import co.csadev.kellocharts.model.Axis
import co.csadev.kellocharts.model.Line
import co.csadev.kellocharts.model.LineChartData
import co.csadev.kellocharts.model.PointValue
import co.csadev.kellocharts.view.LineChartView
import io.reactivex.disposables.Disposable
import ro.upb.factoriocompanion.model.Stat
import ro.upb.factoriocompanion.service.StatsService

class StatDetails : AppCompatActivity() {
    // Stats Service
    private lateinit var statsService: StatsService
    private var mBound: Boolean = false
    private lateinit var statsSubscription: Disposable

    // Stat
    private lateinit var statName: String

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as StatsService.LocalBinder
            statsService = binder.getService()
            mBound = true
            Log.d(LOG_TAG, STATS_DETAILS_TAG + "Service bounded")

            // Create Stats subsription
            statsSubscription = statsService.observeStats()
                .doOnNext { element ->
                    Log.d(LOG_TAG, STATS_DETAILS_TAG + "Received element: " + element)

                    // Update UI
                    updateUI(statsService.getHistoryFor(statName)?.last())
                }
                .subscribe()

            updateUI(statsService.getHistoryFor(statName)?.last())
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            mBound = false
            Log.d(LOG_TAG, STATS_DETAILS_TAG + "Service unbounded")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stat_details)

        // Get item name
        statName = intent.getStringExtra(STAT_NAME_KEY)

        // Bind to service
        Intent(this, StatsService::class.java).also { intent ->
            Log.d(LOG_TAG, STATS_DETAILS_TAG + "Bind StatsService service")
            bindService(intent, connection, Context.BIND_AUTO_CREATE)
        }
    }

    private fun updateUI(stat: Stat?) {
        if (stat == null) return

//        val values = arrayListOf(PointValue(0, 2), PointValue(1, 4), PointValue(2, 3), PointValue(3, 4))
//
//        val line = Line(values, color = Color.parseColor("#00897B"), isCubic = false)
//        val lines = arrayListOf(line)
//
//        val data = LineChartData(lines)
//
//        val axisX = Axis(hasLines = true)
//        val axisY = Axis(hasLines = true)
//        axisX.name = "Time (s)"
//        axisY.name = "Production (units)"
//        axisX.textColor = R.color.lightBackgroundAppColor
//        axisY.textColor = R.color.lightBackgroundAppColor
//        data.axisXBottom = axisX
//        data.axisYLeft = axisY
//
//        findViewById<LineChartView>(R.id.chart).lineChartData = data

        findViewById<TextView>(R.id.item_name).text = stat.name
        findViewById<TextView>(R.id.item_rate).text = stat.rate.toString()
        findViewById<TextView>(R.id.item_count).text = stat.count.toString()

        val imageResource = resources.getIdentifier(stat.getImageName(), "drawable", resources.getResourcePackageName(R.drawable.iron_ore))
        findViewById<ImageView>(R.id.item_image).setImageResource(imageResource)
    }

    companion object {
        const val STATS_DETAILS_TAG = "[StatsDetails]: "
    }
}
