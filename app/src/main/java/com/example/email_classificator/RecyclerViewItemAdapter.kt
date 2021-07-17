package com.example.email_classificator

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView


class RecyclerviewItemAdapter(mItemList: List<CardItem>) :
    RecyclerView.Adapter<RecyclerviewItemAdapter.MyViewHolder>() {
    private val cardItemsList: List<CardItem> = mItemList
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view: View =
            LayoutInflater.from(parent.context).inflate(R.layout.item_layout, parent, false)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val cardItem: CardItem = cardItemsList[position]
        holder.message.text = cardItem.message
        holder.result.text = cardItem.result
    }

    override fun getItemCount(): Int {
        return cardItemsList.size
    }

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var message: TextView = itemView.findViewById(R.id.messageText)
        var result: TextView = itemView.findViewById(R.id.resultText)

    }

}