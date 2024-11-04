package com.example.testing.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ListView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.testing.*
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class FilterFindFragment : Fragment() {
    lateinit var newFilterButton: Button
    lateinit var adsView: ListView
    lateinit var newAdButton : Button

    var advertisementOther = ArrayList<Advertisement>()

    val userId = Data.id
    val userName = Data.name
    val userPassword = Data.password

    fun getAdvertisements(){

        val service = Data.service

        val requestAd = service.getAdFilter(userId, FilterData.game, FilterData.gameRank)

        requestAd.enqueue(object : Callback<AdvertisementResult> {
            override fun onResponse(
                call: Call<AdvertisementResult>, response: Response<AdvertisementResult>
            ) {

                if(response.code() == 200) {
                    val allAds = response.body()
                    if (allAds != null) {
                        if (allAds.results != null) {
                            advertisementOther = allAds.results
                        }
                    }


                    if (advertisementOther != null && activity !=null) {
                        val adapter = GameAdapter(requireActivity(), advertisementOther)
                        adsView.adapter = adapter

                    }

                }
                else{
                    if(context!=null) {
                        Toast.makeText(context!!, "Žiadne inzeráty s vybraným filtrom", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            override fun onFailure(call: Call<AdvertisementResult>, t: Throwable) {
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
        return inflater.inflate(R.layout.fragment_find, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        newFilterButton = view.findViewById(R.id.buttonNewAdvertisementAdd)
        newAdButton = view.findViewById(R.id.AdAddAdvertisement)

        adsView = view.findViewById(R.id.advertisementsList)
        val actContext : HomeActivity = activity as HomeActivity
        newAdButton.setOnClickListener(){
            actContext.supportFragmentManager.beginTransaction().apply {

                replace(R.id.wrapper_layout, actContext.newAdFraq)
                commit()
            }

        }

        newFilterButton.setOnClickListener(){
            actContext.supportFragmentManager.beginTransaction().apply {

                replace(R.id.wrapper_layout, actContext.filterFraq)
                commit()
            }

        }

        getAdvertisements()
    }


}