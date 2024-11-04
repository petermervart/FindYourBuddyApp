package com.example.testing

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import java.text.SimpleDateFormat
import java.util.*

class ConversationAdapter(val context: Context,
                          val dataSource: ArrayList<Conversation>) : BaseAdapter() {

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

        val rowView = inflater.inflate(R.layout.conversation_layout, parent, false)

        val nameChat = rowView.findViewById<TextView>(R.id.chat_name)

        val timeChat = rowView.findViewById<TextView>(R.id.chat_time)

        val conversation = getItem(position) as Conversation

        val dateStr = conversation.created_at
        val df = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.ENGLISH)
        df.timeZone = TimeZone.getTimeZone("UTC")
        val date = df.parse(dateStr)
        val formatter = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.ENGLISH)
        formatter.timeZone = TimeZone.getDefault()
        val formattedDate = formatter.format(date)

        timeChat.text = formattedDate

        if(Data.id == conversation.sender_id){
            nameChat.text = conversation.reciever_name
        }
        else{
            nameChat.text = conversation.sender_name
        }

        rowView.setOnClickListener(){
            Log.i(
                ConversationAdapter::class.simpleName,
                "sender_id: ${conversation.sender_id} \n reciever_name: ${conversation.reciever_id} \n"
            )
            val actContext : HomeActivity = context as HomeActivity
            actContext.conversationFragment.user1_id = conversation.reciever_id
            actContext.conversationFragment.user2_id = conversation.sender_id
            if(conversation.reciever_id == Data.id){
                actContext.conversationFragment.user_name = conversation.sender_name
            }
            else{
                actContext.conversationFragment.user_name = conversation.reciever_name
            }
            actContext.supportFragmentManager.beginTransaction().apply {
                replace(R.id.wrapper_layout, actContext.conversationFragment)
                commit()
            }
        }


        return rowView
    }
}