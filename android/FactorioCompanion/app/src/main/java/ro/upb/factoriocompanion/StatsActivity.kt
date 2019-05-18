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
import co.csadev.kellocharts.model.Axis
import co.csadev.kellocharts.model.Line
import co.csadev.kellocharts.model.LineChartData
import co.csadev.kellocharts.model.PointValue
import co.csadev.kellocharts.view.Chart
import co.csadev.kellocharts.view.LineChartView
import com.google.firebase.firestore.IgnoreExtraProperties
import io.reactivex.disposables.Disposable
import ro.upb.factoriocompanion.service.StatsService

const val LOG_TAG = "Factorio Companion: "

class StatsActivity : AppCompatActivity() {
    private lateinit var statsService:StatsService
    private var mBound: Boolean = false

    private lateinit var statsSubscription: Disposable

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as StatsService.LocalBinder
            statsService = binder.getService()
            mBound = true

            // Create Stats subsription
            statsSubscription = statsService.observeStats()
                .doOnNext { element ->
                    Log.d(LOG_TAG, STATS_ACTIVITY_TAG + "Received element: " + element)
                }
                .subscribe()
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            mBound = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stats)

        // Start & Bind to StatsService
        Intent(this, StatsService::class.java).also { intent ->
            Log.d(LOG_TAG, STATS_ACTIVITY_TAG + "Start StatsService service")
            startService(intent)
            Log.d(LOG_TAG, STATS_ACTIVITY_TAG + "Bind StatsService service")
            bindService(intent, connection, Context.BIND_AUTO_CREATE)
        }
    }

    override fun onPause() {
        super.onPause()

        Log.d(LOG_TAG, STATS_ACTIVITY_TAG + "Dispose the stats subscription.")
        statsSubscription.dispose()
    }

    companion object {
        const val STATS_ACTIVITY_TAG = "[StatsActivity]: "
    }
}

@IgnoreExtraProperties
data class User(
    var username: String? = "",
    var email: String? = ""
)
