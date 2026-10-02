package com.hirehop.core.designsystem.illustration

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource

@Composable
fun HhCharacterIllustration(
    illustration: HhIllustration,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
) {
    Image(
        painter = painterResource(id = illustration.drawableRes),
        contentDescription = contentDescription,
        modifier = modifier,
        contentScale = ContentScale.Fit,
    )
}
