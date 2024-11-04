package com.example.testing.fragments

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
import com.google.gson.Gson
import de.hdodenhof.circleimageview.CircleImageView
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class NormalProfileFragment : Fragment() {

    private lateinit var name: TextView
    private lateinit var description: TextView
    private lateinit var sendFriendRequest: Button
    private lateinit var pictureShow: CircleImageView
    var user_id : Int = 0
    var user_name : String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_normal_profile, container, false)
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

    fun postFriendRequest(){

        val service = Data.service

        val gson = Gson()
        val friendRequest : PostFriendRequestResult
        friendRequest = PostFriendRequestResult(PostFriendRequest(Data.id, user_id))
        val friendRequestJson: String = gson.toJson(friendRequest)
        Log.i(FriendRequestsFragment::class.simpleName, friendRequestJson)
        val requestBody = friendRequestJson.toRequestBody("application/json".toMediaTypeOrNull())

        val postFriendRequest = service.postFriendRequests(requestBody)

        postFriendRequest.enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if(response.code()==201){
                    if(context!=null) {
                        Toast.makeText(context!!, "Žiadosť odoslaná", Toast.LENGTH_SHORT).show()
                    }
                }
                else{
                    if(context!=null) {
                        Toast.makeText(context!!, "Žiadosť už existuje", Toast.LENGTH_SHORT).show()
                    }
                }
                Log.i(NormalProfileFragment::class.simpleName, "NEW FRIEND REQUEST CREATED")
            }
            override fun onFailure(call: Call<Void>, t: Throwable) {
                if(context!=null) {
                    Toast.makeText(
                        context!!,
                        "Nepodarilo sa kontaktovať server",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                Log.i(NormalProfileFragment::class.simpleName, "FAILURE")
            }
        })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        name = view.findViewById<EditText>(R.id.profile_name_normal)
        description = view.findViewById<EditText>(R.id.profile_description_normal)
        sendFriendRequest = view.findViewById<Button>(R.id.send_friend_request)
        pictureShow = view.findViewById<CircleImageView>(R.id.profile_picture_normal)
        sendFriendRequest.setOnClickListener() {
            postFriendRequest()
        }
        getProfile(user_id)
    }

}