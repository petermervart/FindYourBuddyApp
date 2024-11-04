package com.example.testing.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.example.testing.*
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ChatFragment : Fragment() {

    lateinit var listView : ListView
    var conversations = ArrayList<Conversation>()

    fun getLastMessages(id: Int){
        val service = Data.service

        val conversationRequest = service.getConversations(id)

        conversationRequest.enqueue(object : Callback<ConversationResult> {
            override fun onResponse(call: Call<ConversationResult>, response: Response<ConversationResult>) {
                val allConversations = response.body()
                Log.i("TU SOM", allConversations.toString())
                for (c in allConversations?.results!!) {
                    Log.i(
                        MainActivity::class.simpleName,
                        "reciver_id: ${c.reciever_id} \n reciever_name: ${c.reciever_name} \n sender_name: ${c.sender_name} "
                    )
                }
                conversations = allConversations.results
                if(activity!=null && response.code()==200) {
                    val adapter = ConversationAdapter(requireActivity(), conversations)
                    listView.adapter = adapter
                    if(context!=null) {
                        Toast.makeText(context!!, "Konverzácie získané", Toast.LENGTH_SHORT).show()
                    }
                }
                else{
                    if(context!=null) {
                        Toast.makeText(
                            context!!,
                            "Nepodarilo sa získať konverzácie zo servera",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
            override fun onFailure(call: Call<ConversationResult>, t: Throwable) {
                Log.i(MainActivity::class.simpleName, "on FAILURE!!!!")
                if(context!=null) {
                    Toast.makeText(context!!, "Nemožno kontakotvať server", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        })
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_chat, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        listView = view.findViewById<ListView>(R.id.conversations_list)
        getLastMessages(Data.id)
    }
}