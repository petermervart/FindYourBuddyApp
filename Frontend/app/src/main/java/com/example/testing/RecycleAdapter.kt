package com.example.testing

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.testing.fragments.HomeFragment
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.NonDisposableHandle.parent

class RecycleAdapter(val context : Context, val data: ArrayList<Statuses>): BaseAdapter(){

    private val inflater : LayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    override fun getCount(): Int {
        return  data.size
    }

    override fun getItem(p0: Int): Any {
        return data[p0]
    }

    override fun getItemId(p0: Int): Long {
       return p0.toLong()
    }


    override fun getView(p0: Int, p1: View?, parent: ViewGroup?): View {

        val viewStatus = inflater.inflate(R.layout.card_layout, parent, false)

        val ownerName = viewStatus.findViewById<TextView>(R.id.textName)

        val ownerStatusText = viewStatus.findViewById<TextView>(R.id.textStatus)

        val statusOne = getItem(p0) as Statuses
        if(statusOne.owner_name == Data.name) {
            viewStatus.setBackgroundColor(Color.CYAN)
            val fragmentHome = HomeFragment()
           // val actContext : HomeActivity = context as HomeActivity
            viewStatus.setOnClickListener(){
             fragmentHome.deleteStatus(statusOne.status_id)
               // actContext.homeFrag.deleteStatus(statusOne.status_id)


            }
        }
        ownerName?.text = statusOne.owner_name

        ownerStatusText?.text = statusOne.text
        return viewStatus
    }
}