package com.vasanth.attachfile.attachment.util;

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.util.DisplayMetrics;
import android.webkit.MimeTypeMap;

import com.vasanth.attachfile.attachment.model.AttachmentFileDetail;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * Attachment Util.
 * <p>
 * 1. Responsibility.
 * 1.a. Class used to provide utils functionality for our attachment module.
 * <p>
 * 2. Methods.
 * 2.a. getAttachmentFileDetailFromUri - Used to get attachment file detail from uri.
 * 2.b. getDisplayFileSize - Used to get displayable file size from file size in bytes.
 * 2.c. createThumbnail - Used to create thumbnail for the given URI.
 *
 * @author Vasanth
 */
public class AttachmentUtil {

    /**
     * Used to get attachment file detail from uri.
     * <p>
     * 1. Used to get file detail (uri, name, size, mimeType) from uri.
     * <p>
     * Note:
     * 1. For some Uri's we will get fileSize as "0" - Like "Shared file in google drive" then in those cases we need to read the file content
     * to get actual file size.
     * 2. Reading file content make take long time hence make sure to call this method in separate thread.
     *
     * @param context Context.
     * @param uri     Uri.
     * @return AttachmentFile object if we successfully retrieved data from uri else NULL if we failed to retrieve data.
     */
    public static AttachmentFileDetail getAttachmentFileDetailFromUri(final Context context, final Uri uri) {
        AttachmentFileDetail attachmentFileDetail = null;
        if (uri != null) {
            try {

                // Get fileName, fileSize & fileMimeType.
                String fileName = null;
                long fileSize = 0L;
                String fileMimeType = null;

                // File Scheme.
                if (ContentResolver.SCHEME_FILE.equals(uri.getScheme())) {
                    File file = new File(uri.getPath());
                    fileName = file.getName();
                    fileSize = file.length();
                    fileMimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(getFileExtension(fileName));
                }

                // Content Scheme.
                else if (ContentResolver.SCHEME_CONTENT.equals(uri.getScheme())) {
                    Cursor returnCursor =
                            context.getContentResolver().query(uri, null, null, null, null);
                    if (returnCursor != null && returnCursor.moveToFirst()) {
                        int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                        int sizeIndex = returnCursor.getColumnIndex(OpenableColumns.SIZE);
                        fileName = returnCursor.getString(nameIndex);
                        fileSize = returnCursor.getLong(sizeIndex);
                        fileMimeType = context.getContentResolver().getType(uri);
                        returnCursor.close();
                    }
                    // If "FileSize is 0" - Then get fileSize by reading its content.
                    if (fileSize == 0) {
                        fileSize = getFileSizeFromUri(context, uri);
                    }
                }

                attachmentFileDetail = new AttachmentFileDetail(fileName, fileSize, fileMimeType, uri);
            } catch (Exception exp) {
                exp.printStackTrace();
                attachmentFileDetail = null;
            }
        }
        return attachmentFileDetail;
    }

    /**
     * Used to get file size from uri.
     *
     * @param uri Uri to get file size.
     * @return File size in bytes from uri.
     */
    private static long getFileSizeFromUri(final Context context, final Uri uri) throws IOException {
        long fileSize = 0L;

        InputStream inputStream = context.getContentResolver().openInputStream(uri);
        if (inputStream != null) {
            byte[] bytes = new byte[1024];
            int read = -1;
            while ((read = inputStream.read(bytes)) >= 0) {
                fileSize += read;
            }
        }
        inputStream.close();
        return fileSize;
    }

    /**
     * Used to get display file size.
     * <p>
     * 1. Used to get displayable file size from file size in bytes.
     *
     * @param fileSizeInBytes File Size in Bytes.
     * @return Display file size.
     */
    public static String getDisplayFileSize(final long fileSizeInBytes) {
        String displayFileSize;
        if (fileSizeInBytes >= 1048576) {// 1MB
            displayFileSize = (fileSizeInBytes / 1048576) + " MB";
        } else if (fileSizeInBytes >= 1024) {// 1KB
            displayFileSize = (fileSizeInBytes / 1024) + " KB";
        } else {
            displayFileSize = fileSizeInBytes + " B";
        }
        return displayFileSize;
    }

    /**
     * Gets the extension of a filename.
     *
     * @param fileName The filename to retrieve the extension of.
     * @return The extension of the file or an empty string if none exists.
     */
    private static String getFileExtension(final String fileName) {
        String fileExtension = "";
        if (fileName != null) {
            int lastIndexOfExtension = fileName.lastIndexOf('.');
            int lastIndexOfSeparator = fileName.lastIndexOf('/');
            int index = lastIndexOfSeparator > lastIndexOfExtension ? -1 : lastIndexOfExtension; // We can't have separator ('/') after extension '.'.
            if (index != -1) {
                fileExtension = fileName.substring(index + 1);
            }
        }
        return fileExtension;
    }

    /**
     * Used to create thumbnail for the given URI.
     * <p>
     * 1. Convert the given URI to bitmap.
     * 2. Calculate ratio (depending on thumbnail size) on how much we need to subSample the original bitmap.
     * 3. Create thumbnail bitmap depending on the ration from URI.
     * 4. Reference - http://stackoverflow.com/questions/3879992/how-to-get-bitmap-from-an-uri
     * <p>
     * Note.
     * 1. Make sure to call this method in separate thread because "Getting uri content" may take long time for cloud file (likke Drive files).
     *
     * @param context           Context.
     * @param uri               URI to the file.
     * @param thumbnailSizeInDp Thumbnail size required in DP.
     * @return Thumbnail bitmap created for the given URI.
     * @throws IOException
     */
    public static Bitmap createThumbnail(final Context context, Uri uri, final int thumbnailSizeInDp) throws IOException {

        float thumbnailSizeInPx = convertDpToPixel(thumbnailSizeInDp, context);

        // 1. Convert the given URI to bitmap.
        InputStream input = context.getContentResolver().openInputStream(uri);
        BitmapFactory.Options onlyBoundsOptions = new BitmapFactory.Options();
        onlyBoundsOptions.inJustDecodeBounds = true;
        onlyBoundsOptions.inDither = true;//optional
        onlyBoundsOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;//optional
        BitmapFactory.decodeStream(input, null, onlyBoundsOptions);
        input.close();
        if ((onlyBoundsOptions.outWidth == -1) || (onlyBoundsOptions.outHeight == -1)) {
            return null;
        }

        // 2. Calculate ratio.
        int originalSize = (onlyBoundsOptions.outHeight > onlyBoundsOptions.outWidth) ? onlyBoundsOptions.outHeight : onlyBoundsOptions.outWidth;
        double ratio = (originalSize > thumbnailSizeInPx) ? (originalSize / thumbnailSizeInPx) : 1.0;

        // 3. Create thumbnail bitmap.
        BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
        bitmapOptions.inSampleSize = getPowerOfTwoForSampleRatio(ratio);
        bitmapOptions.inDither = true;//optional
        bitmapOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;//optional
        input = context.getContentResolver().openInputStream(uri);
        Bitmap bitmap = BitmapFactory.decodeStream(input, null, bitmapOptions);
        input.close();

        return bitmap;
    }

    /**
     * FOr Bitmap option inSampleSize - We need to give value in power of two.
     *
     * @param ratio Ratio to be rounded of to power of two.
     * @return Ratio rounded of to nearest power of two.
     */
    private static int getPowerOfTwoForSampleRatio(final double ratio) {
        int k = Integer.highestOneBit((int) Math.floor(ratio));
        if (k == 0) return 1;
        else return k;
    }

    /**
     * This method converts dp unit to equivalent pixels, depending on device density.
     *
     * @param dp      A value in dp (density independent pixels) unit. Which we need to convert into pixels
     * @param context Context to get resources and device specific display metrics
     * @return A float value to represent px equivalent to dp depending on device density
     */
    private static float convertDpToPixel(float dp, Context context) {
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float px = dp * ((float) metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
        return px;
    }
}
