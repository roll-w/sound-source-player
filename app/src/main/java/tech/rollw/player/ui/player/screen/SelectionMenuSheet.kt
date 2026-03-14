/*
 * Copyright (C) 2024 RollW
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package tech.rollw.player.ui.player.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import tech.rollw.player.R
import tech.rollw.player.ui.components.TextIconItem
import tech.rollw.player.ui.compose.components.ActionMenu
import tech.rollw.player.ui.compose.components.ActionMenuItem

/**
 * @author RollW
 */
object SelectionMenuSheetDefaults {

}

@JvmInline
value class SelectState private constructor(private val value: Int) {
    companion object{
        val Empty = SelectState(0)

        val Single = SelectState(1)

        val Multiple = SelectState(2)

        // val All = SelectState(3)
    }
}

@Composable
fun SelectionMenuSheet(
    selectedCount: Int,
    total: Int,
    modifier: Modifier = Modifier,
    items: List<TextIconItem> = emptyList(),
    onClick: (TextIconItem, Int) -> Boolean = { _, _ -> false },
    onSelectAll: (Boolean) -> Unit = {},
    onClickClose: () -> Unit = {}
) {
    val context = LocalContext.current

    ActionMenu(
        modifier = modifier,
        header = {
            SelectionScreenHeader(
                selectedCount = selectedCount,
                total = total,
                modifier = Modifier.padding(5.dp),
                onSelectAll = onSelectAll,
                onClickClose = onClickClose
            )
        },
    ) {
        items(items.size, { it }) {
            val item = items[it]
            val enabled by item.enabledAsFlow().collectAsState(initial = true)

            ActionMenuItem(
                onClick = {
                    if (onClick(item, it)) {
                        onClickClose()
                    }
                },
                icon = {
                    Icon(
                        imageVector = item.getIcon(context),
                        contentDescription = ""
                    )
                },
                label = {
                    Text(
                        text = item.getText(context),
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                enabled = enabled
            )
        }
    }
}

@Composable
private fun SelectionScreenHeader(
    selectedCount: Int,
    total: Int,
    modifier: Modifier = Modifier,
    onSelectAll: (Boolean) -> Unit = {},
    onClickClose: () -> Unit = {}
) {
    var selectAll by remember { mutableStateOf(false) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Checkbox(checked = selectAll, onCheckedChange = {
            selectAll = it
            onSelectAll(it)
        })
        Text(
            text = stringResource(R.string.select_all)
        )
        Spacer(
            modifier = Modifier
                .weight(1f)
                .width(20.dp)
        )
        Text("$selectedCount / $total")
        IconButton(onClick = {
            onClickClose()
        }) {
            Icon(
                imageVector = ImageVector.vectorResource(
                    R.drawable.ic_baseline_close_24
                ),
                contentDescription = stringResource(R.string.close)
            )
        }
    }
}