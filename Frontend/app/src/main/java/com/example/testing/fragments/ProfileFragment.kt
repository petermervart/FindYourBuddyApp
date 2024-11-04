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
import com.google.gson.GsonBuilder
import de.hdodenhof.circleimageview.CircleImageView
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ProfileFragment : Fragment() {

    private lateinit var name: TextView
    private lateinit var description: TextView
    private lateinit var updateProfileButton: Button
    private lateinit var pictureShow: CircleImageView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    fun getProfile( id: Int){

        val service = Data.service

        val profileRequest = service.getProfile(id)

        profileRequest.enqueue(object : Callback<ProfileResult> {
            override fun onResponse(call: Call<ProfileResult>, response: Response<ProfileResult>) {
                val allProfile = response.body()
                Log.i("TU SOM", allProfile.toString())
                val result = allProfile?.user!!
                if(name!=null && result.name!=null) {
                    name.setText(result.name)
                }
                if(pictureShow!=null && result.picture!=null) {
                    val imageBytes = Base64.decode(result.picture, Base64.DEFAULT)
                    val decodedImage = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                    pictureShow.setImageBitmap(decodedImage)
                }
                if(description!=null && result.description!=null) {
                    description.setText(result.description)
                }
                if(updateProfileButton!=null) {
                    updateProfileButton.setOnClickListener() {
                        val actContext : HomeActivity = activity as HomeActivity
                        actContext.supportFragmentManager.beginTransaction().apply {
                            replace(R.id.wrapper_layout, actContext.updateProfileFragment)
                            commit()
                        }
                    }
                }
                Log.i(
                    MainActivity::class.simpleName,
                    "ID: ${result.name} \n NAME: ${result.picture} \n DESCRIPTION: ${result.description} "
                )
                if(response.code()==200){
                    if(context!=null){
                        Toast.makeText(context, "Profil získaný", Toast.LENGTH_SHORT).show()
                    }
                }
                else{
                    if(context!=null) {
                        Toast.makeText(context, "Nepodarilo sa získať profil", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            }
            override fun onFailure(call: Call<ProfileResult>, t: Throwable) {
                Log.i(MainActivity::class.simpleName, "FAILURE")
                Log.i(MainActivity::class.simpleName,"requestFailed", t)
                if(context!=null) {
                    Toast.makeText(context, "Nepodarilo sa kontaktovať server", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        name = view.findViewById<EditText>(R.id.profile_name)
        description = view.findViewById<EditText>(R.id.profile_description)
        updateProfileButton = view.findViewById<Button>(R.id.changeProfile)
        pictureShow = view.findViewById<CircleImageView>(R.id.profile_picture)
        getProfile(Data.id)
    }

}