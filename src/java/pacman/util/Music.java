package pacman.util;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import java.io.File;
import java.net.URI;

public class Music {

    private Clip clip;

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

    public boolean isStop() {
        return !clip.isRunning();
    }

    public void stopMusic() {
        clip.stop();
    }

    public void startMusic() {
        clip.start();
        clip.loop(Clip.LOOP_CONTINUOUSLY);
    }
}
