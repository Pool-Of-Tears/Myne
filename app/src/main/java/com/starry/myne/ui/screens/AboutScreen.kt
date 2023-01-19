package com.starry.myne.ui.screens

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.starry.myne.BuildConfig
import com.starry.myne.R
import com.starry.myne.others.Constants
import com.starry.myne.ui.common.CustomTopAppBar
import com.starry.myne.ui.theme.figeronaFont


@Composable
fun AboutScreen(navController: NavController) {
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        CustomTopAppBar(headerText = stringResource(id = R.string.about_header)) {
            navController.navigateUp()
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 14.dp, end = 14.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(
                    4.dp
                )
            ),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(148.dp), contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surface)
                ) {
                    Image(
                        modifier = Modifier.size(120.dp),
                        painter = painterResource(id = R.drawable.ic_splash_screen),
                        contentDescription = null
                    )
                }
            }

            Text(
                text = stringResource(id = R.string.app_name),
                modifier = Modifier.fillMaxWidth(),
                fontSize = 26.sp,
                fontFamily = figeronaFont,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Version ${BuildConfig.VERSION_NAME}",
                modifier = Modifier.fillMaxWidth(),
                fontSize = 14.sp,
                fontFamily = figeronaFont,
                fontWeight = FontWeight.Normal,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(id = R.string.about_desc),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 22.dp, end = 22.dp),
                fontSize = 14.sp,
                fontFamily = figeronaFont,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Row {
                    LinkButton(
                        text = "Github",
                        icon = ImageVector.vectorResource(id = R.drawable.ic_github_logo)
                    ) {
                        openWebLink(context, Constants.REPO_URL)
                    }

                    LinkButton(
                        text = "Telegram",
                        icon = ImageVector.vectorResource(id = R.drawable.ic_telegram_logo)
                    ) {
                        openWebLink(context, Constants.TELEGRAM_GROUP_URL)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = stringResource(id = R.string.developed_by),
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, bottom = 12.dp),
            fontSize = 16.sp,
            fontFamily = figeronaFont,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )


        Card(
            modifier = Modifier
                .height(135.dp)
                .fillMaxWidth()
                .padding(start = 14.dp, end = 14.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(
                    2.dp
                )
            ),
        ) {
            Row(modifier = Modifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(id = R.drawable.github_pfp),
                    contentDescription = null,
                    modifier = Modifier
                        .padding(start = 12.dp)
                        .size(90.dp)
                        .clip(CircleShape)
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 14.dp)
                ) {
                    Text(
                        text = stringResource(id = R.string.dev_name),
                        fontSize = 18.sp,
                        fontFamily = figeronaFont,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onBackground,
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = stringResource(id = R.string.dev_username),
                        fontSize = 16.sp,
                        fontFamily = figeronaFont,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onBackground,
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row {
                        LinkButton(
                            text = "Github",
                            icon = ImageVector.vectorResource(id = R.drawable.ic_github_logo)
                        ) {
                            openWebLink(context, Constants.DEV_GITHUB_URL)
                        }

                        LinkButton(
                            text = "Telegram",
                            icon = ImageVector.vectorResource(id = R.drawable.ic_telegram_logo)
                        ) {
                            openWebLink(context, Constants.DEV_TELEGRAM_URL)
                        }
                    }
                }
            }
        }
    }

}

@Composable
fun LinkButton(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clickable { onClick() }
            .padding(all = 4.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(size = 18.dp),
            tint = MaterialTheme.colorScheme.onSurface
        )

        Text(
            text = text.uppercase(),
            fontWeight = FontWeight.Bold,
            fontFamily = figeronaFont,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(start = 4.dp),
        )
    }
}

fun openWebLink(context: Context, url: String) {
    val uri: Uri = Uri.parse(url)
    val intent = Intent(Intent.ACTION_VIEW, uri)
    try {
        context.startActivity(intent)
    } catch (exc: ActivityNotFoundException) {
        exc.printStackTrace()
    }
}

@Composable
@Preview(showBackground = true)
fun AboutScreenPreview() {
    AboutScreen(rememberNavController())
}