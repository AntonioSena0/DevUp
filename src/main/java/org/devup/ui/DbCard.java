package org.devup.ui;

import org.devup.core.ConfigStore;
import org.devup.core.DockerManager;
import org.devup.db.DbPreset;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.util.function.Consumer;

import static org.devup.ui.UiStyle.BG_PANEL;
import static org.devup.ui.UiStyle.BTN_BLUE;
import static org.devup.ui.UiStyle.BTN_DARK;
import static org.devup.ui.UiStyle.BTN_RED;
import static org.devup.ui.UiStyle.BTN_RED_DARK;
import static org.devup.ui.UiStyle.STATUS_BG_OFF;
import static org.devup.ui.UiStyle.STATUS_OFF;
import static org.devup.ui.UiStyle.TEXT_DIM;
import static org.devup.ui.UiStyle.styleStatusPill;
import static org.devup.ui.UiStyle.styledBorder;
import static org.devup.ui.UiStyle.styledButton;

public final class DbCard extends JPanel {

    public interface Host {
        ConfigStore config();
        void runTask(String startMsg, Consumer<DockerManager> task, boolean refreshAfter);
        void log(String msg);
        boolean confirmReset(DbPreset db);
        void trackDockerDependent(JButton b);
    }

    private final DbPreset db;
    private final Host host;
    private final JLabel statusLabel = new JLabel("...", JLabel.CENTER);
    private final JLabel infoLabel = new JLabel();

    public DbCard(DbPreset db, Host host) {
        super(new BorderLayout(8, 8));
        this.db = db;
        this.host = host;
        setBackground(BG_PANEL);
        setBorder(styledBorder(db.id));
        styleStatusPill(statusLabel, STATUS_OFF, STATUS_BG_OFF);
        infoLabel.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        infoLabel.setForeground(TEXT_DIM);

        JButton on = styledButton("Ligar", BTN_BLUE, BTN_DARK);
        JButton off = styledButton("Parar", BTN_BLUE, BTN_DARK);
        JButton test = styledButton("Testar", BTN_BLUE, BTN_DARK);
        JButton logs = styledButton("Ver logs", BTN_BLUE, BTN_DARK);
        JButton copy = styledButton("Copiar URL", BTN_BLUE, BTN_DARK);
        JButton reset = styledButton("Apagar tudo", BTN_RED, BTN_RED_DARK);

        on.addActionListener(e -> host.runTask("Ligando " + db.id + "...", mgr -> mgr.up(db), true));
        off.addActionListener(e -> host.runTask("Parando " + db.id + "...", mgr -> mgr.down(db), true));
        test.addActionListener(e -> host.runTask("Testando " + db.id + "...", mgr -> {
            int port = port();
            boolean ok = mgr.jdbcOk(db, port, DbPreset.FIXED_PASS);
            host.log((ok ? "OK " : "FALHA ") + db.id + " -> " + db.jdbcUrl("localhost", port));
        }, false));
        logs.addActionListener(e -> host.runTask("Logs " + db.id, mgr -> host.log(mgr.logs(db, 80)), false));
        copy.addActionListener(e -> {
            String url = db.jdbcUrl("localhost", port());
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(url), null);
            host.log("URL copiada: " + url);
        });
        reset.addActionListener(e -> {
            if (host.confirmReset(db)) host.runTask("Resetando " + db.id + "...", mgr -> mgr.reset(db), true);
        });
        host.trackDockerDependent(on);
        host.trackDockerDependent(off);
        host.trackDockerDependent(test);
        host.trackDockerDependent(reset);

        JPanel buttons = new JPanel(new GridLayout(3, 2, 8, 8));
        buttons.setBackground(BG_PANEL);
        buttons.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
        buttons.add(on);
        buttons.add(off);
        buttons.add(test);
        buttons.add(logs);
        buttons.add(copy);
        buttons.add(reset);

        JPanel header = new JPanel(new GridLayout(2, 1, 6, 6));
        header.setBackground(BG_PANEL);
        header.setBorder(BorderFactory.createEmptyBorder(10, 10, 4, 10));
        header.add(statusLabel);
        header.add(infoLabel);
        add(header, BorderLayout.NORTH);
        add(buttons, BorderLayout.CENTER);
    }

    public DbPreset preset() {
        return db;
    }

    public JLabel statusLabel() {
        return statusLabel;
    }

    public JLabel infoLabel() {
        return infoLabel;
    }

    private int port() {
        return Integer.parseInt(host.config().get(db.id + ".port", String.valueOf(db.defaultPort)));
    }
}
