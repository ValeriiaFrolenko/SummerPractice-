package ui;

import javafx.scene.Node;

public interface UIWindow {
    void show();
    void hide();
    Node getRoot();
}