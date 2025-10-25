package com.lebaillyapp.composediceroller.model.dice.config

import androidx.compose.ui.graphics.Color
import com.lebaillyapp.composediceroller.model.dice.Pip
import com.lebaillyapp.composediceroller.model.dice.Vec3

data class CubeConfig(
    val vertices: List<Vec3>,
    val faces: List<FaceConfig>
) {
    companion object {
        fun createDefaultDice(isRootLayer: Boolean): CubeConfig {
            val s = 1f
            val vertices = listOf(
                Vec3(-s, -s, -s), Vec3(s, -s, -s),
                Vec3(s, s, -s), Vec3(-s, s, -s),
                Vec3(-s, -s, s), Vec3(s, -s, s),
                Vec3(s, s, s), Vec3(-s, s, s)
            )

            var colors = emptyList<Color>()

            if(isRootLayer){
                colors = listOf(
                    Color(0xFFFFFFFF), Color(0xFFFFFFFF),
                    Color(0xFFFFFFFF), Color(0xFFFFFFFF),
                    Color(0xFFFFFFFF), Color(0xFFFFFFFF)
                )
            }else{
                colors = listOf(
                    Color(0xFFF50057),
                    Color(0xFF006EF5),
                    Color(0xFF00F5F5),
                    Color(0xFFF59B00),
                    Color(0xFF00F5A7),
                    Color(0xFF8300F5)
                )




            }


            fun pip(x: Float, y: Float) = Pip(x, y)

            // Coordonnées normalisées (0..1)
            val mid = 0.5f
            val low = 0.25f
            val high = 0.75f

            val dicePips = listOf(
                listOf(pip(mid, mid)), // 1
                listOf(pip(low, low), pip(high, high)), // 2
                listOf(pip(low, low), pip(mid, mid), pip(high, high)), // 3
                listOf(pip(low, low), pip(low, high), pip(high, low), pip(high, high)), // 4
                listOf(pip(low, low), pip(low, high), pip(mid, mid), pip(high, low), pip(high, high)), // 5
                listOf(
                    pip(low, low), pip(low, mid), pip(low, high),
                    pip(high, low), pip(high, mid), pip(high, high)
                ) // 6
            )

            val faces = listOf(
                FaceConfig(listOf(0, 3, 2, 1), colors[0], dicePips[0]),  // Face 1 (rouge)
                FaceConfig(listOf(4, 5, 6, 7), colors[5], dicePips[5]),  // Face 6 (violet)
                FaceConfig(listOf(0, 1, 5, 4), colors[1], dicePips[1]),  // Face 2 (bleu)
                FaceConfig(listOf(2, 3, 7, 6), colors[4], dicePips[4]),  // Face 5 (vert)
                FaceConfig(listOf(0, 4, 7, 3), colors[2], dicePips[2]),  // Face 3 (lime)
                FaceConfig(listOf(1, 2, 6, 5), colors[3], dicePips[3])   // Face 4 (orange)
            )

            return CubeConfig(vertices, faces)
        }
    }
}