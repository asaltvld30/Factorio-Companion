package ro.upb.factoriocompanion

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.androidhuman.rxfirebase2.database.dataChanges
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.IgnoreExtraProperties
import io.reactivex.disposables.Disposable

const val STATS_LOG_TAG = "Stats Activity: "

class StatsActivity : AppCompatActivity() {
    private lateinit var database: DatabaseReference
    private lateinit var databaseUpdatesSubscription: Disposable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stats)

        database = FirebaseDatabase.getInstance().reference
    }

    override fun onResume() {
        super.onResume()
        databaseUpdatesSubscription = database.dataChanges()
            .subscribe({
                if (it.exists()) {
                    Log.d(STATS_LOG_TAG, it.toString());
                    // Do something with data
                } else {
                    // Data does not exists
                }
            }) {
                // Handle error
            }
    }

    override fun onPause() {
        super.onPause()
        databaseUpdatesSubscription.dispose()
    }
}

@IgnoreExtraProperties
data class User(
    var username: String? = "",
    var email: String? = ""
)
