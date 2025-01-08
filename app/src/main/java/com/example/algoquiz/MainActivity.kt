package com.example.algoquiz
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.widget.EditText
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okio.IOException
import org.json.JSONObject
import androidx.lifecycle.LifecycleOwner
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.Query
import com.google.gson.Gson
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase


class MainActivity : AppCompatActivity() {
    private var questionId = 0
    private val client = OkHttpClient()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        //firebase setup
        val db = FirebaseFirestore.getInstance()
        val messagesRef = db.collection("messages")
         val scrollView = findViewById<ScrollView>(R.id.conversationView)

        val conversation = mutableListOf<Map<String, String>>()
        //newer messages wont display after 20 msgs is reached,
        //potential fix1: force user to clear and restart after 10 messages
        //potential fix2: allow unlimited messages, but shrink convo to last 10 before sending to the api
        messagesRef
            .orderBy("timeStamp", Query.Direction.ASCENDING)
            .limit(20)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    return@addSnapshotListener
                }
                conversation.clear() // Clear previous data
                for (document in snapshot!!) {
                    val docID = document.get("questionID")?.toString()
                    if(docID == questionId.toString()){
                        val content = document.getString("content") ?: "No content"
                        val role = document.getString("role") ?: "user"
                        val msg = mapOf("role" to role, "content" to content)
                        conversation.add(msg)
                    }
                }
                Log.d("msgList", "$conversation")
                createTextView(conversation)
            }




        // Initialize questionId from intent
        questionId = intent.getIntExtra("questionID", 0)

        // Buttons and other components:
        val nextButton = findViewById<Button>(R.id.next)
        val prevButton = findViewById<Button>(R.id.previous)
        val textInput = findViewById<EditText>(R.id.userInput)
        val viewText = findViewById<TextView>(R.id.algoAnswer)
        val clearButton = findViewById<Button>(R.id.clearB)
        val allQuestions = findViewById<Button>(R.id.allQ)
        val hintButton = findViewById<Button>(R.id.hintB)
        val enterButton = findViewById<Button>(R.id.enter)

        clearButton.setOnClickListener{
            clearMessages()
            //clears convo based on question id,
           // not necessary to pass it because it is declared as a class level variable
        }
        hintButton.setOnClickListener{
            val hint = "Please give me 1 hint to help me solve this question"
            val currentQuestion = QuestionBank.questions[questionId].toString()

            getApiResponse(conversation, hint, currentQuestion) { response ->
                runOnUiThread {
                    //viewText.text = response
                    //we can create a map and attach "role" to "user", "content" to userInput
                    //we can create a map and attach "role" to "assistant", "content" to response


                    val assistantMessage = mapOf(
                        "role" to "assistant",
                        "content" to response,
                        "timeStamp" to FieldValue.serverTimestamp(),
                        "questionID" to questionId
                    )
                    Log.d("Firestore", response)

                    messagesRef.add(assistantMessage)
                        .addOnSuccessListener {
                            Log.d("Firestore", "API response added to Firestore")
                        }
                        .addOnFailureListener { e ->
                            Log.d("Firestore", "Response addition to Firestore failed: ${e.message}")
                        }
                }
            }
        }
        allQuestions.setOnClickListener {
            val intent = Intent(this, QuestionsList::class.java)
            startActivity(intent)
        }

        nextButton.setOnClickListener {
            if (questionId < QuestionBank.questions.size - 1) {
                questionId += 1
                updateQuestion(questionId)
                viewText.text = ""
            }
        }

        prevButton.setOnClickListener {
            if (questionId > 0) {
                questionId -= 1
                updateQuestion(questionId)
                viewText.text = ""
            }
        }

        // Initial question setup
        updateQuestion(questionId)

        enterButton.setOnClickListener {
            val userInput = textInput.text.toString().trim()
            val currentQuestion = QuestionBank.questions[questionId].toString()

            var convoContext = mutableListOf<Map<String, String>>()
            //extract last 10 for context to limit token usage per message
            if (conversation.size > 10) {
                convoContext = conversation.takeLast(10).toMutableList() // get the last 10 items, not modify the original list
            } else {
                convoContext = conversation
            }
            getApiResponse(convoContext, userInput, currentQuestion) { response ->
                runOnUiThread {
                    //viewText.text = response
                    //we can create a map and attach "role" to "user", "content" to userInput
                    //we can create a map and attach "role" to "assistant", "content" to response
                    val userMessage = mapOf(
                        "role" to "user",
                        "content" to userInput,
                        "timeStamp" to FieldValue.serverTimestamp(),
                        "questionID" to questionId
                    )

                    val assistantMessage = mapOf(
                        "role" to "assistant",
                        "content" to response,
                        "timeStamp" to FieldValue.serverTimestamp(),
                        "questionID" to questionId
                    )
                    Log.d("Firestore", response)
                    //send userinput and response to firestore
                    messagesRef.add(userMessage)
                        .addOnSuccessListener {
                            Log.d("Firestore", "User input added to Firestore")
                        }
                        .addOnFailureListener { e ->
                            Log.d("Firestore", "User input addition to Firestore failed: ${e.message}")
                        }

                    messagesRef.add(assistantMessage)
                        .addOnSuccessListener {
                            Log.d("Firestore", "API response added to Firestore")
                        }
                        .addOnFailureListener { e ->
                            Log.d("Firestore", "Response addition to Firestore failed: ${e.message}")
                        }
                }
            }

            textInput.text.clear()
        }
    }

    private fun updateQuestion(questionId: Int) {
        val algoQuestionView = findViewById<TextView>(R.id.algoQ)
        val questionIdView = findViewById<TextView>(R.id.questionID)
        val currentQuestion = QuestionBank.questions[questionId]

        algoQuestionView.text = currentQuestion.toString()
        questionIdView.text = String.format("%d", currentQuestion.id)
        refresh()
    }

    private fun refresh(){
        val db = FirebaseFirestore.getInstance()
        val messagesRef = db.collection("messages")

        val conversation = mutableListOf<Map<String, String>>()
        //newer messages wont display after 10 msgs is reached,
        //potential fix1: force user to clear and restart after 10 messages
        //potential fix2: allow unlimited messages, but shrink convo to last 10 before sending to the api
        messagesRef
            .orderBy("timeStamp", Query.Direction.ASCENDING)
            .limit(20)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    return@addSnapshotListener
                }
                conversation.clear() // Clear previous data
                for (document in snapshot!!) {
                    val docID = document.get("questionID")?.toString()
                    if(docID == questionId.toString()){
                        val content = document.getString("content") ?: "No content"
                        val role = document.getString("role") ?: "user"
                        val msg = mapOf("role" to role, "content" to content)
                        conversation.add(msg)
                    }
                }
                Log.d("msgList", "$conversation")
                createTextView(conversation)
            }
    }

    private fun createTextView(conversation: MutableList<Map<String, String>>){
        Log.d("loopTest", "$conversation")
        Log.d("loopTest", "function executed")
        val scrollView = findViewById<ScrollView>(R.id.conversationView)
        scrollView.removeAllViews()

        //Containers for messages:
        val containerLayoutBoth = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            removeAllViews()
        }
        for(message in conversation){
            Log.d("loopTest", "loop executed")
            val textView = TextView(this)
            val role = message["role"] ?: ""
            val content = message["content"] ?: ""

            textView.text = "$role: $content"

            textView.setTextColor(Color.parseColor("#ffbb39"))
            textView.setPadding(32, 16, 32, 16)
            val layoutParams = LinearLayout.LayoutParams(
               // LinearLayout.LayoutParams.MATCH_PARENT,  // Width
                TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 300f, resources.displayMetrics).toInt(), // Width in pixels
                LinearLayout.LayoutParams.WRAP_CONTENT   // Height
            )
            layoutParams.setMargins(16, 8, 16, 30)
            textView.layoutParams = layoutParams
            if (role == "assistant") {
                textView.setBackgroundResource(R.drawable.rounded_left_textview_background)
                layoutParams.gravity = Gravity.START
            } else if (role == "user") {
                textView.setBackgroundResource(R.drawable.rounded_right_textview_background)
                layoutParams.gravity = Gravity.END
            }
            containerLayoutBoth.addView(textView)
        }

        // Add the combined container to the ScrollView
        scrollView.addView(containerLayoutBoth)

    }

    private fun clearMessages(){
        val db = FirebaseFirestore.getInstance()
        val messagesRef = db.collection("messages")

        messagesRef.get()
            .addOnSuccessListener{ querySnapshot ->
                for(document in querySnapshot){
                    if(document.get("questionID")?.toString() == questionId.toString()){
                        messagesRef.document(document.id).delete()
                    }
                }
            }
            .addOnFailureListener{error->
                val toast = Toast.makeText(this, "Error deleting covno history: $error", Toast.LENGTH_LONG)
                toast.show()
            }
    }


    private fun getApiResponse(conversation: MutableList<Map<String, String>>, userInput: String, question:String, callback: (String) -> Unit) {
        //make api call here
        val apiKey=BuildConfig.API_KEY

        val url = "https://api.openai.com/v1/chat/completions"

        val formattedQuestion = question.trim()
        Log.d("api_response", question)

        val systemPrompt = "You are an AI assistant helping a student understand algorithm problems. Provide guidance without giving direct solutions. Give hints and constructive feedback. Do not tell them how to solve it completely, give them 1 hint each time until they get to the solution by themselves. This is the question being referred to: ${formattedQuestion}"
        //add system prompt to set tone, and objective of the gpt responses:

        //this just adds to the conversation list, not the firestore db
        //create function to add to database, then retrieve messages on update
        conversation.add(mapOf("role" to "system", "content" to systemPrompt))
        conversation.add(mapOf("role" to "user", "content" to userInput))

        //testing conversation size, making sure convoContext is limited to at most 10:
        val cSize = conversation.size
        Log.d("convoContext", "$cSize")

        val gson = Gson()
        val jsonRequestBody = gson.toJson(mapOf("model" to "gpt-3.5-turbo", "messages" to conversation))


        val mediaType = "application/json; charset=utf-8".toMediaType()
        val body = jsonRequestBody.toRequestBody(mediaType)

        val request = Request.Builder()
            .url(url)
            .post(body)
            .addHeader("Authorization", "Bearer $apiKey")
            .addHeader("Content-Type", "application/json")
            .build()

        // req
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback("Failed to get a response: ${e.message}")

            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    val responseBody = response.body?.string()
                    if (!response.isSuccessful || responseBody == null) {
                        callback("Error ${response.code}: ${responseBody ?: "Empty response body"}")
                        Log.d("error","Error ${response.code}: ${responseBody ?: "Empty response body"}")
                        return
                    }

                    try {
                        // Parse the JSON response
                        val jsonObject = JSONObject(responseBody) //turn response body to JSON object
                        val choices = jsonObject.getJSONArray("choices")
                        val content = choices.getJSONObject(0)
                            .getJSONObject("message")
                            .getString("content")
                        //this is due to the structure of the gpt responseBody:
                        //must extract the 0th index of the choices array, then message, then content
                        //for example: responseBody.choices[0].message.content
                        // Return only the 'content'

                        //before returning add content to
                        callback(content.trim())
                    } catch (e: Exception) {
                        callback("Failed to parse response: ${e.message}")
                    }
                }
            }
        })
    }
}


