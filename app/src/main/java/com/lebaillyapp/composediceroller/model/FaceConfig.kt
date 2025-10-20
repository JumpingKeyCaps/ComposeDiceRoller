package com.lebaillyapp.composediceroller.model

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope

data class FaceConfig(
    val indices: List<Int>,
    val color: Color,
    val pips: List<Pip> = emptyList(),
)