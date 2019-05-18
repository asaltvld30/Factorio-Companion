package ro.upb.factoriocompanion

import android.content.res.Resources
import android.graphics.BitmapFactory
import android.graphics.Color
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import co.csadev.kellocharts.model.Axis
import co.csadev.kellocharts.model.Line
import co.csadev.kellocharts.model.LineChartData
import co.csadev.kellocharts.model.PointValue
import co.csadev.kellocharts.view.Chart
import co.csadev.kellocharts.view.LineChartView
import org.w3c.dom.Text
import ro.upb.factoriocompanion.model.Stat


class StatsContainer(private var content: Array<Stat>) {
    fun replaceElements(newContent: Array<Stat>) {
        this.content = newContent
    }

    // Computed size property
    val size: Int
        get() = content.size


    // Operators for indexing
    operator fun get(index: Int): Stat = content[index]
    operator fun set(index: Int, value: Stat) {
        content[index] = value
    }
}

class StatAdapter(private val myDataset: StatsContainer) :
    RecyclerView.Adapter<StatAdapter.MyViewHolder>() {

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder.
    // Each data item is just a string in this case that is shown in a TextView.
    class MyViewHolder(val linearLayout: LinearLayout) : RecyclerView.ViewHolder(linearLayout)


    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(parent: ViewGroup,
                                    viewType: Int): StatAdapter.MyViewHolder {
        // create a new view
        val linearLayout = LayoutInflater.from(parent.context)
            .inflate(R.layout.stat_recycled_item, parent, false) as LinearLayout
        // set the view's size, margins, paddings and layout parameters

        return MyViewHolder(linearLayout)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.linearLayout.findViewById<TextView>(R.id.item_name).text = myDataset[position].name
        holder.linearLayout.findViewById<TextView>(R.id.item_rate).text = myDataset[position].rate.toString()
        // set image
        val imageResource = holder.linearLayout.resources.getIdentifier(myDataset[position].getImageName(), "drawable",  holder.linearLayout.resources.getResourcePackageName(R.drawable.iron_ore))
        holder.linearLayout.findViewById<ImageView>(R.id.item_image).setImageResource(imageResource)

        val values = arrayListOf(PointValue(0, 2), PointValue(1, 4), PointValue(2, 3), PointValue(3, 4))

        val line = Line(values, color = Color.parseColor("#00897B"), isCubic = false)
        val lines = arrayListOf(line)
        // setup line
        line.hasLabels = false
        line.hasPoints = false

        val data = LineChartData(lines)

        val axisX = Axis(hasLines = true)
        val axisY = Axis(hasLines = true)
        axisX.textColor = R.color.primary_material_light
        axisY.textColor = R.color.primary_material_light
//        axisX.has

        data.axisXBottom = axisX
        data.axisYLeft = axisY

        holder.linearLayout.findViewById<LineChartView>(R.id.chart).lineChartData = data

        if (position % 2 == 0) {
            holder.linearLayout.setBackgroundColor(holder.linearLayout.resources.getColor(R.color.lightBackgroundAppColor))
        } else {
            holder.linearLayout.setBackgroundColor(Color.WHITE)
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = myDataset.size
}