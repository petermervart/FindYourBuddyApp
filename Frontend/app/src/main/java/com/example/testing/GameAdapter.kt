package com.example.testing

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.TextView
import com.example.testing.fragments.FindFragment

class GameAdapter(val context : Context, val data : ArrayList<Advertisement>): BaseAdapter() {
    private val inflater : LayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    override fun getCount(): Int {
        return data.size
    }

    override fun getItem(p0: Int): Any {
      return data[p0]
    }

    override fun getItemId(p0: Int): Long {
       return p0.toLong()
    }

    override fun getView(p0: Int, p1: View?, parent: ViewGroup?): View {

        val AdOne = getItem(p0) as Advertisement

        if(Data.name != AdOne.name) {

            val viewAd = inflater.inflate(R.layout.other_player_layout, parent, false)

            val button = viewAd.findViewById<Button>(R.id.buttonOtherAddFriend)
            val ownerName = viewAd.findViewById<TextView>(R.id.textNameOtherPlayer)
            val adText = viewAd.findViewById<TextView>(R.id.textGameRankOther)
            val GameName = viewAd.findViewById<TextView>(R.id.textInfoGameOtherPlayer)
            val RankName = viewAd.findViewById<TextView>(R.id.textInfoGameOther)


            ownerName.text = AdOne.name
            adText.text = AdOne.text
            GameName.text = AdOne.game_name
            RankName.text = AdOne.rank_name

            button.setOnClickListener(){
                val actContext: HomeActivity = context as HomeActivity
                actContext.supportFragmentManager.beginTransaction().apply {
                   actContext.profileNormalFragment.user_id = AdOne.owner_id
                  replace(R.id.wrapper_layout, actContext.profileNormalFragment)
                  commit()
                }
            }
            return viewAd
        }
        else{
            val viewAd = inflater.inflate(R.layout.my_player_layout, parent, false)

            val button = viewAd.findViewById<Button>(R.id.buttonMyDelete)
            val ownerName = viewAd.findViewById<TextView>(R.id.textNameMyPlayer)
            val adText = viewAd.findViewById<TextView>(R.id.textView360)
            val GameName = viewAd.findViewById<TextView>(R.id.textView150)
            val RankName = viewAd.findViewById<TextView>(R.id.textView260)

            val findfrag = FindFragment()

            ownerName.text = AdOne.name
            adText.text = AdOne.text
            GameName.text = AdOne.game_name
            RankName.text = AdOne.rank_name

            button.setOnClickListener(){
               // findfrag.deleteAd(AdOne.id)
                //findfrag.mergeAds.clear()
                val actContext: HomeActivity = context as HomeActivity
                actContext.findPartnerFrag.deleteAd(AdOne.id)
                //actContext.supportFragmentManager.beginTransaction().apply {

                  //  replace(R.id.wrapper_layout, actContext.findPartnerFrag)
                   // commit()
               // }
            }
            return viewAd

        }

    }
}