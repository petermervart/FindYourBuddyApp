package com.example.testing.fragments

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

class HomeFragment : Fragment() {

    lateinit var newStatusButton: Button
    lateinit var statusView: ListView

    var status = ArrayList<Statuses>()

    val userId = Data.id
    val userName = Data.name
    val userPassword = Data.password

    fun deleteStatus(statusId: Int) {

        val service = Data.service

        val statusDeleteRequest = service.deleteStatus(statusId)

        statusDeleteRequest.enqueue(object : Callback<Void>{
            override fun onResponse(call: Call<Void>, response: Response<Void>) {

                Log.i(HomeFragment::class.simpleName, "Status deleted")
                if(context!=null) {
                    Toast.makeText(context!!, "Status zmazaný, obnovte stránku", Toast.LENGTH_SHORT)
                        .show()
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Log.i(HomeFragment::class.simpleName, "Something went wrong")
                if(context!=null) {
                    Toast.makeText(context!!, "Nepodarilo sa kontaktovať server", Toast.LENGTH_SHORT).show()
                }
            }

        })

    }

    fun getStatuses(userId : Int){

        val service = Data.service

        val statusesRequest = service.getStatuses(userId)

        statusesRequest.enqueue(object :Callback<StatusesResult>{

            override fun onResponse(call: Call<StatusesResult>, response: Response<StatusesResult>) {

                val statusesAll = response.body()

                if (statusesAll != null) {
                    status = statusesAll.results
                }
                if(activity!=null) {
                    val adapter = RecycleAdapter(requireActivity(), status)
                    statusView.adapter = adapter
                }
            }

            override fun onFailure(call: Call<StatusesResult>, t: Throwable) {
                if(context!= null) {
                    Toast.makeText(context!!, "Nepodarilo sa kontaktovať server", Toast.LENGTH_SHORT).show()
                }
            }

        })
    }

    override fun onCreateView( inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?

    ): View? {
        val root = inflater.inflate(R.layout.fragment_home, container, false)


        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        newStatusButton = view.findViewById(R.id.buttonNewAdvertisementAdd)
        val actContext : HomeActivity = activity as HomeActivity

       newStatusButton.setOnClickListener(){

           actContext.supportFragmentManager.beginTransaction().apply {

               replace(R.id.wrapper_layout, actContext.newStatusFrag)
               commit()
           }
        }
        statusView = view.findViewById(R.id.advertisementsList)

        getStatuses(userId)
    }


}