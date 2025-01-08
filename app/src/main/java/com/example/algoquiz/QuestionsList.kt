package com.example.algoquiz

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.TypedValue
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class QuestionsList : AppCompatActivity() {
    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_questions_list)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val filterButton = findViewById<Button>(R.id.filterB)
        var filterOption = "Default"
        //create onclick to change filter value


        filterButton.setOnClickListener {
            when (filterOption) {
                "Default" -> {
                    filterOption = "Easy"
                    filterButton.text = "Easy"
                    filter("Easy")
                }

                "Easy" -> {
                    filterOption = "Medium"
                    filterButton.text = "Medium"
                    filter("Medium")
                }

                "Medium" -> {
                    filterOption = "Hard"
                    filterButton.text = "Hard"
                    filter("Hard")
                }

                "Hard" -> {
                    filterOption = "Default"
                    filterButton.text = "Default"
                    filter("Default")
                }
            }
        }

        filter(filterOption)

    }





    private fun filter(filterOption:String){

        val questionsArray = QuestionBank.displayQuestions()
        val scrollView = findViewById<ScrollView>(R.id.qList)
        scrollView.removeAllViews()
        val containerLayout = LinearLayout(this)
        containerLayout.orientation=LinearLayout.VERTICAL
        //clear previous views
        if(filterOption != "Default"){
            for (question in questionsArray){
                if(question.difficulty == filterOption) {
                    val button = Button(this)
                    button.setBackgroundResource(R.drawable.questionlist)
                    button.text = "(Number: ${question.id}) ${question.question} "
                    button.setTextColor(Color.parseColor("#ffbb39"));

                    button.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10f)
                    containerLayout.addView(button)
                    button.setOnClickListener {
                        val intent = Intent(this, MainActivity::class.java)
                        intent.putExtra("questionID", question.id)
                        intent.putExtra("questionText", question.question) // Add this line
                        startActivity(intent)
                    }
                }
            }
            //create method inside question.kt to return all questions, set that return value to questionsList.text
            scrollView.addView(containerLayout)
        }else{
            for (question in questionsArray){
                val button = Button(this)
                button.setBackgroundResource(R.drawable.questionlist)
                button.text = "(Number: ${question.id}) ${question.question} "
                button.setTextColor(Color.parseColor("#ffbb39"));

                button.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10f)
                containerLayout.addView(button)
                button.setOnClickListener {
                    val intent = Intent(this, MainActivity::class.java)
                    intent.putExtra("questionID", question.id)
                    intent.putExtra("questionText", question.question) // Add this line
                    startActivity(intent)
                }
            }
            //create method inside question.kt to return all questions, set that return value to questionsList.text
            scrollView.addView(containerLayout)
        }
    }
}

