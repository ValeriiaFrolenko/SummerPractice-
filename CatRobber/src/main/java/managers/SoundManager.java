package managers;

import javafx.scene.media.AudioClip;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import java.io.File;
import java.util.EnumMap;
import java.util.Map;

/**
 * Клас для управління звуками та музикою в грі. Реалізує патерн Singleton.
 */
public class SoundManager {
    /**
     * Перелік типів звукових ефектів у грі.
     */
    public enum SoundType {
        BUTTON_CLICK, CODE_LOCK_CLICK, CODE_LOCK_CLOSED, CODE_LOCK_OPEN,
        COLLECT_MONEY, DOOR_OPEN, FAIL_GAME, HIT, HITTED,
        KEYBOARD_TYPING, MOVE_PICTURE, MOVE_SHIELD, PICK_LOCK_CLOSED, PICK_LOCK_OPEN, RUN, SHOOT,
        TAKE_NOTE, VICTORY_GAME, WIRE_CUT
    }

    /**
     * Колекція звукових ефектів, прив’язаних до їх типів.
     */
    private Map<SoundType, AudioClip> sounds;

    /**
     * Поточний об’єкт для відтворення фонової музики.
     */
    private MediaPlayer currentMusic;

    /**
     * Поточний об’єкт для відтворення звуку бігу.
     */
    private MediaPlayer currentSound;

    /**
     * Загальна гучність для всіх звуків і музики.
     */
    private double masterVolume = 1;

    /**
     * Гучність звукових ефектів.
     */
    private double soundVolume = 1;

    /**
     * Гучність фонової музики.
     */
    private double musicVolume = 1;

    /**
     * Прапорець, що вказує, чи відтворюється звук бігу.
     */
    private boolean isRunningSoundPlay = false;

    /**
     * Єдиний екземпляр класу SoundManager (патерн Singleton).
     */
    private static SoundManager instance;

    /**
     * Повертає єдиний екземпляр класу SoundManager.
     *
     * @return екземпляр SoundManager
     */
    public static SoundManager getInstance() {
        if (instance == null) {
            instance = new SoundManager();
        }
        return instance;
    }

    /**
     * Приватний конструктор для ініціалізації звукових ефектів і бігу.
     */
    private SoundManager() {
        sounds = new EnumMap<>(SoundType.class);
        loadSounds();
        initRunSound();
    }

    /**
     * Ініціалізує звук бігу для безперервного відтворення.
     */
    public void initRunSound() {
        String path = getFilePath("assets\\music\\sounds\\run.mp3");
        Media media = new Media(path);
        currentSound = new MediaPlayer(media);
        currentSound.setCycleCount(MediaPlayer.INDEFINITE);
        currentSound.setVolume(masterVolume * soundVolume);
    }

    /**
     * Запускає відтворення звуку бігу, якщо він ще не відтворюється.
     */
    public void startRunSound() {
        if (!isRunningSoundPlay && currentSound != null) {
            currentSound.play();
            isRunningSoundPlay = true;
        }
    }

    /**
     * Зупиняє відтворення звуку бігу, якщо він відтворюється.
     */
    public void stopRunSound() {
        if (isRunningSoundPlay && currentSound != null) {
            currentSound.pause();
            isRunningSoundPlay = false;
        }
    }

    /**
     * Завантажує всі звукові ефекти, визначені в SoundType.
     */
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

    /**
     * Отримує повний шлях до файлу відносно директорії проекту.
     *
     * @param relativePath відносний шлях до файлу
     * @return повний шлях до файлу у форматі URI
     */
    private String getFilePath(String relativePath) {
        try {
            File file = new File(System.getProperty("user.dir"), relativePath);
            return file.toURI().toString();
        } catch (Exception e) {
            System.err.println("Помилка отримання шляху: " + relativePath);
            return null;
        }
    }

    /**
     * Завантажує звуковий ефект і додає його до колекції.
     *
     * @param type         тип звукового ефекту
     * @param relativePath відносний шлях до файлу звуку
     */
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

    /**
     * Відтворює звуковий ефект за його типом.
     *
     * @param type тип звукового ефекту
     */
    public void playSound(SoundType type) {
        AudioClip clip = sounds.get(type);
        if (clip != null) {
            clip.setVolume(masterVolume * soundVolume);
            clip.play();
        }
    }

    /**
     * Відтворює фонову музику за назвою треку.
     *
     * @param trackName ім’я файлу треку
     */
    public void playMusic(String trackName) {
        stopMusic();
        try {
            String path = getFilePath("assets\\music\\background\\" + trackName);
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

    /**
     * Зупиняє відтворення фонової музики.
     */
    public void stopMusic() {
        if (currentMusic != null) {
            currentMusic.stop();
            currentMusic = null;
        }
    }

    /**
     * Зупиняє всі звукові ефекти, включаючи звук бігу.
     */
    public void stopSoundEffects() {
        for (AudioClip clip : sounds.values()) {
            clip.stop();
        }
        stopRunSound();
    }

    /**
     * Зупиняє всі звуки та музику в грі.
     */
    public void stopAllSounds() {
        for (AudioClip clip : sounds.values()) {
            clip.stop();
        }
        stopMusic();
    }

}