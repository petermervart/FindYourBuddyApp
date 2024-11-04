package com.example.testing.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
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

class ConversationFragment : Fragment() {
    lateinit var listView : ListView
    var messages = ArrayList<Message>()
    var user1_id : Int = 0
    var user2_id : Int = 0
    var user_name : String = ""

    fun getMessages(id1: Int, id2: Int){
        val service = Data.service

        val conversationRequest = service.getMessages(id1, id2)

        conversationRequest.enqueue(object : Callback<MessageResult> {
            override fun onResponse(call: Call<MessageResult>, response: Response<MessageResult>) {
                val allMessages = response.body()
                Log.i("TU SOM", allMessages.toString())
                for (c in allMessages?.results!!) {
                    Log.i(
                        MainActivity::class.simpleName,
                        "reciver_id: ${c.reciever_id} \n reciever_name: ${c.reciever_name} \n sender_name: ${c.sender_name} "
                    )
                }
                messages = allMessages.results
                if(activity!=null && response.code()==200) {
                    val adapter = MessageAdapter(requireActivity(), messages)
                    listView.adapter = adapter
                    if(context!=null) {
                        Toast.makeText(context, "Správy získané", Toast.LENGTH_SHORT).show()
                    }
                }
                else{
                    if(context!=null) {
                        Toast.makeText(
                            context,
                            "Nepodarilo sa získať správy zo servera",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
            override fun onFailure(call: Call<MessageResult>, t: Throwable) {
                Log.i(MainActivity::class.simpleName, "on FAILURE!!!!")
                if(context!=null){
                    Toast.makeText(context!!, "Nemožno kontakotvať server", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    fun postMessage(text: String){

        val service = Data.service

        val gson = Gson()
        val message : PostMessageResult
        if(user1_id == Data.id){
            message = PostMessageResult(PostMessage(user1_id, user2_id, text))
        }
        else{
            message = PostMessageResult(PostMessage(user2_id, user1_id, text))
        }
        val messageJson: String = gson.toJson(message)
        Log.i(ConversationFragment::class.simpleName, messageJson)
        val requestBody = messageJson.toRequestBody("application/json".toMediaTypeOrNull())

        val postMessageRequest = service.postMessage(requestBody)

        postMessageRequest.enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                Log.i(ConversationFragment::class.simpleName, "NEW MESSAGE CREATED")
                getMessages(user1_id, user2_id)
            }
            override fun onFailure(call: Call<Void>, t: Throwable) {
                Log.i(ConversationFragment::class.simpleName, "FAILURE")
            }
        })
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_conversation, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val videoChatButton = view.findViewById<Button>(R.id.videochat)
        videoChatButton.setOnClickListener() {
            val intent = Intent(activity, MainActivityWebRTC::class.java)
            startActivity(intent)
        }
        val messageEditText = view.findViewById<EditText>(R.id.text_message)
        val sendMessageButton = view.findViewById<Button>(R.id.send_message)
        sendMessageButton.setOnClickListener() {
            if(messageEditText.text.toString() != ""){
                postMessage(messageEditText.text.toString())
                messageEditText.setText("")
            }
        }
        listView = view.findViewById<ListView>(R.id.messages_list)
        val chatName = view.findViewById<TextView>(R.id.conversation_name)
        chatName.text = user_name
        getMessages(user1_id, user2_id)
    }
}