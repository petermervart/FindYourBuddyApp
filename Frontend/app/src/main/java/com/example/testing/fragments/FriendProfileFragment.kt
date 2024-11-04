package com.example.testing.fragments

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.example.testing.*
import de.hdodenhof.circleimageview.CircleImageView
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class FriendProfileFragment : Fragment() {

    private lateinit var name: TextView
    private lateinit var description: TextView
    private lateinit var deleteFriendship: Button
    private lateinit var writeMesssageButton: Button
    private lateinit var pictureShow: CircleImageView
    var user_id : Int = 0
    var user_name : String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_friend_profile, container, false)
    }

    fun getProfile( id: Int){

        val service = Data.service

        val profileRequest = service.getProfile(id)

        profileRequest.enqueue(object : Callback<ProfileResult> {
            override fun onResponse(call: Call<ProfileResult>, response: Response<ProfileResult>) {
                val allProfile = response.body()
                Log.i("TU SOM", allProfile.toString())
                val result = allProfile?.user!!
                if(name!=null && result.name != null) {
                    name.setText(result.name)
                }
                if(pictureShow!=null && result.picture != null) {
                    val imageBytes = Base64.decode(result.picture, Base64.DEFAULT)
                    val decodedImage = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                    pictureShow.setImageBitmap(decodedImage)
                }
                if(description!=null && result.description != null) {
                    description.setText(result.description)
                }
                if(response.code()!=200){
                    if(context!=null) {
                        Toast.makeText(context!!, "Chyba pri získaní profilu", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            }
            override fun onFailure(call: Call<ProfileResult>, t: Throwable) {
                Log.i(FriendProfileFragment::class.simpleName, "FAILURE")
                Log.i(FriendProfileFragment::class.simpleName,"requestFailed", t)
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

    fun deleteFriendship( id: Int){

        val service = Data.service

        val friendRequest = service.deleteFriends(id, user_id)

        friendRequest.enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                Log.i(ConversationFragment::class.simpleName, "FRIENDSHIP DELETED")
            }
            override fun onFailure(call: Call<Void>, t: Throwable) {
                Log.i(ConversationFragment::class.simpleName, "FAILURE")
            }
        })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        name = view.findViewById<EditText>(R.id.profile_name_friend)
        description = view.findViewById<EditText>(R.id.profile_description_friend)
        deleteFriendship = view.findViewById<Button>(R.id.unfriend)
        writeMesssageButton = view.findViewById<Button>(R.id.write_message)
        pictureShow = view.findViewById<CircleImageView>(R.id.profile_picture_friend)
        deleteFriendship.setOnClickListener() {
            deleteFriendship(Data.id)
        }
        writeMesssageButton.setOnClickListener() {
            val actContext : HomeActivity = context as HomeActivity
            actContext.conversationFragment.user1_id = Data.id
            actContext.conversationFragment.user2_id = user_id
            actContext.conversationFragment.user_name = user_name
            actContext.supportFragmentManager.beginTransaction().apply {
                replace(R.id.wrapper_layout, actContext.conversationFragment)
                commit()
            }
        }
        getProfile(user_id)
    }

}