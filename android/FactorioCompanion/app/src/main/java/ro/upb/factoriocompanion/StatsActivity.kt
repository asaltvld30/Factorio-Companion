package ro.upb.factoriocompanion

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import com.google.firebase.firestore.IgnoreExtraProperties
import io.reactivex.disposables.Disposable
import ro.upb.factoriocompanion.service.StatsService
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ProgressBar
import ro.upb.factoriocompanion.model.Stat


const val LOG_TAG = "Factorio Companion: "

class StatsActivity : AppCompatActivity() {
    // RecyclerView
    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var viewManager: RecyclerView.LayoutManager
    private lateinit var recyclerDataSet: StatsContainer

    // Stats Service
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
                    findViewById<ProgressBar>(R.id.progress_circular).visibility = View.GONE
                }
                .subscribe()
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            mBound = false
        }
    }

    override fun onResume() {
        super.onResume()

        // Start & Bind to StatsService
        Intent(this, StatsService::class.java).also { intent ->
            Log.d(LOG_TAG, STATS_ACTIVITY_TAG + "Bind StatsService service")
            bindService(intent, connection, Context.BIND_AUTO_CREATE)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stats)

        // Start StatsService
        Intent(this, StatsService::class.java).also { intent ->
            Log.d(LOG_TAG, STATS_ACTIVITY_TAG + "Start StatsService service")
            startService(intent)
            Log.d(LOG_TAG, STATS_ACTIVITY_TAG + "Bind StatsService service")
            bindService(intent, connection, Context.BIND_AUTO_CREATE)
        }


        recyclerDataSet = StatsContainer(arrayOf<Stat>())

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

        if (mBound) {
            Log.d(LOG_TAG, STATS_ACTIVITY_TAG + "onPause(): Unbind service")
            mBound = false
            unbindService(connection)
        }
    }

    companion object {
        const val STATS_ACTIVITY_TAG = "[StatsActivity]: "
    }
}