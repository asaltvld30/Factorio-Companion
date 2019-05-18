package ro.upb.factoriocompanion

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.graphics.Color
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.support.v7.widget.LinearLayoutManager
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
import android.support.v7.widget.RecyclerView
import ro.upb.factoriocompanion.model.Stat


const val LOG_TAG = "Factorio Companion: "

class StatsActivity : AppCompatActivity() {
    // RecyclerView
    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var viewManager: RecyclerView.LayoutManager
    private lateinit var recyclerDataSet: StatsContainer

    // Stats
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

                    recyclerDataSet.replaceElements(element)
                    viewAdapter.notifyDataSetChanged()
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

        val stat1 = Stat("bla1", 12.0, 32)
        val stat2 = Stat("bla2", 12.0, 32)


//        arrayOf(stat1, stat2)
        recyclerDataSet = StatsContainer(arrayOf(stat1, stat2))

        // Setup Recycler view
        viewManager = LinearLayoutManager(this)
        viewAdapter = StatAdapter(recyclerDataSet)

        recyclerView = findViewById<RecyclerView>(R.id.my_recycler_view).apply {
            // use this setting to improve performance if you know that changes
            // in content do not change the layout size of the RecyclerView
            setHasFixedSize(true)

            // use a linear layout manager
            layoutManager = viewManager

            // specify an viewAdapter (see also next example)
            adapter = viewAdapter
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
