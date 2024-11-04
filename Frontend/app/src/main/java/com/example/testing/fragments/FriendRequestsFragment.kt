package com.example.testing.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import com.example.testing.*
import com.google.gson.Gson
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class FriendRequestsFragment : Fragment() {
    lateinit var listView : ListView
    var friendRequests = ArrayList<FriendRequest>()
    var sender_id : Int = 0
    var reciever_id : Int = 0
    var user_name : String = ""


    fun getFriendRequests(id: Int){

        val service = Data.service

        val friend_requestRequest = service.getFriendRequests(id)

        friend_requestRequest.enqueue(object : Callback<FriendRequestResult> {
            override fun onResponse(call: Call<FriendRequestResult>, response: Response<FriendRequestResult>) {
                val allFriendRequests = response.body()
                for (c in allFriendRequests?.results!!) {
                    Log.i(
                        FriendsFragment::class.simpleName,
                        "id: ${c.id} \n"
                    )
                }
                friendRequests = allFriendRequests.results
                if(activity!=null && response.code()==200) {
                    val adapter = FriendRequestAdapter(requireActivity(), friendRequests)
                    listView.adapter = adapter
                    if(context!=null) {
                        Toast.makeText(context!!, "Žiadosti získané", Toast.LENGTH_SHORT).show()
                    }
                }
                else{
                    if(context!=null) {
                        Toast.makeText(context!!, "Chyba pri získaní žiadostí", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            }
            override fun onFailure(call: Call<FriendRequestResult>, t: Throwable) {
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

    fun postFriendship(){

        val service = Data.service

        val gson = Gson()
        val friendship : PostFriendResult
        friendship = PostFriendResult(PostFriend(sender_id, reciever_id))
        val friendshipJson: String = gson.toJson(friendship)
        Log.i(FriendRequestsFragment::class.simpleName, friendshipJson)
        val requestBody = friendshipJson.toRequestBody("application/json".toMediaTypeOrNull())

        val postFriendRequest = service.postFriends(requestBody)

        postFriendRequest.enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.code()==201) {
                    Log.i(FriendRequestsFragment::class.simpleName, "NEW FRIENDSHIP CREATED")
                    deleteFriendRequest()
                    if(context!=null) {
                        Toast.makeText(context!!, "Priateľstvo vytvorené", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
                else{
                    if(context!=null) {
                        Toast.makeText(
                            context!!,
                            "Chyba pri vytvorení priateľstva",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
            override fun onFailure(call: Call<Void>, t: Throwable) {
                Log.i(FriendRequestsFragment::class.simpleName, "FAILURE")
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

    fun deleteFriendRequest(){

        val service = Data.service

        val friendRequest = service.deleteFriendRequests(sender_id, reciever_id)

        friendRequest.enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if(response.code()==204) {
                    getFriendRequests(Data.id)
                    Log.i(FriendRequestsFragment::class.simpleName, "FRIEND REQUEST DELETED")
                    if(context!=null) {
                        Toast.makeText(
                            context!!,
                            "Žiadosť o priateľstvo vymazaná",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
                else{
                    if(context!=null) {
                        Toast.makeText(
                            context!!,
                            "Chyba pri vymazaní žiadosti o priateľstvo",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
            override fun onFailure(call: Call<Void>, t: Throwable) {
                Toast.makeText(context!!, "Nepodarilo sa kontaktovať server", Toast.LENGTH_SHORT).show()
                if(context!=null) {
                    Log.i(FriendRequestsFragment::class.simpleName, "FAILURE")
                }
            }
        })
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_friend_requests, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        listView = view.findViewById<ListView>(R.id.friends_requests_list)
        getFriendRequests(Data.id)

    }
}