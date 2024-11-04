package com.example.testing.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ListView
import android.widget.Toast
import com.example.testing.*
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


class FriendsFragment : Fragment() {
    lateinit var listView : ListView
    lateinit var friendRequestsButton : Button
    var friends = ArrayList<Friend>()

    fun getFriends(id: Int){

        val service = Data.service

        val friendsRequest = service.getFriends(id)

        friendsRequest.enqueue(object : Callback<FriendResult> {
            override fun onResponse(call: Call<FriendResult>, response: Response<FriendResult>) {
                val allFriends = response.body()
                for (c in allFriends?.results!!) {
                    Log.i(
                        FriendsFragment::class.simpleName,
                        "id: ${c.id} \n name: ${c.name} \n"
                    )
                }
                friends = allFriends.results
                if(activity!=null && response.code()==200) {
                    val adapter = FriendAdapter(requireActivity(), friends)
                    listView.adapter = adapter
                    if(context!=null) {
                        Toast.makeText(context!!, "Priatelia získaní", Toast.LENGTH_SHORT).show()
                    }
                }
                else{
                    if(context!=null) {
                        Toast.makeText(context!!, "Chyba pri získaní priateľov", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            }
            override fun onFailure(call: Call<FriendResult>, t: Throwable) {
                Log.i(MainActivity::class.simpleName, "on FAILURE!!!!")
                if(context!=null) {
                    Toast.makeText(
                        context!!,
                        "Nepodarilo sa kontaktovať server",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        })
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_friends, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        listView = view.findViewById<ListView>(R.id.friends_list)
        friendRequestsButton = view.findViewById<Button>(R.id.friend_requests)
        friendRequestsButton.setOnClickListener() {
            val actContext : HomeActivity = activity as HomeActivity
            actContext.supportFragmentManager.beginTransaction().apply {
                replace(R.id.wrapper_layout, actContext.friendRequestsFragment)
                commit()
            }
        }
        getFriends(Data.id)
    }
}