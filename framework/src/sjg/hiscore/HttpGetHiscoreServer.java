package sjg.hiscore;

import sjg.SJGame;

import java.util.Vector;
import java.util.StringTokenizer;
import java.net.URL;
import java.net.URLConnection;
import java.net.HttpURLConnection;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;

/**
 * Date: Jan 2, 2007
 *
 * @author Christian Hvid
 */

public class HttpGetHiscoreServer implements HiscoreServer {
    private SJGame game;
    private String listAddress;
    private String addAddress;

    public HttpGetHiscoreServer(SJGame game, String listAddress, String addAddress) {
        this.game = game;
        this.listAddress = listAddress;
        this.addAddress = addAddress;
    }

    public Vector listEntries() {
        try {
            Vector entries = new Vector();
            URL url = new URL(game.getDocumentBase(), listAddress + "?game=" + game.getName());

            URLConnection connection;
            connection = url.openConnection();

            if (connection instanceof HttpURLConnection)
                ((HttpURLConnection) connection).setRequestMethod("GET");

            connection.setUseCaches(false);
            connection.connect();

            if (connection instanceof HttpURLConnection)
                if (((HttpURLConnection) connection).getResponseCode() != 200)
                    throw new RuntimeException("HTTP ERROR: " + ((HttpURLConnection) connection).getResponseCode());

            BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String s;
            while ((s = br.readLine()) != null) {
                if (s.length() > 2) {
                    StringTokenizer st = new StringTokenizer(s, ",");
                    String text = st.nextToken().replace('-', ' ');
                    String round = st.nextToken();
                    int score = Integer.parseInt(st.nextToken());
                    entries.addElement(new Hiscore.Entry(score, round, text));
                }
            }
            br.close();
            return entries;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void addEntry(Hiscore.Entry entry) {
        try {
            URL url = new URL(game.getDocumentBase(),
                    addAddress + "?game=" + game.getName() + "&text=" + entry.getText().replace(' ', '-') +
                            "&round=" + entry.getRound() + "&score=" + entry.getScore());

            URLConnection connection = url.openConnection();

            if (connection instanceof HttpURLConnection)
                ((HttpURLConnection) connection).setRequestMethod("GET");

            connection.connect();

            BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            while (br.ready()) {
                String s = br.readLine();
            }
            br.close();

            if (connection instanceof HttpURLConnection)
                ((HttpURLConnection) connection).disconnect();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
