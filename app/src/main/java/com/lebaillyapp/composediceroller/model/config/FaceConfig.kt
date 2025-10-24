package com.lebaillyapp.composediceroller.model.config

import androidx.compose.ui.graphics.Color
import com.lebaillyapp.composediceroller.model.Pip

data class FaceConfig(
    val indices: List<Int>,
    val color: Color,
    val pips: List<Pip> = emptyList(),
)