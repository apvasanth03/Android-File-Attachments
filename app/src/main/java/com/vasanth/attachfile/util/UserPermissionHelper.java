package com.vasanth.attachfile.util;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

/**
 * User Permission Helper.
 * <p>
 * 1. Class used to check new Android M Permission model.
 * 2. Class used to check if user has granted permission for requested feature.
 *
 * @author Vasanth
 */
public class UserPermissionHelper {

    // instance.
    private static UserPermissionHelper instance;

    // userPermissionCallback.
    private UserPermissionCallback userPermissionCallback;

    // requestCode.
    private int requestCode;

    /**
     * Interface used to receive callback, wheather user has granted permission or not.
     */
    public interface UserPermissionCallback {

        /**
         * Gets called when user grant permission.
         *
         * @param requestCode Request COde.
         */
        void userGrantedPermission(int requestCode);

        /**
         * Gets called when user denied permission.
         *
         * @param requestCode Request Code.
         */
        void userDeniedPermission(int requestCode);

        /**
         * Get called if we need to show expaination to user why we need this permission.
         *
         * @param requestCode Request Code.
         */
        void expainToUserWhyWeNeedPermission(int requestCode);
    }


    /**
     * Constructor.
     */
    private UserPermissionHelper() {

    }

    /**
     * Used to get single ton instance of UserPermissionHelper.
     *
     * @return Single Ton instance of UserPermissionHelper.
     */
    public static UserPermissionHelper getInstance() {
        if (instance == null) {
            instance = new UserPermissionHelper();
        }
        return instance;
    }

    /**
     * Used to check if user has granted permission for requested feature.
     * <p>
     * 1. Calling Activity must override method "onRequestPermissionsResult" - and call UserPermissionHelper's "onRequestPermissionsResult"
     * method, to process user's action and send callback.
     *
     * @param activity               Activity.
     * @param permissionName         Name of permission to be checked.
     * @param isShowedExplaination   TRUE if we have already showed explaination to user that why we need this permission.
     * @param requestCode            Request Code.
     * @param userPermissionCallback Used to send callback Whether user has granted or denied permission.
     */
    public void checkIfUserHasGrantedPermission(final Activity activity, final String permissionName, final boolean isShowedExplaination,
                                                final int requestCode, final UserPermissionCallback userPermissionCallback) {

        this.requestCode = requestCode;
        this.userPermissionCallback = userPermissionCallback;
        // Check if we have permission.
        if (ContextCompat.checkSelfPermission(activity, permissionName)
                != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission.
            // Check if we need to show an explanation?
            if (!isShowedExplaination && ActivityCompat.shouldShowRequestPermissionRationale(activity, permissionName)) {
                // Need to explain user why we need this permission.
                if (userPermissionCallback != null) {
                    userPermissionCallback.expainToUserWhyWeNeedPermission(requestCode);
                }
            } else {
                // No need to explain user.
                // Request permission.
                requestPermissionFromUser(activity, permissionName);
            }
        } else {
            // We have permission.
            if (userPermissionCallback != null) {
                userPermissionCallback.userGrantedPermission(requestCode);
            }
        }
    }

    /**
     * Used to request permission from user.
     *
     * @param activity       Activity.
     * @param permissionName Permisision name.
     */
    private void requestPermissionFromUser(final Activity activity, final String permissionName) {
        ActivityCompat.requestPermissions(activity, new String[]{permissionName}, requestCode);
    }

    /**
     * On Request Permission Result.
     *
     * @param requestCode  Request Code.
     * @param permissions  Permission.
     * @param grantResults Grant Results.
     */
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        if (requestCode == this.requestCode) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // permission granted.
                if (userPermissionCallback != null) {
                    userPermissionCallback.userGrantedPermission(requestCode);
                }
            } else {
                // permission denied.
                if (userPermissionCallback != null) {
                    userPermissionCallback.userDeniedPermission(requestCode);
                }
            }
        }
    }
}
