package com.rugved.chatgptapp

import android.os.Bundle
import android.util.Log
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import okio.IOException
import org.json.JSONArray
import org.json.JSONObject

class MainActivity : AppCompatActivity() {
    private val client = OkHttpClient()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val etPrompt = findViewById<EditText>(R.id.editText_prompt)
        val btnSubmit = findViewById<Button>(R.id.button_submit)
        val tvResponse = findViewById<TextView>(R.id.textView_response)

        btnSubmit.setOnClickListener {
            val textPrompt = etPrompt.text.toString()
            try {
                val imm: InputMethodManager =
                    getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(currentFocus!!.windowToken, 0)
            } catch (e: Exception) {
                Log.e("Error","Keyboard error")
            }
            if(textPrompt.isNotEmpty()){
                Toast.makeText(this, "Results loading... Please wait.", Toast.LENGTH_SHORT).show()
                getResponse(textPrompt) {response ->
                    runOnUiThread{
                        tvResponse.text = response
                    }
                }
            } else {
                Toast.makeText(this, "Please enter valid text input.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun getResponse(prompt: String, callback: (String) -> Unit){
        val apiKey = "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"
        val url = "https://api.openai.com/v1/engines/text-davinci-003/completions"

        val requestBody = """
        {
            "prompt" : "$prompt",
            "max_tokens" : 500,
            "temperature" : 0
        }
        """.trimIndent()

        val request = Request.Builder()
            .url(url)
            .addHeader("Content-Type", "application/json")
            .addHeader("Authorization", "Bearer $apiKey")
            .post(requestBody.toRequestBody("application/json".toMediaTypeOrNull()))
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("error", "onFailure: ", e)
            }

            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string()
                if (body != null) {
                    Log.v("data", body)
                } else {
                    Log.v("data", "empty")
                }
                val jsonObject = JSONObject(body)
                val jsonArray: JSONArray = jsonObject.getJSONArray("choices")
                val textResult = jsonArray.getJSONObject(0).getString("text")
                callback(textResult)
            }
        })
    }
}