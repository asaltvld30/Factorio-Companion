package ro.upb.factoriocompanion

import android.graphics.Color
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import co.csadev.kellocharts.model.Axis
import co.csadev.kellocharts.model.Line
import co.csadev.kellocharts.model.LineChartData
import co.csadev.kellocharts.model.PointValue
import co.csadev.kellocharts.view.LineChartView

class StatDetails : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stat_details)

        val values = arrayListOf(PointValue(0, 2), PointValue(1, 4), PointValue(2, 3), PointValue(3, 4))

        val line = Line(values, color = Color.parseColor("#00897B"), isCubic = false)
        val lines = arrayListOf(line)

        val data = LineChartData(lines)

        val axisX = Axis(hasLines = true)
        val axisY = Axis(hasLines = true)
        axisX.name = "Time (s)"
        axisY.name = "Production (units)"
        axisX.textColor = R.color.primary_material_light
        axisY.textColor = R.color.primary_material_light
        data.axisXBottom = axisX
        data.axisYLeft = axisY

//        val chart = LineChartView(this)
//        chart.lineChartData = data

        findViewById<LineChartView>(R.id.chart).lineChartData = data
    }
}
