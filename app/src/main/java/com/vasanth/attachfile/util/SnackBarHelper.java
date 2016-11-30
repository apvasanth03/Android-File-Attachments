package com.vasanth.attachfile.util;

import android.content.Context;
import android.content.res.ColorStateList;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.widget.TextView;

import com.vasanth.attachfile.R;


/**
 * SnackBar Helper.
 * <p>
 * 1. Helper class used to perform varipus operations with snack bar (like show, hide).
 *
 * @author Vasanth
 */
public class SnackBarHelper {

    // instance.
    private static SnackBarHelper instance;

    // snackBarTextColor.
    private ColorStateList snackBarMessageTextColor;

    // snackBarActionTextColor.
    private ColorStateList snackBarActionTextColor;

    /**
     * Constructor.
     */
    private SnackBarHelper() {

    }

    /**
     * Used to get single ton instance of SnackBarHelper.
     *
     * @param context Context
     * @return Single Ton instance of SnackBarHelper.
     */
    public static SnackBarHelper getInstance(final Context context) {
        if (instance == null) {
            instance = new SnackBarHelper();
        }
        instance.snackBarMessageTextColor = context.getResources().getColorStateList(R.color.white);
        instance.snackBarActionTextColor = context.getResources().getColorStateList(R.color.green);
        return instance;
    }

    /**
     * Used to get initialize and get snackbar.
     *
     * @param parentView     Snackbar's parent view.
     * @param message        Snackbar message.
     * @param duration       Snackbar duration (One of Snackbar.LENGTH_INDEFINITE, SnackBar.LENGTH_LONG, SnackBar.LENGTH_SHORT)
     * @param isShowAction   TRUE if we need to add action button.
     * @param actionText     Acction button text.
     * @param actionListener Action button listener.
     * @return SnackBar.
     */
    public Snackbar getSnackBar(final View parentView, final String message, final int duration,
                                final boolean isShowAction, final String actionText,
                                final View.OnClickListener actionListener) {
        Snackbar snackbar = Snackbar
                .make(parentView, message, duration);
        // Action.
        if (isShowAction) {
            snackbar.setAction(actionText, new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (actionListener != null) {
                        actionListener.onClick(view);
                    }
                }
            });
        }
        // Color.
        // Message text color.
        View sbView = snackbar.getView();
        TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(snackBarMessageTextColor);
        // Action text color.
        snackbar.setActionTextColor(snackBarActionTextColor);
        return snackbar;
    }

    /**
     * Get SnackBar message text color.
     *
     * @return SnackBar message text color.
     */
    public ColorStateList getSnackBarMessageTextColor() {
        return snackBarMessageTextColor;
    }

    /**
     * Set SnackBar message text color.
     *
     * @param snackBarMessageTextColor SnackBar message text color.
     */
    public void setSnackBarMessageTextColor(ColorStateList snackBarMessageTextColor) {
        this.snackBarMessageTextColor = snackBarMessageTextColor;
    }

    /**
     * Get SnackBar action text color.
     *
     * @return SnackBar action text color.
     */
    public ColorStateList getSnackBarActionTextColor() {
        return snackBarActionTextColor;
    }

    /**
     * Set SnackBar action text color.
     *
     * @param snackBarActionTextColor SnackBar action text color.
     */
    public void setSnackBarActionTextColor(ColorStateList snackBarActionTextColor) {
        this.snackBarActionTextColor = snackBarActionTextColor;
    }
}
