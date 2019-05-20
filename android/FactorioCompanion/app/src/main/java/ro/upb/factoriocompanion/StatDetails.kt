package ro.upb.factoriocompanion

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.graphics.Color
import android.graphics.Point
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.View
import android.widget.*
import co.csadev.kellocharts.model.Axis
import co.csadev.kellocharts.model.Line
import co.csadev.kellocharts.model.LineChartData
import co.csadev.kellocharts.model.PointValue
import co.csadev.kellocharts.view.LineChartView
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.activity_stat_details.*
import ro.upb.factoriocompanion.model.Stat
import ro.upb.factoriocompanion.service.StatsService
import java.util.*
import java.util.concurrent.TimeUnit

class StatDetails : AppCompatActivity(), AdapterView.OnItemSelectedListener  {
    // Stats Service
    private lateinit var statsService: StatsService
    private var mBound: Boolean = false
    private lateinit var statsSubscription: Disposable

    // Stat name
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


    // Chart
    private var chartWindowSize: Int = DEFAULT_CHART_WINDOW_SIZE

    private lateinit var currentPoints: MutableList<PointValue>

    private var startTime = Stat.getCurrentDateTime().time

    // UI elements

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

        // Setup spinner
        ArrayAdapter.createFromResource(
            this,
            R.array.spinner_array,
            android.R.layout.simple_spinner_item

        ).also { adapter ->
            val spinner = findViewById<Spinner>(R.id.window_size_spinner)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

            spinner.adapter = adapter
            spinner.onItemSelectedListener = this
        }

    }

    private fun updateUI(stat: Stat?) {
        if (stat == null) return

        findViewById<TextView>(R.id.item_name).text = stat.name
        findViewById<TextView>(R.id.item_rate).text = stat.rate.toString()
        findViewById<TextView>(R.id.item_count).text = stat.count.toString()

        // Setup chart
        val imageResource = resources.getIdentifier(stat.getImageName(), "drawable", resources.getResourcePackageName(R.drawable.iron_ore))
        findViewById<ImageView>(R.id.item_image).setImageResource(imageResource)

        val values = statsService.getHistoryFor(stat.name)!!.map { stat ->
            PointValue((stat.date.time - startTime).toInt(), stat.rate.toInt())
        }.toMutableList()

        currentPoints = values


        val points = currentPoints.takeLast(chartWindowSize).toMutableList()
        var average: Double = 0.0
        if (points.size > 0) {
            average = (points.map { it.y }.reduce { a, b -> a + b }.toDouble() / points.size)
        }
        findViewById<TextView>(R.id.item_average).text = average.toString()

        refreshChart()
    }

    private fun refreshChart() {
        val points = currentPoints.takeLast(chartWindowSize).toMutableList()
        val line = Line(points)
        val lines = arrayListOf(line)

        val data = LineChartData(lines)


//        val axisX = Axis(hasLines = true)
        val axisY = Axis(hasLines = true)
//        axisX.name = "Time (s)"
        axisY.name = ""
//        axisX.textColor = Color.WHITE
        axisY.textColor = resources.getColor(R.color.colorPrimary)
//        data.axisXBottom = axisX
        data.axisYLeft = axisY
        line.strokeWidth = 2

        if (chartWindowSize > DEFAULT_CHART_WINDOW_SIZE) {
            line.hasLabels = false
            line.hasPoints = false
            line.strokeWidth = 1
        }

        findViewById<LineChartView>(R.id.chart).lineChartData = data
    }

    override fun onPause() {
        super.onPause()

        if (mBound) {
            Log.d(LOG_TAG, STATS_DETAILS_TAG + "onPause(): Unbind service")
            unbindService(connection)
        }
    }

    override fun onItemSelected(parent: AdapterView<*>, view: View, pos: Int, id: Long) {
        when(pos) {
            0 -> chartWindowSize = DEFAULT_CHART_WINDOW_SIZE
            1 -> chartWindowSize = 50
            2 -> chartWindowSize = 100
        }
    }

    override fun onNothingSelected(parent: AdapterView<*>) {
        // Another interface callback
    }

    companion object {
        const val STATS_DETAILS_TAG = "[StatsDetails]: "
        const val DEFAULT_CHART_WINDOW_SIZE = 10
    }
}
