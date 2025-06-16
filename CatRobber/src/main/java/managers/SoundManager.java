package managers;

import javafx.scene.media.AudioClip;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import java.io.File;
import java.util.EnumMap;
import java.util.Map;

public class SoundManager {
    public enum SoundType {
        BUTTON_CLICK, CODE_LOCK_CLICK, CODE_LOCK_CLOSED, CODE_LOCK_OPEN,
        COLLECT_MONEY, DOOR_OPEN, FAIL_GAME, HIT, HITTED,
        KEYBOARD_TYPING, MOVE_PICTURE, MOVE_SHIELD, PICK_LOCK_CLOSED, PICK_LOCK_OPEN, RUN, SHOOT,
        TAKE_NOTE, VICTORY_GAME, WIRE_CUT
    }

    private Map<SoundType, AudioClip> sounds;
    private MediaPlayer currentMusic;
    private MediaPlayer currentSound;
    private double masterVolume = 1;
    private double soundVolume = 1;
    private double musicVolume = 1;
    private boolean isRunningSoundPlay = false;

    private static SoundManager instance;

    public static SoundManager getInstance() {
        if (instance == null) {
            instance = new SoundManager();
        }
        return instance;
    }

    private SoundManager() {
        sounds = new EnumMap<>(SoundType.class);
        loadSounds();
        initRunSound();
    }

    public void initRunSound() {
        String path = getFilePath("assets\\music\\sounds\\run.mp3" );
        System.out.println("Спроба завантажити музику з: " + path);
        Media media = new Media(path);
        currentSound = new MediaPlayer(media);
        currentSound.setCycleCount(MediaPlayer.INDEFINITE);
        currentSound.setVolume(masterVolume * soundVolume);
    }

    public void startRunSound() {
        if (!isRunningSoundPlay && currentSound != null) {
            currentSound.play();
            isRunningSoundPlay = true;
        }
    }

    public void stopRunSound() {
        if (isRunningSoundPlay && currentSound != null) {
            currentSound.pause();
            isRunningSoundPlay = false;
        }
    }

    private void loadSounds() {
        loadSound(SoundType.BUTTON_CLICK, "assets/music/sounds/button_click.mp3");
        loadSound(SoundType.CODE_LOCK_CLICK, "assets/music/sounds/code_lock_click.mp3");
        loadSound(SoundType.CODE_LOCK_CLOSED, "assets/music/sounds/code_lock_closed.mp3");
        loadSound(SoundType.CODE_LOCK_OPEN, "assets/music/sounds/code_lock_open.mp3");
        loadSound(SoundType.COLLECT_MONEY, "assets/music/sounds/collect_money.mp3");
        loadSound(SoundType.DOOR_OPEN, "assets/music/sounds/door_open.mp3");
        loadSound(SoundType.FAIL_GAME, "assets/music/sounds/fail_game.mp3");
        loadSound(SoundType.HIT, "assets/music/sounds/hit.mp3");
        loadSound(SoundType.HITTED, "assets/music/sounds/hitted.mp3");
        loadSound(SoundType.KEYBOARD_TYPING, "assets/music/sounds/keyboard_typing.mp3");
        loadSound(SoundType.MOVE_PICTURE, "assets/music/sounds/move_picture.mp3");
        loadSound(SoundType.MOVE_SHIELD, "assets/music/sounds/move_shield.mp3");
        loadSound(SoundType.PICK_LOCK_CLOSED, "assets/music/sounds/pick_lock_closed.mp3");
        loadSound(SoundType.PICK_LOCK_OPEN, "assets/music/sounds/pick_lock_open.mp3");
        loadSound(SoundType.RUN, "assets/music/sounds/run.mp3");
        loadSound(SoundType.SHOOT, "assets/music/sounds/shoot.mp3");
        loadSound(SoundType.TAKE_NOTE, "assets/music/sounds/take_note.mp3");
        loadSound(SoundType.VICTORY_GAME, "assets/music/sounds/victory_game.mp3");
        loadSound(SoundType.WIRE_CUT, "assets/music/sounds/wire_cut.mp3");
    }

    private String getFilePath(String relativePath) {
        try {
            File file = new File(System.getProperty("user.dir"), relativePath);
            return file.toURI().toString();
        } catch (Exception e) {
            System.err.println("Помилка отримання шляху: " + relativePath);
            return null;
        }
    }

    private void loadSound(SoundType type, String relativePath) {
        try {
            File file = new File(System.getProperty("user.dir"), relativePath);
            AudioClip clip = new AudioClip(file.toURI().toString());
            sounds.put(type, clip);
        } catch (Exception e) {
            System.err.println("Не вдалося завантажити звук: " + relativePath);
            e.printStackTrace();
        }
    }

    public void playSound(SoundType type) {
        AudioClip clip = sounds.get(type);
        if (clip != null) {
            clip.setVolume(masterVolume * soundVolume);
            clip.play();
        }
    }


    public void playMusic(String trackName) {
        stopMusic(); // зупинити поточну
        try {
            String path = getFilePath("assets\\music\\background\\" + trackName);
            System.out.println("Спроба завантажити музику з: " + path);
            Media media = new Media(path);
            currentMusic = new MediaPlayer(media);
            currentMusic.setVolume(masterVolume * musicVolume);
            currentMusic.setCycleCount(MediaPlayer.INDEFINITE);
            currentMusic.play();
        } catch (Exception e) {
            System.err.println("Не вдалося завантажити музику: " + trackName);
            e.printStackTrace();
        }
    }

    public void stopMusic() {
        if (currentMusic != null) {
            currentMusic.stop();
            currentMusic = null;
        }
    }

    public void stopSoundEffects() {
        for (AudioClip clip : sounds.values()) {
            clip.stop();
        }
        stopRunSound(); // якщо біжить звук бігу — теж зупиняємо
    }

    public void stopAllSounds() {
        for (AudioClip clip : sounds.values()) {
            clip.stop();
        }
        stopMusic();
    }

    public void pauseMusic() {
        if (currentMusic != null) {
            currentMusic.pause();
        }
    }

    public void resumeMusic() {
        if (currentMusic != null) {
            currentMusic.play();
        }
    }

    public void setMasterVolume(double volume) {
        masterVolume = volume;
        if (currentMusic != null) {
            currentMusic.setVolume(masterVolume * musicVolume);
        }
    }

    public void setSoundVolume(double volume) {
        soundVolume = volume;
    }

    public void setMusicVolume(double volume) {
        musicVolume = volume;
        if (currentMusic != null) {
            currentMusic.setVolume(masterVolume * musicVolume);
        }
    }
}
