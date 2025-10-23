package com.lebaillyapp.composediceroller

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.lebaillyapp.composediceroller.model.CubeConfig
import com.lebaillyapp.composediceroller.model.DiceAnimationConfig
import com.lebaillyapp.composediceroller.model.DiceLayerConfig
import com.lebaillyapp.composediceroller.model.LayerLockState
import com.lebaillyapp.composediceroller.model.createUniformDice
import com.lebaillyapp.composediceroller.model.createUniformGhost
import com.lebaillyapp.composediceroller.ui.composition.InteractiveCube
import com.lebaillyapp.composediceroller.ui.composition.InteractiveCubeV2
import com.lebaillyapp.composediceroller.ui.composition.InteractiveCubeV3
import com.lebaillyapp.composediceroller.ui.composition.InteractiveCubeV4
import com.lebaillyapp.composediceroller.ui.composition.InteractiveCubeWith3Nested
import com.lebaillyapp.composediceroller.ui.composition.InteractiveCubeWith3NestedCrystal
import com.lebaillyapp.composediceroller.ui.composition.InteractiveCubeWith3NestedLag
import com.lebaillyapp.composediceroller.ui.composition.InteractiveCubeWith3NestedShiny
import com.lebaillyapp.composediceroller.ui.composition.InteractiveCubeWithInner
import com.lebaillyapp.composediceroller.ui.composition.InteractiveDiceComposable
import com.lebaillyapp.composediceroller.ui.composition.NestedInteractiveDice
import com.lebaillyapp.composediceroller.ui.containeur.CubeCavityContainerV1
import com.lebaillyapp.composediceroller.ui.containeur.CubeCavityContainerV2
import com.lebaillyapp.composediceroller.ui.containeur.CubeCavityContainerV3
import com.lebaillyapp.composediceroller.ui.theme.ComposeDiceRollerTheme
import kotlin.random.Random

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ComposeDiceRollerTheme {

                Scaffold (
                    Modifier.fillMaxSize()
                ){
                    Surface(
                        modifier = Modifier.fillMaxSize().padding(it),
                        color = MaterialTheme.colorScheme.background
                    ) {
                       // CubeGallery()
                           TestCube()
                       // TestSingleCube()
                    }
                }
            }
        }
    }
}
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CubeGallery() {
    val cubesizedemo = 550f
    val cubes = listOf<@Composable (Modifier) -> Unit>(
        { InteractiveCube(size = cubesizedemo) },
        { InteractiveCubeV2(size = cubesizedemo) },
        { InteractiveCubeV4(size = cubesizedemo) },
        { InteractiveCubeWithInner(size = cubesizedemo) },
        { InteractiveCubeWith3Nested(size = cubesizedemo) },
        { InteractiveCubeWith3NestedLag(size = cubesizedemo) },
        { InteractiveCubeWith3NestedShiny(size = cubesizedemo) },
        { InteractiveCubeWith3NestedCrystal(size = cubesizedemo) },
        { CubeCavityContainerV2(size = cubesizedemo) },
        { CubeCavityContainerV3(size = cubesizedemo) },
        { InteractiveDiceComposable(size = 120f) }
    )

    LazyVerticalGrid(
        columns = GridCells.Fixed(1),
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceContainerLowest),
        verticalArrangement = Arrangement.spacedBy(6.dp),
        horizontalArrangement = Arrangement.spacedBy(0.dp),
    ) {
        items(cubes.size) { index ->
            Box(
                modifier = Modifier
                    .padding(start = 28.dp, end = 28.dp, top = 14.dp, bottom = 14.dp)
                    .aspectRatio(1f)
                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))

            ) {
                cubes[index].invoke(Modifier.fillMaxSize())
            }
        }
    }
}


@Composable
fun TestSingleCube() {


    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 0.dp),
            contentAlignment = Alignment.Center
        ) {
            NestedInteractiveDice(
                layers = listOf(
                    DiceLayerConfig.createGhostParent(),

                    DiceLayerConfig(
                        cubeConfig = CubeConfig.createDefaultDice(false),
                        ratio = 0.90f,
                        lagFactor = 1f,
                        showPips = false,
                        alpha = 0.15f
                    ),

                    DiceLayerConfig(
                        cubeConfig = CubeConfig.createDefaultDice(false),
                        ratio = 0.5f,
                        lagFactor = 0.3f,
                        invertRotationX = false,
                        showPips = true,
                        alpha = 0.95f
                    )
                ),
                size = 300f,
                pipRadius = 0.10f,
                pipPadding = 0.05f,
                layerLocks = listOf(
                    LayerLockState.unlocked(),  // Cube 0 : libre
                    LayerLockState.unlocked() ,
                    LayerLockState.unlocked()   // Cube 2 : libre
                )
            )
        }


    }
}


@Composable
fun TestCube() {
    var diceAnimConfig by remember { mutableStateOf(DiceAnimationConfig.idle(0)) }
    var diceValue by remember { mutableStateOf(0) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            // Bouton pour lancer le dé
            Button(
                onClick = {
                    val newValue = Random.nextInt(1, 7)
                    diceValue = newValue

                    diceAnimConfig = DiceAnimationConfig.rollTo(
                        targetValue = newValue,
                        rotationsX = 20f,       // 30 tours sur X
                        rotationsY = 20f,       // 30 tours sur Y
                        rollingDuration = 5000L // 4 secondes de roll fluide
                    )
                },
                modifier = Modifier.padding(16.dp)
            ) {
                Text("Roll Dice! (Currently: ${if (diceValue == 0) "Classic" else diceValue})")
            }

            // Le dé
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                DiceItem(
                    value = diceValue,
                    animationConfig = diceAnimConfig
                )
            }
        }
    }
}

@Composable
fun DiceItemOLD(value: Int) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        NestedInteractiveDice(
            layers = listOf(
                DiceLayerConfig.createGhostParent(),

                DiceLayerConfig(
                    cubeConfig = if(value == 0) CubeConfig.createDefaultDice(false) else CubeConfig.createUniformGhost(value),
                    ratio = 0.90f,
                    lagFactor = 1f,
                    showPips = false,
                    alpha = 0.3f
                ),

                DiceLayerConfig(
                    cubeConfig = if(value == 0) CubeConfig.createDefaultDice(false) else CubeConfig.createUniformDice(value),
                    ratio = 0.75f,
                    lagFactor = 0.5f,
                    invertRotationX = false,
                    showPips = true,
                    alpha = 1.0f
                )
            ),
            size = 150f,
            pipRadius = 0.13f,
            pipPadding = 0.05f,
            layerLocks = listOf(
                LayerLockState.unlocked(),
                LayerLockState.unlocked(),
                LayerLockState.unlocked()
            )
        )
    }
}




@Composable
fun DiceItem(
    value: Int,
    animationConfig: DiceAnimationConfig = DiceAnimationConfig.idle(0)
) {
    var currentAnimConfig by remember { mutableStateOf(animationConfig) }
    var currentValue by remember { mutableStateOf(value) }

    LaunchedEffect(animationConfig) {
        currentAnimConfig = animationConfig
    }

    val layers = remember(currentValue) {
        listOf(
            DiceLayerConfig.createGhostParent(),

            DiceLayerConfig(
                cubeConfig = if (currentValue == 0)
                    CubeConfig.createDefaultDice(false)
                else
                    CubeConfig.createUniformGhost(currentValue),
                ratio = 0.90f,
                lagFactor = 1f,
                showPips = false,
                alpha = 0.3f
            ),

            DiceLayerConfig(
                cubeConfig = if (currentValue == 0)
                    CubeConfig.createDefaultDice(false)
                else
                    CubeConfig.createUniformDice(currentValue),
                ratio = 0.75f,
                lagFactor = 0.5f,
                invertRotationX = false,
                showPips = true,
                alpha = 0.9f
            )
        )
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        NestedInteractiveDice(
            animationConfig = currentAnimConfig,
            onAnimationStateChange = { newConfig ->
                currentAnimConfig = newConfig
            },
            onValueChange = { newValue ->
                currentValue = newValue
            },
            layers = layers,
            size = 110f,
            pipRadius = 0.13f,
            pipPadding = 0.05f,
            layerLocks = listOf(
                LayerLockState.unlocked(),
                LayerLockState.unlocked(),
                LayerLockState.unlocked()
            )
        )
    }
}