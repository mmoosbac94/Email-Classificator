package com.example.email_classificator

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.email_classificator.extensions.convertToRoundedInt
import com.example.email_classificator.extensions.convertToRoundedPercentageAsString


class RecyclerviewItemAdapter(var itemList: List<CardItem>) :
    RecyclerView.Adapter<RecyclerviewItemAdapter.MyViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view: View =
            LayoutInflater.from(parent.context).inflate(R.layout.item_layout, parent, false)

        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val cardItem: CardItem = itemList[position]

        holder.message.text = cardItem.message
        holder.labelNonPersonal.text = cardItem.categoryList[0].label
        holder.scoreNonPersonal.text =
            cardItem.categoryList[0].score.convertToRoundedPercentageAsString()
        holder.labelPersonal.text = cardItem.categoryList[1].label
        holder.scorePersonal.text =
            cardItem.categoryList[1].score.convertToRoundedPercentageAsString()

        customizeLayout(cardItem, holder)
    }

    private fun customizeLayout(cardItem: CardItem, holder: MyViewHolder) {
        when {
            cardItem.categoryList[0].score.convertToRoundedInt() < cardItem.categoryList[1].score.convertToRoundedInt() ->
                holder.personalContainer.setBackgroundColor(Color.GREEN)
            else -> holder.nonPersonalContainer.setBackgroundColor(Color.GREEN)
        }
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var message: TextView = itemView.findViewById(R.id.messageText)
        var labelNonPersonal: TextView = itemView.findViewById(R.id.label_non_personal)
        var scoreNonPersonal: TextView = itemView.findViewById(R.id.score_non_personal)
        var labelPersonal: TextView = itemView.findViewById(R.id.label_personal)
        var scorePersonal: TextView = itemView.findViewById(R.id.score_personal)
        var personalContainer: LinearLayout = itemView.findViewById(R.id.personal_container)
        val nonPersonalContainer: LinearLayout = itemView.findViewById(R.id.non_personal_container)

    }

}