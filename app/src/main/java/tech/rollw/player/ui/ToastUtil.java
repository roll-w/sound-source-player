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

package tech.rollw.player.ui;

import android.content.Context;
import android.widget.Toast;
import androidx.annotation.StringRes;

public final class ToastUtil {
    private static Toast TOAST = null;

    public static void show(Context context, CharSequence s) {
        if (TOAST != null) {
            TOAST.cancel();
        }

        TOAST = Toast.makeText(context, s, Toast.LENGTH_SHORT);
        TOAST.show();
    }

    public static void show(Context context, @StringRes int s) {
        show(context, context.getString(s));
    }

    public static void showLong(Context context, CharSequence s) {
        if (TOAST != null) {
            TOAST.cancel();
        }

        TOAST = Toast.makeText(context, s, Toast.LENGTH_LONG);
        TOAST.show();
    }

    public static void showLong(Context context, @StringRes int s) {
        showLong(context, context.getString(s));
    }
}
