package com.example.testing.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.example.testing.*
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class FilterFragment : Fragment() {
    lateinit var spinnerGame : Spinner
    lateinit var  spinnerRank : Spinner
    lateinit var buttonAdd : Button
    lateinit var buttonRemove : Button

    var games = ArrayList<Game>()
    var gameName = ArrayList<String>()
    var ranks = ArrayList<Rank>()
    var rankName = ArrayList<String>()

    val userId = Data.id
    val userName = Data.name
    val userPassword = Data.password


    fun getRanks( rankId : Int){

        val service = Data.service

        val rankRequest = service.getRanks(rankId)

        rankRequest.enqueue(object : Callback<RankResult> {
            override fun onResponse(call: Call<RankResult>, response: Response<RankResult>) {
                val allRanks = response.body()
                if (allRanks != null) {
                    ranks = allRanks.results

                    for(c in ranks){
                        rankName.add(c.rank)
                    }
                    if(activity!=null) {
                        val arrayAdapter = ArrayAdapter<String>(
                            context!!,
                            android.R.layout.simple_list_item_1,
                            rankName
                        )
                        spinnerRank.adapter = arrayAdapter

                        spinnerRank.onItemSelectedListener =
                            object : AdapterView.OnItemSelectedListener {
                                override fun onItemSelected(
                                    p0: AdapterView<*>?,
                                    p1: View?,
                                    p2: Int,
                                    p3: Long
                                ) {
                                    for (c in ranks) {
                                        if (c.rank == rankName.get(p2))
                                            FilterData.gameRank = c.id
                                    }

                                }

                                override fun onNothingSelected(p0: AdapterView<*>?) {
                                    if (context != null) {
                                        Toast.makeText(
                                            context!!,
                                            "Nic nie je vybrane",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                            }
                    }
                }

            }

            override fun onFailure(call: Call<RankResult>, t: Throwable) {
                if(context!=null) {
                    Toast.makeText(context!!, "Nieco sa pokazilo", Toast.LENGTH_SHORT).show()
                }
            }

        })

    }

    fun getGames(){

        val service = Data.service

        val gamesRequest = service.getGames()

        gamesRequest.enqueue(object : Callback<GamesResult> {
            override fun onResponse(call: Call<GamesResult>, response: Response<GamesResult>) {
                val allGames = response.body()
                Log.i("TU SOM", allGames.toString())
                if (allGames != null) {
                    games = allGames.results
                    for (c in games){
                        gameName.add(c.name)
                    }
                    if(activity!=null) {
                        val arrayAdapter = ArrayAdapter<String>(
                            context!!,
                            android.R.layout.simple_list_item_1,
                            gameName
                        )

                        spinnerGame.adapter = arrayAdapter

                        spinnerGame.onItemSelectedListener =
                            object : AdapterView.OnItemSelectedListener {
                                override fun onItemSelected(
                                    p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long
                                ) {
                                    for (c in games) {
                                        if (c.name == gameName.get(p2)) {
                                            FilterData.game = c.id
                                            rankName.clear()
                                            getRanks(c.id)
                                        }
                                    }

                                }

                                override fun onNothingSelected(p0: AdapterView<*>?) {
                                    if (context != null) {
                                        Toast.makeText(
                                            context!!,
                                            "Nic nie je vybrane",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                            }
                    }
                }
            }
            override fun onFailure(call: Call<GamesResult>, t: Throwable) {
                Log.i(MainActivity::class.simpleName, "on FAILURE!!!!")
                if(context!=null) {
                    Toast.makeText(context!!, "Nieco sa pokazilo", Toast.LENGTH_SHORT).show()
                }
            }
        })

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_filter, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        spinnerGame = view.findViewById(R.id.FilterGameToFilter)
        spinnerRank = view.findViewById(R.id.FilterRankToFilter)
        buttonAdd = view.findViewById(R.id.ButtoToAddFilter)
        buttonRemove = view.findViewById(R.id.ButtonToDeleteFilter)
        getGames()
        val actContext : HomeActivity = activity as HomeActivity
        buttonAdd.setOnClickListener(){
            actContext.supportFragmentManager.beginTransaction().apply {

                replace(R.id.wrapper_layout, actContext.findPartnerFiltered)
                commit()
            }

        }

        buttonRemove.setOnClickListener(){
            actContext.supportFragmentManager.beginTransaction().apply {

                replace(R.id.wrapper_layout, actContext.findPartnerFrag)
                commit()
            }
        }
    }
}