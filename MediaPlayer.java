import java.io.File;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

public class MediaPlayer extends Thread{
    String path;
    boolean loop = false;

    MediaPlayer(String path)
    {
        this.path = path;
    }
    void playLoop() {
        try {
            File soundPath = new File(path);
    
            if (soundPath.exists()) {
                AudioInputStream audioInput =   AudioSystem.getAudioInputStream(soundPath);
                Clip clip = AudioSystem.getClip();
                clip.open(audioInput);
                clip.start();
                clip.loop(Clip.LOOP_CONTINUOUSLY);

                // JOptionPane.showMessageDialog(null, "press ok to stop");
            }
        } catch (Exception e) {
            // TODO: handle exception
        }
    }

    public void run() {
        playLoop();
        while(loop);
        loop = true;
    }

    public void stopLoop()
    {
        loop = false;
    }
}
