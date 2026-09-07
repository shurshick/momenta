package com.bghitech.momenta.core.design

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

val MomentaSmallShape = RoundedCornerShape(8.dp)
val MomentaMediumShape = RoundedCornerShape(8.dp)
val MomentaLargeShape = RoundedCornerShape(8.dp)
val MomentaRoundShape = RoundedCornerShape(50)

val MomentaMaterialShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = MomentaSmallShape,
    medium = MomentaMediumShape,
    large = MomentaLargeShape,
    extraLarge = RoundedCornerShape(12.dp)
)
