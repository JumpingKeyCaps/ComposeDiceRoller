package com.lebaillyapp.composediceroller

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat.enableEdgeToEdge
import com.lebaillyapp.composediceroller.model.dice.config.CubeConfig
import com.lebaillyapp.composediceroller.model.dice.config.DiceAnimationConfig
import com.lebaillyapp.composediceroller.model.dice.config.DiceLayerConfig
import com.lebaillyapp.composediceroller.model.dice.state.LayerLockState
import com.lebaillyapp.composediceroller.model.dice.config.createUniformDice
import com.lebaillyapp.composediceroller.model.dice.config.createUniformGhost
import com.lebaillyapp.composediceroller.model.score.DiceScoreResult
import com.lebaillyapp.composediceroller.model.score.DiceScorer
import com.lebaillyapp.composediceroller.ui.composition.NeonCirclesRefined
import com.lebaillyapp.composediceroller.ui.composition.dice.legacy.InteractiveCube
import com.lebaillyapp.composediceroller.ui.composition.dice.legacy.InteractiveCubeV2
import com.lebaillyapp.composediceroller.ui.composition.dice.legacy.InteractiveCubeV4
import com.lebaillyapp.composediceroller.ui.composition.dice.legacy.InteractiveCubeWith3Nested
import com.lebaillyapp.composediceroller.ui.composition.dice.legacy.InteractiveCubeWith3NestedCrystal
import com.lebaillyapp.composediceroller.ui.composition.dice.legacy.InteractiveCubeWith3NestedLag
import com.lebaillyapp.composediceroller.ui.composition.dice.legacy.InteractiveCubeWith3NestedShiny
import com.lebaillyapp.composediceroller.ui.composition.dice.legacy.InteractiveCubeWithInner
import com.lebaillyapp.composediceroller.ui.composition.dice.legacy.InteractiveDiceComposable
import com.lebaillyapp.composediceroller.ui.composition.dice.NestedInteractiveDice
import com.lebaillyapp.composediceroller.ui.composition.dice.legacy.CubeCavityContainerV2
import com.lebaillyapp.composediceroller.ui.composition.dice.legacy.CubeCavityContainerV3
import com.lebaillyapp.composediceroller.ui.theme.ComposeDiceRollerTheme
import kotlin.math.hypot
import kotlin.random.Random

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ComposeDiceRollerTheme {

                Scaffold (
                    Modifier.fillMaxSize()
                ){
                    Surface(
                        modifier = Modifier.fillMaxSize().padding(it),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        CubeGallery()
                        //   TestCube2(numberOfDice = 1, cubeSize = 200f)
                       // TestCubeHex()
                      //  TestSingleCube()
                     // NeonCirclesScreen()


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


//todo -------------------------------------------------


@Composable
fun TestCube(numberOfDice: Int = 5) {
    var diceAnimConfigs by remember { mutableStateOf(List(numberOfDice) { DiceAnimationConfig.idle(0) }) }
    var diceValues by remember { mutableStateOf(List(numberOfDice) { 0 }) }
    var turnCounter by remember { mutableStateOf(0) }

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

            // Bouton pour lancer les dés
            Button(
                onClick = {
                    turnCounter++

                    diceValues = List(numberOfDice) { Random.nextInt(1, 7) }

                    diceAnimConfigs = diceValues.map { value ->
                        DiceAnimationConfig.rollTo(
                            targetValue = value,
                            rotationsX = 20f,
                            rotationsY = 20f,
                            rollingDuration = 5000L,
                            diceTicker = turnCounter
                        )
                    }
                },
                modifier = Modifier.padding(16.dp)
            ) {
                val currentValues = if (diceValues.all { it == 0 }) "Classic" else diceValues.joinToString(", ")
                Text("Roll Dice! (Currently: $currentValues)")
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Affichage des dés
            diceValues.forEachIndexed { index, value ->
                DiceItem(
                    value = value,
                    animationConfig = diceAnimConfigs[index],
                    diceSize = 100f
                )
            }
        }
    }
}

@Composable
fun TestCube2(numberOfDice: Int = 6,cubeSize: Float = 100f) {
    var diceAnimConfigs by remember { mutableStateOf(List(numberOfDice) { DiceAnimationConfig.idle(0) }) }
    var diceValues by remember { mutableStateOf(List(numberOfDice) { 0 }) }
    var turnCounter by remember { mutableStateOf(0) }

    val scorer = remember { DiceScorer() }
    var lastScoreResult by remember { mutableStateOf<DiceScoreResult?>(null) }


    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        // Affichage des dés en 2 colonnes
        Column(
            modifier = Modifier.fillMaxWidth().align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            diceValues.chunked(2).forEach { rowDice ->
                Row(
                    modifier = Modifier.padding(vertical = 0.dp),
                    horizontalArrangement = Arrangement.spacedBy(0.dp)
                ) {
                    rowDice.forEachIndexed { indexInRow, value ->
                        val globalIndex = diceValues.indexOf(value) +
                                diceValues.subList(0, diceValues.indexOf(value)).count { it == value }
                        DiceItem(
                            value = value,
                            animationConfig = diceAnimConfigs[globalIndex],
                            diceSize = cubeSize
                        )
                    }
                }
            }
        }


        // --- Affichage du score et des combinaisons ---
        lastScoreResult?.let { result ->
            Column(
                modifier = Modifier.padding(top = 26.dp).align(Alignment.TopCenter),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Score: ${result.totalScore}" + if (result.isFanny) " (Fanny!)" else "",
                    color = Color.White
                )
                if (result.combinations.isNotEmpty()) {
                    result.combinations.forEach { combo ->
                        Text(
                            text = "${combo.type}: ${combo.diceUsed.joinToString(", ")} → ${combo.score}",
                            color = Color.LightGray,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }



        // Bouton pour lancer les dés
        Button(
            onClick = {
                turnCounter++

                diceValues = List(numberOfDice) { Random.nextInt(1, 7) }

                diceAnimConfigs = diceValues.map { value ->
                    DiceAnimationConfig.rollTo(
                        targetValue = value,
                        rotationsX = 10f,
                        rotationsY = 10f,
                        rollingDuration = 4000L,
                        diceTicker = turnCounter
                    )
                }

                // --- Calcul du score à chaque lancer ---
                lastScoreResult = scorer.evaluate(diceValues)


            },
            modifier = Modifier.padding(25.dp).height(54.dp).align(Alignment.BottomCenter),
            colors = ButtonDefaults.buttonColors(Color(0x77000000))
        ) {
            val currentValues = if (diceValues.all { it == 0 }) "Classic" else diceValues.joinToString(", ")
            Text("Roll Dice! (Currently: $currentValues)", color = Color.White)
        }

    }
}


@Composable
fun TestCubeHex(numberOfDice: Int = 6, middleRowSpacing: Dp = 100.dp) {
    var diceAnimConfigs by remember { mutableStateOf(List(numberOfDice) { DiceAnimationConfig.idle(0) }) }
    var diceValues by remember { mutableStateOf(List(numberOfDice) { 0 }) }
    var turnCounter by remember { mutableStateOf(0) }

    val scorer = remember { DiceScorer() }
    var lastScoreResult by remember { mutableStateOf<DiceScoreResult?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
    ) {

        Column(
            modifier = Modifier.fillMaxWidth().align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            val rows = diceValues.chunked(2)

            rows.forEachIndexed { rowIndex, rowDice ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    rowDice.forEachIndexed { indexInRow, value ->
                        val globalIndex = rowIndex * 2 + indexInRow
                        if (rowIndex == 1 && indexInRow == 1) {
                            Spacer(modifier = Modifier.width(middleRowSpacing))
                        }

                        DiceItem(
                            value = value,
                            animationConfig = diceAnimConfigs[globalIndex],
                            diceSize = 100f
                        )
                    }
                }
            }
        }

        // --- Score ---
        lastScoreResult?.let { result ->
            Column(
                modifier = Modifier.padding(top = 26.dp).align(Alignment.TopCenter),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Score: ${result.totalScore}" + if (result.isFanny) " (Fanny!)" else "",
                    color = Color.White
                )
                if (result.combinations.isNotEmpty()) {
                    result.combinations.forEach { combo ->
                        Text(
                            text = "${combo.type}: ${combo.diceUsed.joinToString(", ")} → ${combo.score}",
                            color = Color.LightGray,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        // --- Bouton ---
        Button(
            onClick = {
                turnCounter++
                diceValues = List(numberOfDice) { Random.nextInt(1, 7) }
                diceAnimConfigs = diceValues.mapIndexed { i, value ->
                    DiceAnimationConfig.rollTo(
                        targetValue = value,
                        rotationsX = 10f,
                        rotationsY = 10f,
                        rollingDuration = 4000L,
                        diceTicker = turnCounter
                    )
                }
                lastScoreResult = scorer.evaluate(diceValues)
            },
            modifier = Modifier.padding(25.dp).height(54.dp).align(Alignment.BottomCenter),
            colors = ButtonDefaults.buttonColors(Color(0x77000000))
        ) {
            val currentValues = if (diceValues.all { it == 0 }) "Classic" else diceValues.joinToString(", ")
            Text("Roll Dice! (Currently: $currentValues)", color = Color.White)
        }
    }
}



@Composable
fun DiceItem(
    value: Int,
    animationConfig: DiceAnimationConfig = DiceAnimationConfig.idle(0),
    diceSize: Float = 150f
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
                lagFactor = 1.0f,
                invertRotationX = false,
                showPips = true,
                alpha = 1.0f
            )
        )
    }

    Box(
        modifier = Modifier.width(diceSize.dp).height(diceSize.dp),
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
            size = diceSize,
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


//todo -----------------------------



@Composable
fun NeonCirclesScreen() {

    var diceAnimConfig by remember { mutableStateOf(DiceAnimationConfig.idle(0)) }
    var diceValue by remember { mutableStateOf(0) }


    var glowStates by remember {
        mutableStateOf(
            listOf(false, false, false)
        )
    }

    val radii = listOf(150f, 140f, 130f)
   // val colors = listOf(Color(0xFFFF1696), Color(0xFFFF357C), Color(0xFFFC94B9))

    val colors = listOf(Color(0xFFFFFFFF), Color(0xFFFFFFFF), Color(0xFFFFFFFF))

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    val center = Offset((size.width / 2).toFloat(), (size.height / 2).toFloat())
                    val distance = hypot(offset.x - center.x, offset.y - center.y)

                    val index = radii.indexOfFirst { distance in (it - 40f)..(it + 40f) }
                    if (index != -1) {
                        glowStates = glowStates.mapIndexed { i, old ->
                            if (i == index) !old else old
                        }
                    }
                }
            }
    ) {
        radii.zip(colors).forEachIndexed { index, (radius, color) ->
            NeonCirclesRefined(
                color = color,
                radius = radius,
                glowEnabled = glowStates[index],
                animated = false
            )
        }

        DiceItem(
            value = diceValue,
            animationConfig = diceAnimConfig,
            diceSize = 100f
        )


    }
}

