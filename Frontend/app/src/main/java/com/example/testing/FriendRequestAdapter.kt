package com.example.testing

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView

class FriendRequestAdapter(val context: Context,
                    val dataSource: ArrayList<FriendRequest>) : BaseAdapter() {

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

        val friendRequest = getItem(position) as FriendRequest

        if(friendRequest.reciever_id == Data.id){
            val rowView = inflater.inflate(R.layout.friend_request_layout, parent, false)
            val nameFriend = rowView.findViewById<TextView>(R.id.name_request)
            val buttonAccept = rowView.findViewById<ImageView>(R.id.accept_request)
            val buttonDeny = rowView.findViewById<ImageView>(R.id.deny_request)
            val actContext : HomeActivity = context as HomeActivity
            actContext.friendRequestsFragment.reciever_id = friendRequest.reciever_id
            actContext.friendRequestsFragment.sender_id = friendRequest.sender_id
            actContext.profileNormalFragment.user_id = friendRequest.sender_id
            nameFriend.text = friendRequest.sender_name
            buttonAccept.setOnClickListener(){
                actContext.friendRequestsFragment.postFriendship()
            }

            buttonDeny.setOnClickListener(){
                actContext.friendRequestsFragment.deleteFriendRequest()
            }
            rowView.setOnClickListener(){
                actContext.supportFragmentManager.beginTransaction().apply {
                    replace(R.id.wrapper_layout, actContext.profileNormalFragment)
                    commit()
                }
            }
            return rowView
        }
        else{
            val rowView = inflater.inflate(R.layout.friend_request_not_owner, parent, false)
            val nameFriend = rowView.findViewById<TextView>(R.id.name_request)
            val buttonDeny = rowView.findViewById<ImageView>(R.id.deny_request)
            val actContext : HomeActivity = context as HomeActivity
            actContext.friendRequestsFragment.reciever_id = friendRequest.reciever_id
            actContext.friendRequestsFragment.sender_id = friendRequest.sender_id
            nameFriend.text = friendRequest.reciever_name
            actContext.profileNormalFragment.user_id = friendRequest.reciever_id
            buttonDeny.setOnClickListener(){
                actContext.friendRequestsFragment.deleteFriendRequest()
            }
            rowView.setOnClickListener(){
                actContext.supportFragmentManager.beginTransaction().apply {
                    replace(R.id.wrapper_layout, actContext.profileNormalFragment)
                    commit()
                }
            }
            return rowView
        }

    }
}