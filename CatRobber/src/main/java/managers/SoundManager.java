package managers;

import javafx.scene.media.AudioClip;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import java.util.Map;

// Керує звуками та музикою
public class SoundManager {
    // Поля
    private Map<SoundType, AudioClip> sounds; // Звуки за типами
    private MediaPlayer currentMusic; // Поточна музика
    private double masterVolume; // Загальна гучність
    private double soundVolume; // Гучність звуків
    private double musicVolume; // Гучність музики

    // Енум для типів звуків
    public enum SoundType { STEP, DOOR_OPEN, ALARM, BUTTON_CLICK }

    // Конструктор
    public SoundManager() {}

    // Відтворює звук
    // Отримує тип звуку (STEP, DOOR_OPEN, тощо)
    public void playSound(SoundType type) {}

    // Відтворює музику
    // Отримує шлях із /assets/music/ (наприклад, background.mp3)
    public void playMusic(String track) {}

    // Встановлює загальну гучність
    // Отримує значення від Settings
    public void setMasterVolume(double volume) {}

    // Встановлює гучність звуків
    // Отримує значення від Settings
    public void setSoundVolume(double volume) {}

    // Встановлює гучність музики
    // Отримує значення від Settings
    public void setMusicVolume(double volume) {}

    // Зупиняє всі звуки
    public void stopAllSounds() {}

    // Ставить музику на паузу
    public void pauseMusic() {}

    // Відновлює музику
    public void resumeMusic() {}
}