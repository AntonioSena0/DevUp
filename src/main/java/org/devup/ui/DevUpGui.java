package org.devup.ui;

import org.devup.core.ConfigStore;
import org.devup.core.DockerManager;
import org.devup.core.DockerRunner;
import org.devup.db.DbPreset;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.border.TitledBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.io.File;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;

import static org.devup.ui.UiStyle.*;

public final class DevUpGui implements DbCard.Host {

    private final ConfigStore config = new ConfigStore();
    private final JTextArea output = new JTextArea(16, 80);
    private final EnumMap<DbPreset, JLabel> statusOf = new EnumMap<>(DbPreset.class);
    private final EnumMap<DbPreset, JLabel> infoOf = new EnumMap<>(DbPreset.class);
    private static final String[] DOCKER_PATHS = {
        "C:\\Program Files\\Docker\\Docker\\Docker Desktop.exe"
    };
    private final JLabel dockerDot = new JLabel();
    private final JPanel banner = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 8));
    private final List<JButton> dockerDependent = new ArrayList<>();
    private JFrame frame;
    private volatile boolean refreshing = false;

    public void show() {
        frame = new JFrame("DevUp");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        try {
            java.net.URL iconUrl = getClass().getResource("/devup.png");
            if (iconUrl != null) frame.setIconImage(new javax.swing.ImageIcon(iconUrl).getImage());
        } catch (Exception ignored) {}
        frame.setLayout(new BorderLayout(12, 12));
        frame.getContentPane().setBackground(BG_MAIN);
        frame.setMinimumSize(new Dimension(1080, 800));
        frame.setSize(1150, 840);

        JPanel cards = new JPanel(new GridLayout(0, 2, 14, 14));
        cards.setBackground(BG_MAIN);
        cards.setBorder(BorderFactory.createEmptyBorder(10, 14, 0, 14));
        for (DbPreset db : DbPreset.values()) {
            DbCard card = new DbCard(db, this);
            statusOf.put(db, card.statusLabel());
            infoOf.put(db, card.infoLabel());
            cards.add(card);
        }

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 12));
        top.setBackground(BG_TOP);
        top.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(40, 100, 180)));
        JLabel logo = new JLabel();
        try {
            java.net.URL logoUrl = getClass().getResource("/header-symbol.png");
            if (logoUrl != null) {
                javax.swing.ImageIcon original = new javax.swing.ImageIcon(logoUrl);
                int h = 48;
                int w = original.getIconWidth() * h / Math.max(1, original.getIconHeight());
                logo.setIcon(new javax.swing.ImageIcon(
                        original.getImage().getScaledInstance(w, h, java.awt.Image.SCALE_SMOOTH)));
            }
        } catch (Exception ignored) {}
        JPanel titles = new JPanel(new GridLayout(2, 1, 0, 0));
        titles.setBackground(BG_TOP);
        JLabel title = new JLabel("DevUp");
        title.setForeground(TEXT_MAIN);
        title.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 24));
        JLabel subtitle = new JLabel("root  |  devup_database  |  localhost");
        subtitle.setForeground(TEXT_DIM);
        subtitle.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        titles.add(title);
        titles.add(subtitle);
        JButton upAll = styledButton("Ligar tudo", BTN_BLUE, BTN_DARK);
        JButton downAll = styledButton("Desligar tudo", BTN_BLUE, BTN_DARK);
        JButton refresh = styledButton("Atualizar status", BTN_BLUE, BTN_DARK);
        dockerDot.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        upAll.addActionListener(e -> runTask("Ligando tudo...", mgr -> {
            for (DbPreset db : DbPreset.values()) mgr.up(db);
        }, true));
        downAll.addActionListener(e -> runTask("Desligando tudo...", mgr -> {
            for (DbPreset db : DbPreset.values()) mgr.down(db);
        }, true));
        refresh.addActionListener(e -> refreshStatusQuiet());
        dockerDependent.add(upAll);
        dockerDependent.add(downAll);
        top.add(logo);
        top.add(titles);
        top.add(upAll);
        top.add(downAll);
        top.add(refresh);
        top.add(dockerDot);

        banner.setBackground(BG_BANNER);
        banner.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(200, 80, 90)));
        banner.setVisible(false);
        JLabel bannerMsg = new JLabel("Docker Desktop nao esta rodando. Abra o Docker Desktop e aguarde o icone ficar verde.");
        bannerMsg.setForeground(new Color(255, 200, 205));
        bannerMsg.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        JButton openDocker = styledButton("Abrir Docker Desktop", BTN_BLUE, BTN_DARK);
        JButton retry = styledButton("Tentar de novo", BTN_BLUE, BTN_DARK);
        openDocker.addActionListener(e -> openDockerDesktop());
        retry.addActionListener(e -> refreshStatusQuiet());
        banner.add(bannerMsg);
        banner.add(openDocker);
        banner.add(retry);

        JPanel north = new JPanel(new BorderLayout());
        north.setBackground(BG_TOP);
        north.add(top, BorderLayout.NORTH);
        north.add(banner, BorderLayout.SOUTH);

        output.setEditable(false);
        output.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        output.setBackground(new Color(6, 17, 34));
        output.setForeground(new Color(190, 220, 250));
        output.setCaretColor(TEXT_MAIN);
        JScrollPane scroll = new JScrollPane(output);
        scroll.getViewport().setBackground(new Color(6, 17, 34));
        JPanel activity = new JPanel(new BorderLayout(6, 6));
        activity.setBackground(BG_MAIN);
        activity.setBorder(BorderFactory.createEmptyBorder(0, 14, 10, 14));
        JPanel activityBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        activityBar.setBackground(BG_MAIN);
        JLabel activityTitle = new JLabel("Atividade");
        activityTitle.setForeground(TEXT_MAIN);
        activityTitle.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        JButton clear = smallButton("Limpar");
        clear.addActionListener(e -> output.setText(""));
        activityBar.add(activityTitle);
        activityBar.add(clear);
        activity.add(activityBar, BorderLayout.NORTH);
        activity.add(scroll, BorderLayout.CENTER);

        frame.add(north, BorderLayout.NORTH);
        frame.add(cards, BorderLayout.CENTER);
        frame.add(activity, BorderLayout.SOUTH);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        log("DevUp pronto. Clique em Ligar no banco que voce quer usar.");
        refreshStatusQuiet();
    }

    @Override
    public ConfigStore config() {
        return config;
    }

    @Override
    public void trackDockerDependent(JButton b) {
        dockerDependent.add(b);
    }

    @Override
    public boolean confirmReset(DbPreset db) {
        JDialog dlg = new JDialog(frame, "Apagar tudo de " + db.id + "?", true);
        dlg.setLayout(new BorderLayout(10, 10));
        JPanel body = new JPanel(new GridLayout(0, 1, 4, 4));
        body.setBackground(BG_PANEL);
        body.setBorder(BorderFactory.createEmptyBorder(16, 18, 6, 18));
        JLabel warn = new JLabel("!  Esta acao nao pode ser desfeita.");
        warn.setForeground(STATUS_WARN);
        warn.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 15));
        JLabel l1 = new JLabel("Container: " + db.container);
        JLabel l2 = new JLabel("Volume: " + db.volume + " (todos os dados serao apagados)");
        l1.setForeground(TEXT_MAIN);
        l2.setForeground(TEXT_MAIN);
        l1.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        l2.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        body.add(warn);
        body.add(l1);
        body.add(l2);
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        actions.setBackground(BG_PANEL);
        JButton cancel = styledButton("Cancelar", BTN_BLUE, BTN_DARK);
        JButton confirm = styledButton("Apagar mesmo assim", BTN_RED, BTN_RED_DARK);
        boolean[] result = {false};
        cancel.addActionListener(e -> dlg.dispose());
        confirm.addActionListener(e -> { result[0] = true; dlg.dispose(); });
        actions.add(cancel);
        actions.add(confirm);
        dlg.add(body, BorderLayout.CENTER);
        dlg.add(actions, BorderLayout.SOUTH);
        dlg.getRootPane().setDefaultButton(cancel);
        dlg.pack();
        dlg.setMinimumSize(new Dimension(460, 220));
        dlg.setLocationRelativeTo(frame);
        dlg.setVisible(true);
        return result[0];
    }

    private void openDockerDesktop() {
        for (String path : DOCKER_PATHS) {
            if (new File(path).exists()) {
                try {
                    new ProcessBuilder("cmd", "/c", "start", "\"\"", "\"" + path + "\"").start();
                    log("Abrindo Docker Desktop... aguarde o icone ficar verde.");
                    waitForDocker();
                    return;
                } catch (Exception ex) {
                    log("ERRO ao abrir Docker Desktop: " + ex.getMessage());
                    return;
                }
            }
        }
        log("ERRO: Docker Desktop nao encontrado. Instale em https://www.docker.com/products/docker-desktop/");
    }

    private void waitForDocker() {
        setActionsEnabled(false);
        new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() {
                long deadline = System.currentTimeMillis() + 120000;
                while (System.currentTimeMillis() < deadline) {
                    if (DockerRunner.available()) return true;
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        return false;
                    }
                }
                return DockerRunner.available();
            }

            @Override
            protected void done() {
                try {
                    log(get() ? "Docker Desktop pronto." : "Docker Desktop ainda nao respondeu. Clique em Tentar de novo.");
                } catch (Exception ex) {
                    log("ERRO: " + ex.getMessage());
                }
                refreshStatusQuiet();
            }
        }.execute();
    }

    private void refreshStatusQuiet() {
        if (refreshing) return;
        refreshing = true;
        new SwingWorker<Void, Void>() {
            private boolean dockerOk;
            private final EnumMap<DbPreset, String> states = new EnumMap<>(DbPreset.class);

            @Override
            protected Void doInBackground() {
                dockerOk = DockerRunner.available();
                if (dockerOk) {
                    DockerManager mgr = new DockerManager(config, msg -> {});
                    for (DbPreset db : DbPreset.values()) states.put(db, mgr.describe(db));
                }
                return null;
            }

            @Override
            protected void done() {
                refreshing = false;
                try {
                    get();
                    paintDocker(dockerOk);
                    for (DbPreset db : DbPreset.values()) {
                        if (dockerOk) {
                            paintLabel(db, statusOf.get(db), infoOf.get(db), states.get(db));
                        } else {
                            paintOffline(statusOf.get(db), infoOf.get(db));
                        }
                    }
                    setActionsEnabled(dockerOk);
                } catch (Exception ex) {
                    log("ERRO ao atualizar status: " + ex.getMessage());
                }
            }
        }.execute();
    }

    private void paintDocker(boolean ok) {
        dockerDot.setText((ok ? "●" : "○") + " DOCKER " + (ok ? "ON" : "OFF"));
        dockerDot.setForeground(ok ? DOT_ON : DOT_OFF);
        banner.setVisible(!ok);
        frame.revalidate();
    }

    private void paintOffline(JLabel status, JLabel info) {
        status.setText("DOCKER OFF");
        styleStatusPill(status, STATUS_OFF, STATUS_BG_OFF);
        info.setText("<html><font color='#96BEE6'>aguardando Docker Desktop...</font></html>");
    }

    private void paintLabel(DbPreset db, JLabel status, JLabel info, String state) {
        int port = Integer.parseInt(config.get(db.id + ".port", String.valueOf(db.defaultPort)));
        status.setText(state.toUpperCase());
        if (state.equals("pronto") || state.equals("healthy") || state.equals("rodando")) {
            styleStatusPill(status, STATUS_OK, STATUS_BG_OK);
        } else if (state.equals("iniciando") || state.equals("starting")) {
            styleStatusPill(status, STATUS_WARN, STATUS_BG_WARN);
        } else {
            styleStatusPill(status, STATUS_OFF, STATUS_BG_OFF);
        }
        info.setText("<html><font color='#96BEE6'>" + db.jdbcUrl("localhost", port)
                + "<br>usuario: " + DbPreset.FIXED_USER
                + " | senha: " + DbPreset.FIXED_PASS
                + " | banco: " + DbPreset.FIXED_DB
                + " | porta: " + port + "</font></html>");
    }

    private void styleStatusPill(JLabel label, Color fg, Color bg) {
        label.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 19));
        label.setForeground(fg);
        label.setOpaque(true);
        label.setBackground(bg);
        label.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(fg.darker(), 1, true),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)));
    }

    @Override
    public void runTask(String startMsg, java.util.function.Consumer<DockerManager> task, boolean refreshAfter) {
        setEnabledAll(false);
        if (startMsg != null) log(startMsg);
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() {
                DockerManager mgr = new DockerManager(config, msg -> SwingUtilities.invokeLater(() -> log(msg)));
                try {
                    task.accept(mgr);
                } catch (Exception ex) {
                    SwingUtilities.invokeLater(() -> log("ERRO: " + ex.getMessage()));
                }
                return null;
            }

            @Override
            protected void done() {
                setEnabledAll(true);
                if (refreshAfter) refreshStatusQuiet();
            }
        }.execute();
    }

    private void setActionsEnabled(boolean enabled) {
        for (JButton b : dockerDependent) b.setEnabled(enabled);
    }

    private void setEnabledAll(boolean enabled) {
        if (frame != null) {
            for (java.awt.Component c : frame.getContentPane().getComponents()) {
                c.setEnabled(enabled);
            }
        }
    }

    @Override
    public void log(String msg) {
        output.append(msg + "\n");
        output.setCaretPosition(output.getDocument().getLength());
    }
}
