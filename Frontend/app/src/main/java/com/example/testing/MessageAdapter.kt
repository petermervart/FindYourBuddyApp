package com.example.testing

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView

class MessageAdapter(val context: Context,
                          val dataSource: ArrayList<Message>) : BaseAdapter() {

    private val inflater: LayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    override fun getCount(): Int {
        return dataSource.size
    }

    override fun getItem(position: Int): Any {
        return dataSource[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {

        val message = getItem(position) as Message
        if(message.sender_id == Data.id){
            val rowView = inflater.inflate(R.layout.user_message_layout, parent, false)
            val text = rowView.findViewById<TextView>(R.id.chat_message_user)
            text.text = message.text
            return rowView
        }
        else{
            val rowView = inflater.inflate(R.layout.friend_message_layout, parent, false)
            val text = rowView.findViewById<TextView>(R.id.chat_message_friend)
            text.text = message.text
            return rowView
        }
    }
}