package puzzles;

import entities.Door;
import interfaces.Savable;
import org.json.JSONObject;
import javafx.scene.Node;
import javafx.stage.Stage;

public abstract class Puzzle implements Savable {
    protected PuzzleState state;
    protected Object solution;
    protected Node uiNode;
    protected Stage stage;
    protected Door linkedDoor; // Двері, пов’язані з головоломкою
    protected PuzzleCallback callback; // Callback для повідомлення результату

    public enum PuzzleState { UNSOLVED, SOLVED }

    // Інтерфейс для callback
    public interface PuzzleCallback {
        void onPuzzleSolved(boolean solved, Door door);
    }

    public Puzzle(JSONObject defaultData) {
        this.state = PuzzleState.UNSOLVED;
        this.solution = defaultData.opt("solution");
    }

    // Встановлює двері та callback
    public void setLinkedDoor(Door door, PuzzleCallback callback) {
        this.linkedDoor = door;
        this.callback = callback;
    }

    public abstract void solve(Object input);

    public abstract Node getUI();

    public void initializeFromDefault() {}

    public void initializeFromSave(JSONObject saveData) {
        this.state = PuzzleState.valueOf(saveData.optString("state", "UNSOLVED"));
    }

    @Override
    public JSONObject getSerializableData() {
        JSONObject data = new JSONObject();
        data.put("state", state.toString());
        return data;
    }

    @Override
    public void setFromData(JSONObject data) {
        this.state = PuzzleState.valueOf(data.optString("state", "UNSOLVED"));
    }
}