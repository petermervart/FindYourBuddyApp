package com.example.testing

import android.content.Intent
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import com.example.testing.fragments.*
import com.google.android.material.bottomnavigation.BottomNavigationView
import de.hdodenhof.circleimageview.CircleImageView
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


class HomeActivity : AppCompatActivity() {
    lateinit var conversationFragment : ConversationFragment
    lateinit var profileFriendFragment : FriendProfileFragment
    lateinit var friendRequestsFragment : FriendRequestsFragment
    lateinit var profileNormalFragment : NormalProfileFragment
    lateinit var updateProfileFragment : UpdateProfileFragment
    lateinit var newStatusFrag : NewStatusFragment
    lateinit var homeFrag : HomeFragment
    lateinit var  findPartnerFrag : FindFragment
    lateinit var newAdFraq : NewAdFragment
    lateinit var filterFraq : FilterFragment
    lateinit var  findPartnerFiltered : FilterFindFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        val homeFragment = HomeFragment()
        val friendsFragment = FriendsFragment()
        val chatFragment = ChatFragment()
        val findFragment = FindFragment()
        val profileFragment = ProfileFragment()
        conversationFragment = ConversationFragment()
        profileFriendFragment = FriendProfileFragment()
        friendRequestsFragment = FriendRequestsFragment()
        profileNormalFragment = NormalProfileFragment()
        updateProfileFragment = UpdateProfileFragment()
        newStatusFrag = NewStatusFragment()
        homeFrag = HomeFragment()
        findPartnerFrag = FindFragment()
        newAdFraq = NewAdFragment()
        filterFraq = FilterFragment()
        findPartnerFiltered = FilterFindFragment()
        val menu_bottom = findViewById<BottomNavigationView>(R.id.bottom_menu)
        val menu_top = findViewById<BottomNavigationView>(R.id.top_menu)

        supportFragmentManager.beginTransaction().apply {
            replace(R.id.wrapper_layout, homeFragment)
            commit()
        }
        menu_bottom.setOnNavigationItemSelectedListener{
            when (it.itemId){
                R.id.ic_baseline_home -> {
                    supportFragmentManager.beginTransaction().apply {
                        replace(R.id.wrapper_layout, homeFragment)
                        commit()
                    }
                }
                R.id.ic_baseline_friends -> {
                    supportFragmentManager.beginTransaction().apply {
                        replace(R.id.wrapper_layout, friendsFragment)
                        commit()
                    }
                }
                R.id.ic_baseline_chat -> {
                    supportFragmentManager.beginTransaction().apply {
                        replace(R.id.wrapper_layout, chatFragment)
                        commit()
                    }
                }
                R.id.ic_baseline_find -> {
                    supportFragmentManager.beginTransaction().apply {
                        replace(R.id.wrapper_layout, findFragment)
                        commit()
                    }
                }
            }
            true
        }
        menu_top.setOnNavigationItemSelectedListener{
            when (it.itemId){
                R.id.ic_baseline_profile -> {
                    supportFragmentManager.beginTransaction().apply {
                        replace(R.id.wrapper_layout, profileFragment)
                        commit()
                    }
                }
                R.id.logo_first -> {
                    supportFragmentManager.beginTransaction().apply {
                        replace(R.id.wrapper_layout, homeFragment)
                        commit()
                    }
                }
            }
            true
        }
    }
}