/*
    Copyright (C) 2020 Forrest Guice
    This file is part of Suntimes.

    Suntimes is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Suntimes is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with Suntimes.  If not, see <http://www.gnu.org/licenses/>.
*/

package com.forrestguice.suntimes.addon.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.Html;
import android.util.TypedValue;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.forrestguice.suntimes.addon.R;
import com.forrestguice.suntimes.annotation.NonNull;
import com.forrestguice.suntimes.annotation.Nullable;

import android.support.design.widget.Snackbar;

@SuppressWarnings({"WeakerAccess", "UnusedReturnValue"})
public class Messages extends MessagesBase
{
    public static TextView getSnackBarText(View v) {
        return (TextView)v.findViewById(android.support.design.R.id.snackbar_text);
    }

    @NonNull
    public static Snackbar createMessage(Context context, @NonNull View view, @NonNull CharSequence message, int textSize, int maxLines, int displayLength, @Nullable View.OnClickListener onClickListener)
    {
        Snackbar snackbar = Snackbar.make(view, message, displayLength);
        View snackbarView = snackbar.getView();
        themeSnackbar(context, snackbar, null);

        //snackbarView.setBackgroundColor(ContextCompat.getColor(context, R.color.snackbarError_background));   // TODO
        snackbarView.setOnClickListener(onClickListener);

        TextView textView = getSnackBarText(snackbarView);
        if (textView != null)
        {
            //textView.setTextColor(ContextCompat.getColor(context, R.color.snackbarError_text));    // TODO
            textView.setMaxLines(maxLines);
            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize);
        }

        return snackbar;
    }

    @Nullable
    public static Snackbar showMessage(final Activity context, @Nullable View view, @NonNull CharSequence message, int textSize, int maxLines, int displayLength, @Nullable View.OnClickListener onClickListener)
    {
        if (view == null)
        {
            Toast.makeText(context, message, Toast.LENGTH_LONG).show();
            return null;

        } else {
            Snackbar snackbar = createMessage(context, view, message, textSize, maxLines, displayLength, onClickListener);
            snackbar.show();
            return snackbar;
        }
    }

    public static Snackbar showMissingDependencyMessage(final Activity context, View view)
    {
        CharSequence message = Html.fromHtml(context.getString(R.string.missing_dependency, context.getString(R.string.min_suntimes_version)));
        return showMessage(context, view, message, 18, 3, Snackbar.LENGTH_INDEFINITE, new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(context.getString(R.string.min_suntimes_url)));
                if (intent.resolveActivity(context.getPackageManager()) != null) {
                    context.startActivity(intent);
                }
            }
        });
    }

    public static Snackbar showPermissionDeniedMessage(final Activity context, View view)
    {
        CharSequence message = Html.fromHtml(context.getString(R.string.missing_permission, context.getString(R.string.app_name)));
        return showMessage(context, view, message, 12, 9, Snackbar.LENGTH_INDEFINITE, null);
    }

    @SuppressLint("ResourceType")
    public static void themeSnackbar(Context context, @NonNull Snackbar snackbar, @Nullable Integer[] colorOverrides)
    {
        Integer[] colors = getSnackbarColors(context, colorOverrides);

        View snackbarView = snackbar.getView();
        snackbarView.setBackgroundColor(colors[2]);
        snackbar.setActionTextColor(colors[1]);

        TextView snackbarText = getSnackBarText(snackbarView);
        if (snackbarText != null) {
            snackbarText.setTextColor(colors[0]);
            snackbarText.setMaxLines(3);
        }
    }

}

