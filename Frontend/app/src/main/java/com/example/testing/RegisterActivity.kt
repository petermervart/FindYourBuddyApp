package com.example.testing

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.google.gson.Gson
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.*
import retrofit2.converter.gson.GsonConverterFactory

class RegisterActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val button = findViewById<Button>(R.id.buttonReg)

        button.setOnClickListener(){

            val nameText = findViewById<EditText>(R.id.editTextTextPersonName2)
            val passwdText = findViewById<EditText>(R.id.editTextTextPassword2)
            val emailText = findViewById<EditText>(R.id.editTextTextEmailAddress)

            val name = nameText.text
            val passwd = passwdText.text
            val email = emailText.text

            if (validate(name, passwd, email)){
                postRegister(name,passwd,email)
            }

        }
    }

    fun validate(name: Editable, passwd: Editable, email: Editable): Boolean {

        if (name.trim().isEmpty()){
            Toast.makeText(this, "Chýba meno, heslo alebo email", Toast.LENGTH_SHORT).show()
            return false
        }
        if (passwd.trim().isEmpty()){
            Toast.makeText(this, "Chýba meno, heslo alebo email", Toast.LENGTH_SHORT).show()
            return false
        }

        if (email.trim().isEmpty()){
            Toast.makeText(this, "Chýba meno, heslo alebo email", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    fun postRegister(name: Editable,passwd: Editable,email: Editable){

        val url = Data.ip_server
        val retrofit = Retrofit.Builder().baseUrl(url).addConverterFactory(GsonConverterFactory.create()).build()
        val service = retrofit.create(Api::class.java)

        val gson = Gson()

        val newUser = RegisterResult(Register(name.trim().toString(), passwd.trim().toString(), email.trim().toString()))

        val newUserJson : String = gson.toJson(newUser)

        val requestBody= newUserJson.toRequestBody("aplication/json".toMediaTypeOrNull())

        val postRegisterRequest = service.postRegister(requestBody)

        postRegisterRequest.enqueue(object : Callback<Void>{
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if(response.code()==204) {
                    Toast.makeText(this@RegisterActivity, "Registrácia bola úspešná",Toast.LENGTH_SHORT).show()
                    val intent = Intent(this@RegisterActivity, LoginActivity::class.java)
                    startActivity(intent)
                    finish()
                }
                if(response.code() == 422){
                    Toast.makeText(this@RegisterActivity, "Zlý formát emailu",Toast.LENGTH_SHORT).show()
                }

                if(response.code() == 400){
                    Toast.makeText(this@RegisterActivity,"Meno sa už používa, je príliš dlhé alebo krátke",Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Toast.makeText(this@RegisterActivity, "Nepodarilo sa kontaktovať server", Toast.LENGTH_SHORT).show()
            }


        })

    }
}