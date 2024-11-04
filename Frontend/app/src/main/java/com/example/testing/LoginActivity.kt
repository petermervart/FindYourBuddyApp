package com.example.testing

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import okhttp3.OkHttpClient
import retrofit2.*
import retrofit2.converter.gson.GsonConverterFactory

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val button = findViewById<Button>(R.id.buttonToLogin)
        button.setOnClickListener(){
            val nameText = findViewById<EditText>(R.id.editTextTextPersonName)
            val passwdText = findViewById<EditText>(R.id.editTextTextPassword)

            val name = nameText.text
            val passwd = passwdText.text
            if (validate(name, passwd)){
                getLogin(name,passwd)
            }
           // val intent = Intent(this, HomeActivity::class.java)
            //startActivity(intent)
        }
    }

     fun validate(name: Editable, passwd: Editable): Boolean {

        if (name.trim().isEmpty()){
            Toast.makeText(this, "Missing Username or Password", Toast.LENGTH_SHORT).show()
            return false
        }
        if (passwd.trim().isEmpty()){
            Toast.makeText(this, "Missing Username or Password", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

     fun getLogin(name: Editable, passwd: Editable){

         val url = Data.ip_server
         val client = OkHttpClient.Builder().addInterceptor(Authorization(name.trim().toString(),passwd.trim().toString())).build()

         val retrofit = Retrofit.Builder().baseUrl(url).client(client).addConverterFactory(GsonConverterFactory.create()).build()

         val service = retrofit.create(Api::class.java)

         val loginRequest = service.getLogin()

        loginRequest.enqueue(object : Callback<LoginResult>{

            override fun onResponse(call: Call<LoginResult>, response: Response<LoginResult>) {

                if(response.code() == 200) {
                    Toast.makeText(this@LoginActivity, "Úspešne prihlásený", Toast.LENGTH_SHORT)
                        .show()
                    val loggedUser = response.body()
                    val checkLoggedUser = loggedUser?.result!!

                    Data.id =  checkLoggedUser.id

                    Data.name = checkLoggedUser.name
                    Data.password = passwd.trim().toString()

                    val url = Data.ip_server
                    val client = OkHttpClient.Builder().addInterceptor(Authorization(Data.name, Data.password)).build()

                    val retrofit = Retrofit.Builder()
                        .baseUrl(url)
                        .client(client)
                        .addConverterFactory(GsonConverterFactory.create())
                        .build()

                    Data.service = retrofit.create(Api::class.java)

                    val intent = Intent(this@LoginActivity, HomeActivity::class.java)
                    startActivity(intent)
                    finish()
                }

                else{

                    Toast.makeText(this@LoginActivity, "Zlé prihlasovacie údaje", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<LoginResult>, t: Throwable) {

                Toast.makeText(this@LoginActivity, "Nepodarilo sa kontaktovať server", Toast.LENGTH_SHORT).show()
            }


        })

    }

 }


