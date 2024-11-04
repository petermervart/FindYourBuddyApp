package com.example.testing.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
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

class NewStatusFragment : Fragment() {

    lateinit var statusText : EditText
    lateinit var addStatus : Button

    val userId = Data.id
    val userName = Data.name
    val userPassword = Data.password

    fun postStatus(text : String){

        val service = Data.service

        val gson = Gson()

        val statusNew = NewStatusResult(NewStatus(userId,text))

        val statusNewToJson : String = gson.toJson(statusNew)

        val requestStatus = statusNewToJson.toRequestBody("application/json".toMediaTypeOrNull())

        val postRequestNewStatus = service.postStatuses(requestStatus)

        postRequestNewStatus.enqueue(object : Callback<Void>{
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.code() == 204) {
                    val actContext: HomeActivity = activity as HomeActivity
                    if(context!=null) {
                        Toast.makeText(context!!, "Status pridaný", Toast.LENGTH_SHORT).show()
                    }
                    actContext.supportFragmentManager.beginTransaction().apply {

                        replace(R.id.wrapper_layout, actContext.homeFrag)
                        commit()
                    }
                }
                if (response.code() == 400 && context!=null){
                    Toast.makeText(context!!, "Žiaden text v statuse", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<Void>, t: Throwable) {
                if(context!=null) {
                    Toast.makeText(context!!, "Nepodarilo sa kontaktovať server", Toast.LENGTH_SHORT).show()
                }
            }

        })


    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        return inflater.inflate(R.layout.fragment_new_status, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        statusText = view.findViewById(R.id.editTextTextPersonStatus3)
        addStatus  = view.findViewById(R.id.buttonToAddStatus)

        addStatus.setOnClickListener(){
            postStatus(statusText.text.toString())

        }
    }


}