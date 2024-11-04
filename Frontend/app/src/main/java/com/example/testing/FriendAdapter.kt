package com.example.testing

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView

class FriendAdapter(val context: Context,
                          val dataSource: ArrayList<Friend>) : BaseAdapter() {

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

        val rowView = inflater.inflate(R.layout.friendship_layout, parent, false)

        val nameFriend = rowView.findViewById<TextView>(R.id.friend_name)

        val friend = getItem(position) as Friend

        nameFriend.text = friend.name

        rowView.setOnClickListener(){
            val actContext : HomeActivity = context as HomeActivity
            actContext.profileFriendFragment.user_id = friend.id
            actContext.profileFriendFragment.user_name = friend.name
            actContext.supportFragmentManager.beginTransaction().apply {
                replace(R.id.wrapper_layout, actContext.profileFriendFragment)
                commit()
            }
        }

        return rowView
    }
}