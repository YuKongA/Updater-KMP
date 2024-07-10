package ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import updaterkmp.composeapp.generated.resources.Res
import updaterkmp.composeapp.generated.resources.logged_in
import updaterkmp.composeapp.generated.resources.login_desc
import updaterkmp.composeapp.generated.resources.login_expired
import updaterkmp.composeapp.generated.resources.login_expired_desc
import updaterkmp.composeapp.generated.resources.no_account
import updaterkmp.composeapp.generated.resources.using_v2

@Composable
fun LoginCardView(
    isLogin: MutableState<Int>
) {
    val account = when (isLogin.value) {
        1 -> stringResource(Res.string.logged_in)
        0 -> stringResource(Res.string.no_account)
        else -> stringResource(Res.string.login_expired)
    }
    val info = when (isLogin.value) {
        1 -> stringResource(Res.string.using_v2)
        0 -> stringResource(Res.string.login_desc)
        else -> stringResource(Res.string.login_expired_desc)
    }
    val icon = if (isLogin.value == 1) Icons.Filled.DoneAll else Icons.Filled.Done

    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(18.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(32.dp).padding(start = 8.dp)
            )
            Column(modifier = Modifier.padding(start = 20.dp)) {
                Text(
                    text = account,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = MaterialTheme.typography.titleMedium.fontSize
                )
                Text(
                    text = info,
                    fontSize = MaterialTheme.typography.titleMedium.fontSize
                )
            }
        }
    }
}