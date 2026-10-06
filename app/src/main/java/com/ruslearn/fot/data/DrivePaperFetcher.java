package com.ruslearn.fot.data;

import android.content.Context;
import android.util.Log;

import com.ruslearn.fot.api.RestClient;
import com.ruslearn.fot.auth.GoogleAuth;
import com.ruslearn.fot.model.Paper;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Downloads a past paper to app storage (FR14). Papers are Drive files, often
 * domain-restricted, so the fetch is layered (ADR-0005):
 *  1. the public Drive download link — works for papers shared with anyone-with-link,
 *  2. the Drive API with the signed-in student's OAuth token — works when the student
 *     approved the drive.readonly scope at sign-in,
 *  3. otherwise a clear failure so the UI can suggest opening the paper online.
 * Call only from background threads.
 */
public class DrivePaperFetcher {

    private static final String TAG = "PaperDownload";
    private static final String DOWNLOAD_SUFFIX = "%PDF";

    /** Result listener; runs on the downloading thread, so post to the UI thread in callers. */
    public interface Callback {
        void onDownloaded(File file);

        void onFailed(String message);
    }

    private final Context context;

    public DrivePaperFetcher(Context context) {
        this.context = context.getApplicationContext();
    }

    public void download(Paper paper, Callback callback) {
        if (paper.getDriveFileId() == null) {
            callback.onFailed("This paper has no Drive file attached.");
            return;
        }
        try {
            File file = tryPublicDownload(paper);
            if (file != null) {
                callback.onDownloaded(file);
                return;
            }
        } catch (IOException e) {
            // Fall through to the authenticated attempt; only keep the failure cause.
            Log.i(TAG, "public download attempt failed", e);
        }
        try {
            File file = tryDriveApiDownload(paper);
            if (file != null) {
                callback.onDownloaded(file);
                return;
            }
            callback.onFailed("This paper is domain-restricted. Open it online with your university account instead.");
        } catch (IOException e) {
            callback.onFailed("Download failed: " + e.getMessage());
        }
    }

    /** Layer 1: the public download URL. Returns null when the response is not a PDF. */
    private File tryPublicDownload(Paper paper) throws IOException {
        String url = "https://drive.google.com/uc?export=download&id=" + paper.getDriveFileId();
        RestClient.RawResponse response = RestClient.get(url);
        if (response.code != 200) {
            return null;
        }
        if (!isPdfBytes(response.bytes)) {
            return null;
        }
        return writeBytes(paper, response.bytes);
    }

    /** Layer 2: Drive API with the student's OAuth token; null on any authorisation issue. */
    private File tryDriveApiDownload(Paper paper) throws IOException {
        String token = GoogleAuth.driveAccessToken(context);
        if (token == null) {
            return null;
        }
        String url = "https://www.googleapis.com/drive/v3/files/" + paper.getDriveFileId()
                + "?alt=media";
        RestClient.RawResponse response = RestClient.get(url, token);
        if (response.code != 200 || !isPdfBytes(response.bytes)) {
            return null;
        }
        return writeBytes(paper, response.bytes);
    }

    private boolean isPdfBytes(byte[] bytes) {
        return bytes != null && bytes.length > 4
                && bytes[0] == '%' && bytes[1] == 'P' && bytes[2] == 'D' && bytes[3] == 'F';
    }

    private File writeBytes(Paper paper, byte[] bytes) throws IOException {
        File dir = context.getExternalFilesDir(null);
        File folder = dir == null ? context.getFilesDir() : dir;
        File papersFolder = new File(folder, "papers");
        if (!papersFolder.exists() && !papersFolder.mkdirs()) {
            throw new IOException("Could not create the papers folder");
        }
        File file = new File(papersFolder, targetName(paper));
        try (FileOutputStream out = new FileOutputStream(file)) {
            out.write(bytes);
        }
        return file;
    }

    /** e.g. "2019_MTT_2207_1uT6KuVosER1ycZ7dLH_U4gUkUp-D2laM.pdf" */
    private static String targetName(Paper paper) {
        String code = paper.getCourseCode().replaceAll("[^A-Za-z0-9_-]", "_");
        String year = paper.getExamYear().replaceAll("[^A-Za-z0-9_-]", "_");
        return year + "_" + code + "_" + paper.getDriveFileId() + ".pdf";
    }
}
