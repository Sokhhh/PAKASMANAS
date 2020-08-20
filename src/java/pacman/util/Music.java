package pacman.util;

import java.io.File;
import java.net.URI;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

/**
 * Contains an utility to play background music.
 *
 * @version 1.0
 */
public class Music {
    /** Contains the music clip. */
    private Clip clip;

    /**
     * Creates a new Music instance.
     *
     * @param musicLocation the path of the music file
     */
    public Music(URI musicLocation) {
        try {
            File musicPath = new File(musicLocation);
            if (musicPath.exists()) {
                AudioInputStream audioInput = AudioSystem.getAudioInputStream(musicPath);
                clip = AudioSystem.getClip();
                clip.open(audioInput);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    /**
     * Indicates whether the music is running.
     *
     * @return <code>true</code> if the line is running, otherwise <code>false</code>
     * @see #start()
     * @see #stop()
     */
    public boolean isRunning() {
        return clip.isRunning();
    }

    /**
     * Starts playing the music.
     */
    public void start() {
        clip.start();
        clip.loop(Clip.LOOP_CONTINUOUSLY);
    }

    /**
     * Stops playing the music.
     */
    public void stop() {
        clip.stop();
    }
}
