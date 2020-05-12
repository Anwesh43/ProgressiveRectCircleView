package com.anwesh.uiprojects.linkedprogressiverectcircleview

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.anwesh.uiprojects.progressiverectcircleview.ProgressiveRectCircleView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ProgressiveRectCircleView.create(this)
    }
}
