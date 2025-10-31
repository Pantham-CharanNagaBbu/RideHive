package uk.ac.tees.mad.ridehive.display

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import uk.ac.tees.mad.ridehive.R

@Composable
fun Splash(innerPadding: PaddingValues) {
    val boxWidth = LocalConfiguration.current.screenWidthDp.dp
    val boxHeight = LocalConfiguration.current.screenHeightDp.dp
    var startAnimation by remember { mutableStateOf(false) }
    val offsetX by animateDpAsState(
        targetValue = if (startAnimation) boxWidth else 0.dp,
        animationSpec = tween(durationMillis = 3000),
        label = "Offset Animation"
    )

    val imageSize = animateDpAsState(
        targetValue = if (startAnimation) 250.dp else 0.dp,
        animationSpec = tween(durationMillis = 3000),
        label = "Image Size Animation"
    ).value

    LaunchedEffect(Unit) {
        startAnimation = true
    }


    Box(modifier = Modifier.fillMaxSize()){
        Column(modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally) {
            Image(painter = painterResource(id = R.drawable.app_icon),contentDescription = null,
                modifier = Modifier.size(imageSize).clip(RoundedCornerShape(50.dp)))
        }
        Image(painterResource(R.drawable.car__1_),contentDescription = null,
            modifier = Modifier.offset(x = offsetX, y = boxHeight/2 - 70.dp))
    }
}