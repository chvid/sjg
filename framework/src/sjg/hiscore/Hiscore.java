package sjg.hiscore;

import sjg.*;

import java.util.*;
import java.awt.*;
import java.awt.event.*;

public abstract class Hiscore {
    public static final int SIZE = 10;

    private Vector entries = new Vector();
    private Date lastLoad;

    public static class Entry {
        private int score;
        private String round;
        private String text;

        public int getScore() {
            return score;
        }

        public String getRound() {
            return round;
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

        public Entry(int score, String round, String text) {
            this.score = score;
            this.round = round;
            this.text = text;
        }
    }

    private int addScoreToLocalTable(int score, String round, String text) {
        int p = 0;

        for (int i = 0; i < entries.size(); i++)
            if (score < ((Entry) entries.elementAt(i)).getScore()) p = i + 1;

        entries.insertElementAt(new Entry(score, round, text), p);

        if (entries.size() >= SIZE) entries.removeElementAt(entries.size() - 1);

        return p;
    }

    private int minimumScore() {
        if (entries.size() < SIZE) {
            return 0;
        } else {
            return ((Entry) entries.elementAt(entries.size() - 1)).getScore();
        }
    }

    public boolean registerScore(int score, String round) {
        if (score < minimumScore()) return false;
        else {
            position = addScoreToLocalTable(score, round, "");
            editing = true;
            return true;
        }
    }

    Thread loadThread;

    private void load() {
        loadThread = new Thread() {
            public void run() {
                try {
                    Vector entries = server.listEntries();

                    if (editing) lastLoad = null; // refresh as soon as possible
                    else Hiscore.this.entries = entries;

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };

        if ((lastLoad == null) || (new Date().getTime() > 60 * 1000 + lastLoad.getTime())) {
            lastLoad = new Date();
            loadThread.start();
        }
    }

    public abstract void drawHiscoreText(int position, String positionText, String entryText, int score, String round);

    public abstract void drawWriteHiscoreText(int position, String positionText, String entryText, int score, String round);

    public void drawHiscore(Graphics g) {
        load();
        g.setColor(Color.black);
        for (int i = 0; i < entries.size(); i++) {
            Entry entry = (Entry) entries.elementAt(i);
            String positionText;
            if (i == 0) positionText = "1ST";
            else if (i == 1) positionText = "2ND";
            else if (i == 2) positionText = "3RD";
            else positionText = (i + 1) + "TH";
            drawHiscoreText(i, positionText, entry.getText(), entry.getScore(), entry.getRound());
        }
    }

    int count = 0;
    int position = 0;

    public void drawHiscoreEditor(Graphics g) {
        for (int i = 0; i < entries.size(); i++) {
            Entry entry = (Entry) entries.elementAt(i);
            String positionText;
            if (i == 0) positionText = "1ST";
            else if (i == 1) positionText = "2ND";
            else if (i == 2) positionText = "3RD";
            else positionText = (i + 1) + "TH";

            if (position == i) {
                String s = entry.getText();
                if ((count / 8) % 2 == 0)
                    s += "_";

                drawWriteHiscoreText(i, positionText, s, entry.getScore(), entry.getRound());
            } else
                drawHiscoreText(i, positionText, entry.getText(), entry.getScore(), entry.getRound());
        }
    }

    public boolean moveHiscoreEditor() {
        count++;
        return editing;
    }

    private Thread submitThread;

    private void submitResults() {
        final Entry entry = (Entry) entries.elementAt(position);

        submitThread = new Thread() {
            public void run() {
                server.addEntry(entry);
            }
        };

        submitThread.start();
        editing = false;
    }

    private boolean editing = false;

    private HiscoreServer server;

    public Hiscore(SJGame game, HiscoreServer server) {
        this.server = server;

        game.addKeyListener(new KeyAdapter() {
            public void keyTyped(KeyEvent e) {
                if (editing) {
                    Entry entry = (Entry) entries.elementAt(position);
                    // entry.setText(entry.getText()+e.getKeyChar());

                    // backspace 8
                    // return 10

                    // A .. Z 65 .. 90
                    // a .. z 97 .. 122

                    count = 0;

                    char c = e.getKeyChar();

                    if (((c >= 65) && (c <= 90)) || ((c >= 97) && (c <= 122)) || (c == 32)) {
                        if (entry.getText().length() < 20)
                            entry.setText((entry.getText() + e.getKeyChar()).toUpperCase());
                    } else {
                        if ((c == 8) && (entry.getText().length() > 0))
                            entry.setText(entry.getText().substring(0, entry.getText().length() - 1));
                        if (c == 10) submitResults();
                    }
                }
            }
        });

        load();
    }
}
