package ro.upb.factoriocompanion

import android.content.Intent
import android.graphics.Color
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import ro.upb.factoriocompanion.model.Stat

const val STAT_NAME_KEY = "STAT_NAME_KEY"

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

        if (position % 2 == 0) {
            holder.linearLayout.setBackgroundColor(holder.linearLayout.resources.getColor(R.color.lightBackgroundAppColor))
        } else {
            holder.linearLayout.setBackgroundColor(Color.WHITE)
        }

        holder.linearLayout.setOnClickListener { _ ->
            val intent = Intent(holder.linearLayout.context, StatDetails::class.java).apply {
                putExtra(STAT_NAME_KEY, myDataset[position].name)
            }

            holder.linearLayout.context.startActivity(intent)
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = myDataset.size
}