package com.vasanth.attachfile.attachment.ui.component;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.vasanth.attachfile.R;
import com.vasanth.attachfile.attachment.model.AttachmentFileDetail;
import com.vasanth.attachfile.attachment.util.AttachmentUtil;

import java.lang.ref.WeakReference;


/**
 * Attachment View.
 * <p/>
 * 1. Responsibility.
 * 1.a. Class contains information about a single attachment file.
 * 1.b. Like its view & about its detail.
 * <p/>
 * 2. Methods.
 * 2.a. getAttachmentView - Used to get attachment view.
 * 2.b. getAttachmentFileDetail - Used to get attachment file detail.
 * <p>
 * 3. Output.
 * 3.a. AttachmentListener - Is used to notify if user has performed action to remove attachment.
 *
 * @author Vasanth
 */
public class Attachment implements View.OnClickListener {

    private static final String TAG = "Attachment";
    private Activity activity;
    private AttachmentFileDetail attachmentFileDetail;
    private AttachmentListener attachmentListener;
    private View attachmentView;
    private ImageView imageViewAttachmentThumbnail;
    private TextView textViewAttachmentName;
    private TextView textViewAttachmentSize;
    private ImageView imageViewRemoveAttachment;

    /**
     * Attachment Listener.
     */
    public interface AttachmentListener {

        /**
         * Remove Attachment.
         * <p/>
         * 1. Gets called to remove the attachment.
         *
         * @param attachment Attachment to be removed.
         */
        void removeAttachment(final Attachment attachment);

    }

    /**
     * Constructor.
     *
     * @param activity             Activity.
     * @param attachmentFileDetail Attachment file detail.
     * @param attachmentListener   Attachment Listener.
     */
    public Attachment(final Activity activity, final AttachmentFileDetail attachmentFileDetail, final AttachmentListener attachmentListener) {
        this.activity = activity;
        this.attachmentFileDetail = attachmentFileDetail;
        this.attachmentListener = attachmentListener;

        createAttachmentView();
    }

    /**
     * View.OnClickListener Methods.
     */
    @Override
    public void onClick(View v) {
        // Remove Attachment.
        if (v.getId() == R.id.attachment_imageView_removeAttachment) {
            removeAttachment();
        }
    }

    /**
     * Used to get attachment view.
     *
     * @return Attachment view.
     */
    public View getAttachmentView() {
        return attachmentView;
    }

    /**
     * Used to get attachment file detail.
     *
     * @return Attachment file detail.
     */
    public AttachmentFileDetail getAttachmentFileDetail() {
        return attachmentFileDetail;
    }

    private void createAttachmentView() {
        initializeAttachmentView();

        populateAttachmentView();

        addListenerForAttachmentView();
    }

    private void initializeAttachmentView() {
        attachmentView = activity.getLayoutInflater().inflate(R.layout.view_attachment, null);
        imageViewAttachmentThumbnail = (ImageView) attachmentView.findViewById(R.id.attachment_imageView_thumbnailImage);
        textViewAttachmentName = (TextView) attachmentView.findViewById(R.id.attachment_textView_attachmentDetailFileName);
        textViewAttachmentSize = (TextView) attachmentView.findViewById(R.id.attachment_textView_attachmentDetailFileSize);
        imageViewRemoveAttachment = (ImageView) attachmentView.findViewById(R.id.attachment_imageView_removeAttachment);
    }

    private void populateAttachmentView() {
        if (attachmentFileDetail != null) {
            textViewAttachmentName.setText(attachmentFileDetail.getName());
            textViewAttachmentSize.setText(AttachmentUtil.getDisplayFileSize(attachmentFileDetail.getSize()));
            // Only for MimeType is "images/.*" - We will get thumbnail & set it.
            // Else we will show default thumbnail.
            if (attachmentFileDetail.getMimeType() != null && attachmentFileDetail.getMimeType().matches("image/.*")) {
                new GetThumbnailForImageAttachment(activity, imageViewAttachmentThumbnail, attachmentFileDetail.getUri()).execute();
            } else {
                imageViewAttachmentThumbnail.setImageResource(R.drawable.ic_attachment_defaultthumbnail);
            }
        }
    }

    private void addListenerForAttachmentView() {
        imageViewRemoveAttachment.setOnClickListener(this);
    }

    private void removeAttachment() {
        if (attachmentListener != null) {
            attachmentListener.removeAttachment(this);
        }
    }

    /**
     * Used to get Thumbnail for the given uri & set it to view.
     * <p>
     * 1. Gets thumbnail for the given uri in background & sets it to view.
     */
    private static class GetThumbnailForImageAttachment extends AsyncTask<Void, Void, Bitmap> {

        private static final int THUMBNAIL_IMAGE_SIZE_DP = 90;
        private WeakReference<Context> contextWeakReference;
        private WeakReference<ImageView> imageViewAttachmentThumbnailWeakReference;
        private Uri attachmentImageFileUri;

        /**
         * Constructor.
         *
         * @param context                      Context.
         * @param imageViewAttachmentThumbnail ImageView Attachment Thumbnail.
         * @param attachmentImageFileUri       Attachment ImageFile Uri.
         */
        public GetThumbnailForImageAttachment(final Context context, final ImageView imageViewAttachmentThumbnail, final Uri attachmentImageFileUri) {
            contextWeakReference = new WeakReference<Context>(context);
            imageViewAttachmentThumbnailWeakReference = new WeakReference<ImageView>(imageViewAttachmentThumbnail);
            this.attachmentImageFileUri = attachmentImageFileUri;
        }

        @Override
        protected Bitmap doInBackground(Void... voids) {
            Bitmap thumbnail = null;
            if (contextWeakReference != null && contextWeakReference.get() != null) {
                try {
                    thumbnail = AttachmentUtil.createThumbnail(contextWeakReference.get(), attachmentImageFileUri, THUMBNAIL_IMAGE_SIZE_DP);
                } catch (Exception exp) {
                    exp.printStackTrace();
                    thumbnail = null;
                }
            }
            return thumbnail;
        }

        @Override
        protected void onPostExecute(Bitmap thumbnail) {
            super.onPostExecute(thumbnail);

            if (imageViewAttachmentThumbnailWeakReference != null && imageViewAttachmentThumbnailWeakReference.get() != null) {
                ImageView imageViewThumbnail = imageViewAttachmentThumbnailWeakReference.get();
                if (thumbnail != null) {
                    imageViewThumbnail.setImageBitmap(thumbnail);
                } else {
                    imageViewThumbnail.setImageResource(R.drawable.ic_attachment_defaultthumbnail);
                }
            }
        }
    }
}
