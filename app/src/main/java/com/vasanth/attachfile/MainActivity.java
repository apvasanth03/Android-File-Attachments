package com.vasanth.attachfile;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.Toast;

import com.vasanth.attachfile.attachment.model.AttachmentFileDetail;
import com.vasanth.attachfile.attachment.ui.component.Attachment;
import com.vasanth.attachfile.attachment.util.AttachmentUtil;
import com.vasanth.attachfile.attachment.util.FileAttachmentUtil;
import com.vasanth.attachfile.util.SnackBarHelper;
import com.vasanth.attachfile.util.UserPermissionHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Main Activity.
 * <p>
 * 1. Responsibility.
 * 1.a. Activity responsible to allow user to pick files from his device & attach it.
 *
 * @author Vasanth
 */
public class MainActivity extends AppCompatActivity implements View.OnClickListener, UserPermissionHelper.UserPermissionCallback,
        FileAttachmentUtil.FileAttachmentCallback, Attachment.AttachmentListener {

    private static final String TAG = "MainActivity";
    private static final int REQUEST_CODE_USER_PERMISSIONS_WRITE_STORAGE = 1;

    private ScrollView scrollViewRoot;
    private Button buttonAttachFile;
    private ViewGroup viewGroupAttachmentHolder;
    private ProgressDialog progressDialog;

    private List<Attachment> attachments;
    private FileAttachmentUtil fileAttachmentUtil;

    /**
     * ACTIVITY METHODS.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        scrollViewRoot = (ScrollView) findViewById(R.id.scrollView_activityMain_root);
        buttonAttachFile = (Button) findViewById(R.id.button_activityMain_attachFile);
        viewGroupAttachmentHolder = (ViewGroup) findViewById(R.id.linearLayout_activityMain_attachmentHolder);

        buttonAttachFile.setOnClickListener(this);
    }

    /**
     * 1. If RequestCode is for "AttachFile" then pass it to "FileAttachmentUtil" to process it.
     */
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case FileAttachmentUtil.ATTACH_FILE_REQUEST_CODE:
                processAttachFileResult(requestCode, resultCode, data);
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * 1. Pass "Permission Result" to "UserPermissionHelper" process it.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        UserPermissionHelper.getInstance().onRequestPermissionsResult(requestCode, permissions, grantResults);
    }


    /**
     * View.OnClickListener METHODS.
     */
    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.button_activityMain_attachFile) {
            checkIfWeHavePermissionToWriteStorage(false);
        }
    }

    /**
     * Attachment.AttachmentListener METHODS.
     */
    @Override
    public void removeAttachment(Attachment attachment) {
        // Remove attachment.
        if (attachment != null) {
            attachments.remove(attachment);
            viewGroupAttachmentHolder.removeView(attachment.getAttachmentView());
        }
    }

    /**
     * ATTACH FILE STUFF.
     */
    private void attachFile() {
        if (fileAttachmentUtil == null) {
            fileAttachmentUtil = new FileAttachmentUtil(this, this);
        }
        fileAttachmentUtil.attachFile(getString(R.string.attach_file));
    }

    private void processAttachFileResult(final int requestCode, final int resultCode, final Intent data) {
        if (fileAttachmentUtil != null) {
            fileAttachmentUtil.processAttachFileResult(requestCode, resultCode, data);
        }
    }

    /**
     * FileAttachmentUtil.FileAttachmentCallback Methods.
     */
    @Override
    public void onFileAttachmentSuccess(List<Uri> attachedFileUris) {
        // Get attachments & populate.
        new GetAttachmentFileDetailsAndPopulate(attachedFileUris).execute();
    }

    @Override
    public void onFileAttachmentFailure(int errorCode) {
        switch (errorCode) {
            case FileAttachmentUtil.ATTACH_FILE_ERROR_CODE_NO_ATTACHMENT_APPLICATION:
                Toast.makeText(this, getString(R.string.attachment_error_noApplicationToChooseFileErrorMessage), Toast.LENGTH_SHORT).show();
                break;

            case FileAttachmentUtil.ATTACH_FILE_ERROR_CODE_FAILED:
                Toast.makeText(this, getResources().getQuantityString(R.plurals.attachment_error_failedToAttachFileErrorMessage,
                        1), Toast.LENGTH_SHORT).show();
                break;
        }
    }

    /**
     * Used to get AttachmentFileDetails & Populate View.
     */
    private class GetAttachmentFileDetailsAndPopulate extends AsyncTask<Void, Void, Void> {

        private List<Uri> attachedFileUris;
        private List<AttachmentFileDetail> attachmentFileDetails;
        private List<Uri> failedAttachmentFileUris;

        /**
         * Constructor.
         *
         * @param attachedFileUris AttachedFileUri's for which we need to get its details.
         */
        public GetAttachmentFileDetailsAndPopulate(final List<Uri> attachedFileUris) {
            this.attachedFileUris = attachedFileUris;
        }

        // Show Progress to get the attachment details.
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            showProgressDialog();
        }

        // Get attachment file details from its uri.
        @Override
        protected Void doInBackground(Void... voids) {
            if (attachedFileUris != null && attachedFileUris.size() > 0) {
                attachmentFileDetails = new ArrayList<>();
                failedAttachmentFileUris = new ArrayList<>();
                for (Uri attachmentFileUri : attachedFileUris) {
                    if (attachmentFileUri != null) {
                        AttachmentFileDetail attachmentFileDetail = AttachmentUtil.getAttachmentFileDetailFromUri(MainActivity.this, attachmentFileUri);
                        if (attachmentFileDetail != null) {
                            attachmentFileDetails.add(attachmentFileDetail);
                        } else {
                            failedAttachmentFileUris.add(attachmentFileUri);
                        }
                    }
                }
            }
            return null;
        }

        // Hide progress & create & populate attachments.
        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            hideProgressDialog();

            // Create attachments view.
            List<Attachment> attachments = null;
            if (attachmentFileDetails != null) {
                attachments = new ArrayList<>();
                for (AttachmentFileDetail attachmentFileDetail : attachmentFileDetails) {
                    if (attachmentFileDetail != null) {
                        Attachment attachment = new Attachment(MainActivity.this, attachmentFileDetail, MainActivity.this);
                        attachments.add(attachment);
                    }
                }
            }

            // Populate attachments view.
            if (attachments != null) {
                for (Attachment attachment : attachments) {
                    if (attachment != null) {
                        viewGroupAttachmentHolder.addView(attachment.getAttachmentView());
                    }
                }
            }

            // Add attachments to the parent list
            if (attachments != null) {
                if (MainActivity.this.attachments == null) {
                    MainActivity.this.attachments = new ArrayList<>();
                }
                MainActivity.this.attachments.addAll(attachments);
            }

            // Notify user if there is any failed uri.
            if (failedAttachmentFileUris != null && failedAttachmentFileUris.size() > 0) {
                Toast.makeText(MainActivity.this, getResources().getQuantityString(R.plurals.attachment_error_failedToAttachFileErrorMessage,
                        failedAttachmentFileUris.size()), Toast.LENGTH_SHORT).show();
            }
        }

    }

    /**
     * PERMISSION STUFF.
     */
    /**
     * UserPermissionHelper.UserPermissionCallback Methods.
     */
    @Override
    public void userGrantedPermission(int requestCode) {
        if (requestCode == REQUEST_CODE_USER_PERMISSIONS_WRITE_STORAGE) {
            weHavePermissionToWriteStorage();
        }
    }

    @Override
    public void userDeniedPermission(int requestCode) {
        if (requestCode == REQUEST_CODE_USER_PERMISSIONS_WRITE_STORAGE) {
            weDontHavePermissionToWriteStorage();
        }
    }

    @Override
    public void expainToUserWhyWeNeedPermission(int requestCode) {
        if (requestCode == REQUEST_CODE_USER_PERMISSIONS_WRITE_STORAGE) {
            explainToUserWhyWeNeedWriteStoragePermission();
        }
    }

    /**
     * Used to check if we have permission to write storage.
     *
     * @param isShownExplaination TRUE if we have already shown explanation to user why we need this permission.
     */
    private void checkIfWeHavePermissionToWriteStorage(final boolean isShownExplaination) {
        String permissionName = Manifest.permission.WRITE_EXTERNAL_STORAGE;
        UserPermissionHelper.getInstance().checkIfUserHasGrantedPermission(this, permissionName, isShownExplaination,
                REQUEST_CODE_USER_PERMISSIONS_WRITE_STORAGE, this);
    }

    /**
     * Gets called if we have permission to write storage.
     */
    private void weHavePermissionToWriteStorage() {
        attachFile();
    }

    /**
     * Get called if we don't have permission to write storage.
     */
    private void weDontHavePermissionToWriteStorage() {
        Snackbar snackbar = SnackBarHelper.getInstance(this).getSnackBar(scrollViewRoot, getString(R.string.user_permission_attach_file_explanation_message),
                Snackbar.LENGTH_LONG, true, getString(R.string.user_permission_settings), new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Go to settings.
                        Intent intent = new Intent();
                        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", MainActivity.this.getPackageName(), null);
                        intent.setData(uri);
                        startActivity(intent);
                    }
                });
        snackbar.show();
    }

    /**
     * Gets called if we need to explain to user why we need write storage permission.
     */
    private void explainToUserWhyWeNeedWriteStoragePermission() {
        checkIfWeHavePermissionToWriteStorage(true);
    }

    /**
     * HELPER METHODS.
     */
    private void showProgressDialog() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getString(R.string.attachment_loaderMessageWhileGettingAttachmentContent));
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    private void hideProgressDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }
}
