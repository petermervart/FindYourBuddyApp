package com.example.testing.fragments

import android.R.attr
import android.content.ContentResolver
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.testing.*
import com.google.gson.Gson
import de.hdodenhof.circleimageview.CircleImageView
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.ByteArrayOutputStream


class UpdateProfileFragment : Fragment() {
    private lateinit var nameField: EditText
    private lateinit var descriptionField: EditText
    private lateinit var updateProfileButton: Button
    private lateinit var uploadPictureButton: Button
    private lateinit var pictureShow: CircleImageView
    private var pictureString : String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val button = activity?.findViewById<Button>(R.id.updateProfile)
        button?.setOnClickListener() {

        }
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_update_profile, container, false)
    }

    fun putProfile(id: Int, new_name: String, description: String, picture: String?){

        val service = Data.service

        val gson = Gson()
        var new_nameString : String? = new_name
        var descriptionString : String? = description
        if(new_name.length > 35){
            Toast.makeText(requireContext(), "Meno veľmi dlhé", Toast.LENGTH_SHORT).show()
            return
        }
        if(description.length > 500){
            Toast.makeText(requireContext(), "Opis veľmi dlhý", Toast.LENGTH_SHORT).show()
            return
        }
        if(new_name == ""){
            new_nameString = null
        }
        if(description == ""){
            descriptionString = null
        }
        val profile = ProfileResult(Profile(new_nameString, picture, descriptionString))
        val profileJson: String = gson.toJson(profile)
        Log.i(UpdateProfileFragment::class.simpleName, profileJson)
        val requestBody = profileJson.toRequestBody("application/json".toMediaTypeOrNull())

        val putProfileRequest = service.putProfile(id, requestBody)

        putProfileRequest.enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if(response.code()==204){
                    if(context!=null) {
                        Toast.makeText(context!!, "Profil upravený", Toast.LENGTH_SHORT).show()
                    }
                }
                else{
                    if(context!=null) {
                        Toast.makeText(context!!, "Chyba pri úprave profilu", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
                Log.i(UpdateProfileFragment::class.simpleName, "UPDATED")
            }
            override fun onFailure(call: Call<Void>, t: Throwable) {
                if(context!=null) {
                    Toast.makeText(
                        context!!,
                        "Nepodarilo sa kontaktovať server",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                Log.i(UpdateProfileFragment::class.simpleName, "FAILURE")
            }
        })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        nameField = view.findViewById<EditText>(R.id.update_profile_name)
        descriptionField = view.findViewById<EditText>(R.id.update_profile_description)
        updateProfileButton = view.findViewById<Button>(R.id.updateProfile)
        uploadPictureButton = view.findViewById<Button>(R.id.uploadPicture)
        pictureShow = view.findViewById<CircleImageView>(R.id.picture_update_show)
        Log.i(ProfileFragment::class.simpleName, Data.name)
        Log.i(ProfileFragment::class.simpleName, Data.password)
        uploadPictureButton.setOnClickListener() {
            var intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, 25)
        }
        updateProfileButton.setOnClickListener() {
            putProfile(Data.id, nameField.text.toString(), descriptionField.text.toString(), pictureString)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == 25){
            Log.i(ProfileFragment::class.simpleName, data?.data.toString())
            pictureShow.setImageURI(data?.data)
            val uri: Uri = data?.data!!
            val bitmap = MediaStore.Images.Media.getBitmap(getActivity()?.getContentResolver(), uri)
            val stream = ByteArrayOutputStream()
            val cr = getActivity()?.getContentResolver()
            val mime = cr?.getType(uri)
            Log.i(ProfileFragment::class.simpleName, mime!!)
            if(mime == "image/png"){
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            }
            else{
                bitmap.compress(Bitmap.CompressFormat.JPEG, 20, stream)
            }
            val bytes = stream.toByteArray()
            pictureString = Base64.encodeToString(bytes, Base64.DEFAULT)
            Log.i(ProfileFragment::class.simpleName, pictureString!!)
        }
    }

}