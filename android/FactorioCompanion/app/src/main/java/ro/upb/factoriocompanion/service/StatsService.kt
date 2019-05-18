package ro.upb.factoriocompanion.service

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import com.androidhuman.rxfirebase2.database.dataChanges
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.PublishSubject
import ro.upb.factoriocompanion.LOG_TAG
import ro.upb.factoriocompanion.model.Stat
import java.util.*
import kotlin.collections.HashMap

//class StatHistory(date: Date, stat: Stat)

class StatsService: Service() {
    private lateinit var database: DatabaseReference
    private lateinit var databaseUpdatesSubscription: Disposable
    private var statsPublishSubject = PublishSubject.create<Array<Stat>>()

    // StatsHistory
    private var statsHistory = HashMap<String, List<Stat>>()

    // Binder given to clients
    private val binder = LocalBinder()

    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    inner class LocalBinder : Binder() {
        // Return this instance of LocalService so clients can call public methods
        fun getService(): StatsService = this@StatsService
    }

    override fun onBind(intent: Intent): IBinder {
        Log.d(LOG_TAG, StatsService.statsServiceTag + "onBind()")
        return binder
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(LOG_TAG, statsServiceTag + "onCreate() " + Thread.currentThread().name)

        // Initialize Firebase Database
        Log.d(LOG_TAG, statsServiceTag + "Initialize Firebase Database.")
        database = FirebaseDatabase.getInstance().reference.child("stats/items")

        // Initialize data store

        // Put model into the data store
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Subscribe to data changes
        Log.d(LOG_TAG, statsServiceTag + "onStartComand() Subscribe to Firebase Database data changes.")
        databaseUpdatesSubscription = database.dataChanges()
            .doOnError { error -> Log.e(LOG_TAG, error.toString()) }
            .subscribe({ databaseSnapshot ->
                if (databaseSnapshot.exists()) {
                    Log.d(LOG_TAG, statsServiceTag + databaseSnapshot.toString() + Thread.currentThread().name)

                    // Parse data from Firebase
                    val databaseHashMap = databaseSnapshot.value as HashMap<Any, Any?>
                    val parsed = Stat.parseIncomingFirebaseHashSet(databaseHashMap)
                    Log.d(LOG_TAG, statsServiceTag + parsed.toString())

                    // Publish parsed data
                    statsPublishSubject.onNext(parsed)

                    // Add to history HM
                    parsed.forEach { stat ->
                        updateHistoryFor(stat)
                    }
                    // TODO: Save history object to UserPreferences. Maybe?
                } else {
                    // Data does not exists
                }
            }) {
                // Handle error
            }


        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(LOG_TAG, statsServiceTag + "onDestroy() " + Thread.currentThread().name)

        Log.d(LOG_TAG, statsServiceTag + "Dispose the FirebaseDatabase subscription.")
        // Dispose Firebase Database subscription
        databaseUpdatesSubscription.dispose()
    }

    fun observeStats(): PublishSubject<Array<Stat>> {
        return statsPublishSubject
    }

    fun getHistoryFor(stat: String): List<Stat>? {
        return statsHistory.get(stat)
    }

    private fun updateHistoryFor(stat: Stat) {
        statsHistory.set(
            stat.name,
            statsHistory.getOrDefault(stat.name, listOf<Stat>()) + listOf<Stat>(stat)
        )
    }

    companion object {
        const val statsServiceTag = "[StatsService]: "
    }
}