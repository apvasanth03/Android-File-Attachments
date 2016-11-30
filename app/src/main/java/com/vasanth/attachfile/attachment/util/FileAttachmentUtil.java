package com.vasanth.attachfile.attachment.util;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * File Attachment Util.
 * <p>
 * 1. Responsibility.
 * 1.a. Class responsible to allow user to pick file and return the appropriate callback.
 * <p>
 * 2. Methods.
 * 2.a. attachFile - Used to attach file.
 * 2.b. processAttachFileResult - Used to process Attach File Result.
 * <p>
 * 3. Result.
 * 3.a. We will send result (User selected files uri) through Callback.
 * <p>
 * 4. Note
 * 4.a. FileAttachmentUtil requires WRITE_EXTERNAL_STORAGE permission.
 * 4.b. Once user selects file from some third party application, we get callback to activity's "onActivityResult" method,
 * hence override onActivityResult and if the "RequestCode is ATTACH_FILE_REQUEST_CODE" then send the extra data to our method "processAttachFileResult" to process the result.
 * 4.c. Beware of Security Exception - We will get the user attached file as a Uri, we will only hold temporary permission to read content from the Uri,
 * The permission will be expired once the activity is destroyed, hence we can't read the content from uri (Hence consume this uri with in this activity &
 * don't pass it around to other activity).
 * <p>
 * 5. Reference
 * 5.a. https://developer.android.com/guide/topics/providers/document-provider.html
 * 5.b. https://developer.android.com/reference/android/content/Intent.html#ACTION_GET_CONTENT
 *
 * @author Vasanth
 */
public class FileAttachmentUtil {

    private static final String TAG = "FileAttachmentUtil";

    // Request code - Used to fire a intent to pick files for attachment, In the user activity's onActivityResult if the request codes matches then
    // Pass the result here to process it.
    public static final int ATTACH_FILE_REQUEST_CODE = 202;

    // This error code will be returned - If there is no external application to choose attachment from.
    public static final int ATTACH_FILE_ERROR_CODE_NO_ATTACHMENT_APPLICATION = 1001;

    // This error code will be returned - If user cancelled the attachment operation.
    public static final int ATTACH_FILE_ERROR_CODE_CANCELLED = 1003;

    // This error code will be returned - If any other error occurs.
    public static final int ATTACH_FILE_ERROR_CODE_FAILED = 1002;

    private Activity activity;
    private FileAttachmentCallback fileAttachmentCallback;

    /**
     * File Attachment Callback.
     */
    public interface FileAttachmentCallback {

        /**
         * Called on success of file attachment.
         *
         * @param attachedFileUris Uri's of the user attached files.
         */
        void onFileAttachmentSuccess(List<Uri> attachedFileUris);

        /**
         * Called on failure of file attachment.
         *
         * @param errorCode Error Code.
         */
        void onFileAttachmentFailure(int errorCode);

    }

    /**
     * Constructor.
     *
     * @param activity               Activity.
     * @param fileAttachmentCallback File Attachment callback.
     */
    public FileAttachmentUtil(final Activity activity, final FileAttachmentCallback fileAttachmentCallback) {
        this.activity = activity;
        this.fileAttachmentCallback = fileAttachmentCallback;
    }

    /**
     * Used to attach file.
     * <p>
     * 1. Launch intent to allow user to select file from third party application.
     *
     * @param attachFileChooserTitle Title used to set on Attachment file chooser dialog.
     */
    public void attachFile(final String attachFileChooserTitle) {
        Intent attachIntent = new Intent(Intent.ACTION_GET_CONTENT);
        attachIntent.addCategory(Intent.CATEGORY_OPENABLE);
        attachIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        attachIntent.setType("*/*");
        attachIntent.putExtra(Intent.EXTRA_LOCAL_ONLY, false);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            attachIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        }
        try {
            activity.startActivityForResult(Intent.createChooser(attachIntent, attachFileChooserTitle), ATTACH_FILE_REQUEST_CODE);
        } catch (ActivityNotFoundException activityNotFoundException) {
            Log.i(TAG, activityNotFoundException.getMessage());
            sendFileAttachmentFailureCallback(ATTACH_FILE_ERROR_CODE_NO_ATTACHMENT_APPLICATION);
        }
    }

    /**
     * Used to process Attach File Result.
     * <p>
     * 1. Once user selects file from some third party application, we get callback to activity's "onActivityResult" method,
     * hence from there call this method to process the result.
     * 2. Process the result data.
     * 3. Send appropriate callback.
     *
     * @param requestCode Request Code.
     * @param resultCode  Result Code.
     * @param data        Data.
     */
    public void processAttachFileResult(final int requestCode, final int resultCode, final Intent data) {
        if (requestCode == ATTACH_FILE_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                if (data != null) {
                    // Attach file success.
                    // Before API - 18 - User can only select only one file & we will get selected file data through intent.getData.
                    // But from API - 18 - User can select multiple file &
                    // If user selects only one file then we will get the data through intent.getData()
                    // If user selects multiple file then we will get the data through intent.getClipData()
                    List<Uri> attachedFileUris = new ArrayList<>();
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                        ClipData clipData = data.getClipData();
                        if (clipData != null) {
                            for (int i = 0; i < clipData.getItemCount(); i++) {
                                ClipData.Item clipDataItem = clipData.getItemAt(i);
                                if (clipDataItem != null) {
                                    Uri attachedFileUri = clipDataItem.getUri();
                                    if (attachedFileUri != null) {
                                        attachedFileUris.add(attachedFileUri);
                                    }
                                }
                            }
                        } else {
                            Uri attachedFileUri = data.getData();
                            if (attachedFileUri != null) {
                                attachedFileUris.add(attachedFileUri);
                            }
                        }
                    } else {
                        Uri attachedFileUri = data.getData();
                        if (attachedFileUri != null) {
                            attachedFileUris.add(attachedFileUri);
                        }
                    }
                    sendFileAttachmentSuccessCallback(attachedFileUris);
                }
            } else {
                sendFileAttachmentFailureCallback(ATTACH_FILE_ERROR_CODE_CANCELLED);
            }
        }
    }

    /**
     * Helper Methods.
     */
    private void sendFileAttachmentSuccessCallback(final List<Uri> attachedFileUris) {
        if (fileAttachmentCallback != null) {
            fileAttachmentCallback.onFileAttachmentSuccess(attachedFileUris);
        }
    }

    private void sendFileAttachmentFailureCallback(final int errorCode) {
        if (fileAttachmentCallback != null) {
            fileAttachmentCallback.onFileAttachmentFailure(errorCode);
        }
    }

}
