package com.example.email_classificator

import org.tensorflow.lite.support.label.Category

data class CardItem(val message: String, val categoryList: List<Category>)