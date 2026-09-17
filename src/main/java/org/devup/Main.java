package org.devup;

import org.devup.ui.DevUpGui;

import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new DevUpGui().show());
    }
}
