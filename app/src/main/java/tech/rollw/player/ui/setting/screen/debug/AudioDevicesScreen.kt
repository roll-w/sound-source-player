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

package tech.rollw.player.ui.setting.screen.debug

import android.media.AudioDeviceInfo
import android.media.AudioFormat
import android.media.AudioManager
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.getSystemService
import tech.rollw.player.ui.ContentTypography
import tech.rollw.player.ui.PlayerTheme

/**
 * @author RollW
 */
@Composable
fun AudioDevicesScreen(
    modifier: Modifier = Modifier,
    contentTypography: ContentTypography = PlayerTheme.typography.contentNormal
) {
    val context = LocalContext.current
    val audioManager = remember {
        context.getSystemService<AudioManager>()!!
    }

    val devices = audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
    ) {
        devices.forEach { device ->
            DeviceInfo(
                device,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp, horizontal = 16.dp),
                contentTypography = contentTypography
            )
        }
    }
}


@Composable
private fun DeviceInfo(
    deviceInfo: AudioDeviceInfo,
    modifier: Modifier = Modifier,
    contentTypography: ContentTypography = PlayerTheme.typography.contentNormal
) {
    Card(
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier.padding(10.dp)
        ) {
            Text(
                text = toLogFriendlyType(deviceInfo.type),
                style = contentTypography.title,
                modifier = Modifier.padding(bottom = 5.dp)
            )
            Text(
                text = "${deviceInfo.id}  ${deviceInfo.productName}",
                style = contentTypography.subtitle,
                modifier = Modifier.padding(bottom = 5.dp)
            )
            Text(
                text = "Channels: ${deviceInfo.getChannelCounts().joinToString { it.toString() }}",
                style = contentTypography.body,
                modifier = Modifier.padding(bottom = 2.dp)
            )
            Text(
                text = "Encodings: ${formatEncodings(deviceInfo.encodings)}",
                style = contentTypography.body,
                modifier = Modifier.padding(bottom = 2.dp)
            )
            Text(
                text = "Sample Rates: ${deviceInfo.sampleRates.joinToString { it.toString() }}",
                style = contentTypography.body,
                modifier = Modifier.padding(bottom = 2.dp)
            )
        }
    }
}

private fun formatEncodings(encodings: IntArray): String {
    return encodings.joinToString(", ") { toLogFriendlyEncoding(it) }
}

private fun toLogFriendlyType(type: Int): String {
    return when (type) {
        AudioDeviceInfo.TYPE_BUILTIN_EARPIECE -> "TYPE_BUILTIN_EARPIECE"
        AudioDeviceInfo.TYPE_BUILTIN_SPEAKER -> "TYPE_BUILTIN_SPEAKER"
        AudioDeviceInfo.TYPE_WIRED_HEADSET -> "TYPE_WIRED_HEADSET"
        AudioDeviceInfo.TYPE_WIRED_HEADPHONES -> "TYPE_WIRED_HEADPHONES"
        AudioDeviceInfo.TYPE_BLUETOOTH_SCO -> "TYPE_BLUETOOTH_SCO"
        AudioDeviceInfo.TYPE_BLUETOOTH_A2DP -> "TYPE_BLUETOOTH_A2DP"
        AudioDeviceInfo.TYPE_HDMI -> "TYPE_HDMI"
        AudioDeviceInfo.TYPE_DOCK -> "TYPE_DOCK"
        AudioDeviceInfo.TYPE_USB_ACCESSORY -> "TYPE_USB_ACCESSORY"
        AudioDeviceInfo.TYPE_USB_DEVICE -> "TYPE_USB_DEVICE"
        AudioDeviceInfo.TYPE_USB_HEADSET -> "TYPE_USB_HEADSET"
        AudioDeviceInfo.TYPE_TELEPHONY -> "TYPE_TELEPHONY"
        AudioDeviceInfo.TYPE_LINE_ANALOG -> "TYPE_LINE_ANALOG"
        AudioDeviceInfo.TYPE_HDMI_ARC -> "TYPE_HDMI_ARC"
        AudioDeviceInfo.TYPE_HDMI_EARC -> "TYPE_HDMI_EARC"
        AudioDeviceInfo.TYPE_LINE_DIGITAL -> "TYPE_LINE_DIGITAL"
        AudioDeviceInfo.TYPE_FM -> "TYPE_FM"
        AudioDeviceInfo.TYPE_AUX_LINE -> "TYPE_AUX_LINE"
        AudioDeviceInfo.TYPE_IP -> "TYPE_IP"
        AudioDeviceInfo.TYPE_BUS -> "TYPE_BUS"
        AudioDeviceInfo.TYPE_HEARING_AID -> "TYPE_HEARING_AID"
        AudioDeviceInfo.TYPE_BUILTIN_MIC -> "TYPE_BUILTIN_MIC"
        AudioDeviceInfo.TYPE_FM_TUNER -> "TYPE_FM_TUNER"
        AudioDeviceInfo.TYPE_TV_TUNER -> "TYPE_TV_TUNER"
        AudioDeviceInfo.TYPE_BUILTIN_SPEAKER_SAFE -> "TYPE_BUILTIN_SPEAKER_SAFE"
        AudioDeviceInfo.TYPE_REMOTE_SUBMIX -> "TYPE_REMOTE_SUBMIX"
        AudioDeviceInfo.TYPE_BLE_HEADSET -> "TYPE_BLE_HEADSET"
        AudioDeviceInfo.TYPE_BLE_SPEAKER -> "TYPE_BLE_SPEAKER"
        AudioDeviceInfo.TYPE_BLE_BROADCAST -> "TYPE_BLE_BROADCAST"
        else -> "Invalid encoding $type"
    }
}

private fun toLogFriendlyEncoding(enc: Int): String {
    return when (enc) {
        AudioFormat.ENCODING_INVALID -> "ENCODING_INVALID"
        AudioFormat.ENCODING_PCM_16BIT -> "ENCODING_PCM_16BIT"
        AudioFormat.ENCODING_PCM_8BIT -> "ENCODING_PCM_8BIT"
        AudioFormat.ENCODING_PCM_FLOAT -> "ENCODING_PCM_FLOAT"
        AudioFormat.ENCODING_AC3 -> "ENCODING_AC3"
        AudioFormat.ENCODING_E_AC3 -> "ENCODING_E_AC3"
        AudioFormat.ENCODING_DTS -> "ENCODING_DTS"
        AudioFormat.ENCODING_DTS_HD -> "ENCODING_DTS_HD"
        AudioFormat.ENCODING_MP3 -> "ENCODING_MP3"
        AudioFormat.ENCODING_AAC_LC -> "ENCODING_AAC_LC"
        AudioFormat.ENCODING_AAC_HE_V1 -> "ENCODING_AAC_HE_V1"
        AudioFormat.ENCODING_AAC_HE_V2 -> "ENCODING_AAC_HE_V2"
        AudioFormat.ENCODING_IEC61937 -> "ENCODING_IEC61937"
        AudioFormat.ENCODING_DOLBY_TRUEHD -> "ENCODING_DOLBY_TRUEHD"
        AudioFormat.ENCODING_AAC_ELD -> "ENCODING_AAC_ELD"
        AudioFormat.ENCODING_AAC_XHE -> "ENCODING_AAC_XHE"
        AudioFormat.ENCODING_AC4 -> "ENCODING_AC4"
        AudioFormat.ENCODING_E_AC3_JOC -> "ENCODING_E_AC3_JOC"
        AudioFormat.ENCODING_DOLBY_MAT -> "ENCODING_DOLBY_MAT"
        AudioFormat.ENCODING_OPUS -> "ENCODING_OPUS"
        AudioFormat.ENCODING_PCM_24BIT_PACKED -> "ENCODING_PCM_24BIT_PACKED"
        AudioFormat.ENCODING_PCM_32BIT -> "ENCODING_PCM_32BIT"
        AudioFormat.ENCODING_MPEGH_BL_L3 -> "ENCODING_MPEGH_BL_L3"
        AudioFormat.ENCODING_MPEGH_BL_L4 -> "ENCODING_MPEGH_BL_L4"
        AudioFormat.ENCODING_MPEGH_LC_L3 -> "ENCODING_MPEGH_LC_L3"
        AudioFormat.ENCODING_MPEGH_LC_L4 -> "ENCODING_MPEGH_LC_L4"
        AudioFormat.ENCODING_DTS_HD_MA -> "ENCODING_DTS_HD_MA"
        AudioFormat.ENCODING_DTS_UHD_P1 -> "ENCODING_DTS_UHD_P1"
        AudioFormat.ENCODING_DTS_UHD_P2 -> "ENCODING_DTS_UHD_P2"
        AudioFormat.ENCODING_DSD -> "ENCODING_DSD"
        AudioFormat.ENCODING_DRA -> "ENCODING_DRA"
        else -> "Invalid encoding $enc"
    }
}