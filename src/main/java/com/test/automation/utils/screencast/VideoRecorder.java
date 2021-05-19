package com.test.automation.utils.screencast;

import org.apache.log4j.Logger;
import org.monte.media.Format;
import org.monte.media.math.Rational;
import org.monte.screenrecorder.ScreenRecorder;

import java.awt.*;
import java.io.File;
import java.util.List;

import static org.monte.media.VideoFormatKeys.*;

/**
 * Created by tmuminova on 4/10/20.
 */
public class VideoRecorder {
    private final static Logger logger = Logger.getLogger(VideoRecorder.class);
    private ScreenRecorder screenRecorder;
    public boolean isRecording = false;

    public void startRecording(String movieFolder) throws Exception {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int width = screenSize.width;
        int height = screenSize.height;
        Rectangle captureSize = new Rectangle(0, 0, width, height);

        GraphicsConfiguration gc = GraphicsEnvironment
                .getLocalGraphicsEnvironment()
                .getDefaultScreenDevice()
                .getDefaultConfiguration();

        this.screenRecorder = new ScreenRecorder(gc,
                captureSize,
                new Format(MediaTypeKey, MediaType.FILE, MimeTypeKey, MIME_AVI),
                new Format(MediaTypeKey, MediaType.VIDEO, EncodingKey, ENCODING_AVI_TECHSMITH_SCREEN_CAPTURE,
                        CompressorNameKey, ENCODING_AVI_TECHSMITH_SCREEN_CAPTURE,
                        DepthKey, 24, FrameRateKey, Rational.valueOf(15),
                        QualityKey, 1.0f,
                        KeyFrameIntervalKey, 15 * 60),
                new Format(MediaTypeKey, MediaType.VIDEO, EncodingKey, "black",
                        FrameRateKey, Rational.valueOf(30)),
                null,
                new File(movieFolder));
        this.screenRecorder.start();
        this.isRecording = true;
    }

    public void stopRecording() throws Exception {
        this.screenRecorder.stop();
        this.isRecording = false;
        List<File> listVideos = this.screenRecorder.getCreatedMovieFiles();
        if (listVideos.size() != 1) {
            logger.error("VideoRecorder recorded " + listVideos.size() + " videos instead of 1.");
        } else {
            File videoFile = listVideos.get(0);
            File newVideoFile = new File(videoFile.getParent() + File.separator + "screencast.avi");
            boolean moveOK = videoFile.renameTo(newVideoFile);
            logger.info("Screencast file renamed OK: " + moveOK);
        }
    }
}
