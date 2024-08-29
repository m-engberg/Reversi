import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.util.List;
import java.util.TreeMap;
import java.util.concurrent.ExecutionException;

public class ServerBrowser extends JFrame {
    private JButton connectButton, refreshButton;
    private JPanel mainPanel, buttonPanel;
    private JScrollPane scrollPane;
    private JList serverList;
    private TreeMap<String, String> serversMap = new TreeMap<>();
    protected Statement statement;
    private String url;

    //Server browser-window
    public ServerBrowser(){
        mainPanel = new JPanel(new BorderLayout());
        JLabel serverLabel = new JLabel("Games");
        mainPanel.add(BorderLayout.NORTH, serverLabel);

        serverList = new JList(serversMap.keySet().toArray());
        scrollPane = new JScrollPane();
        scrollPane.setViewportView(serverList);

        mainPanel.add(BorderLayout.CENTER, scrollPane);

        buttonPanel = new JPanel(new GridLayout(1,2));
        refreshButton = new JButton();
        refreshButton.setText("Refresh");
        refreshButton.addActionListener(new refreshListener());
        buttonPanel.add(refreshButton);
        connectButton = new JButton();
        connectButton.setText("Connect");
        connectButton.addActionListener(new connectServerListener());
        buttonPanel.add(connectButton);
        mainPanel.add(BorderLayout.SOUTH, buttonPanel);

        this.url = "jdbc:mysql://atlas.dsv.su.se/db_22886728?user=usr_22886728&password=886728";
        connectToServer();

        this.add(mainPanel);
        this.setTitle("Browse servers");
        this.setLocation(500,500);
        this.setSize(300,200);
        this.show();
    }

    //Sets up connection to MySQL server
    private void connectToServer() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver").newInstance();
            Connection connection = DriverManager.getConnection(this.url);
            this.statement = connection.createStatement();
            this.statement.executeUpdate("CREATE TABLE IF NOT EXISTS othelloServers (id INT NOT NULL PRIMARY KEY AUTO_INCREMENT, name TINYTEXT, ip TINYTEXT)");
            this.setTitle("CONNECTED TO MySQL ON: atlas.dsv.su.se");
            getServers();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("---> COULD NOT CONNECT OR CREATE TABLE!");
        }
    }

    //Retrieves all of the games that searches for players
    private void getServers() {
        refreshButton.setEnabled(false);
        SwingWorker<Boolean, String> worker = new SwingWorker<Boolean, String>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                //Quering info from MySQL
                String queryString = "SELECT * FROM othelloServers";
                ResultSet resultSet = statement.executeQuery(queryString);
                serverList.removeAll();
                serversMap = new TreeMap<>();

                //For every game in database, send to the worker
                while(resultSet.next()) {
                    String gamename = resultSet.getString("name");
                    String IP = resultSet.getString("ip");
                    publish(gamename + "/" + IP);
                }
                return true;
            }
            @Override
            protected void done(){
                boolean status;
                try{
                    status = get();
                    System.out.println("Servers fetched: " + status);
                    refreshButton.setEnabled(status);
                } catch (InterruptedException e) {
                    System.out.println("Thread interrupted");
                } catch (ExecutionException e) {
                    System.out.println("Exception in background");
                }
            }
            @Override
            protected void process(List<String> chunks){
                //For every row in database, add the name of the game and IP to TreeMap
                for(String chunk: chunks){
                    String[] block = chunk.split("/");
                    serversMap.put(block[0], block[1]);
                }
                //When all info is gathered, update the server list
                updateScrollPane();
            }
        };
        worker.execute();
    }

    //Updates the serverlist
    private void updateScrollPane() {
        mainPanel.remove(scrollPane);
        serverList = new JList(serversMap.keySet().toArray(new String[0]));
        scrollPane = new JScrollPane();
        scrollPane.setViewportView(serverList);
        mainPanel.add(BorderLayout.CENTER, scrollPane);
        this.revalidate();
        serverList.setSelectedIndex(0);
    }

    //Connect to selected game-handler
    private class connectServerListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            new Connector(serversMap.get(serverList.getSelectedValue().toString()));
        }
    }

    //Refresh-button handler, updates the game list
    private class refreshListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            getServers();
        }

    }

    //Class used by the creator of the game, sends info to database
    public static class ServerSender{
        protected Statement statement;
        private String url = "jdbc:mysql://atlas.dsv.su.se/db_22886728?user=usr_22886728&password=886728";

        //Publishes info about game on the database
        public ServerSender(String localHost, String nameOfGame){
            try{
                Class.forName("com.mysql.cj.jdbc.Driver").newInstance();
                Connection connection = DriverManager.getConnection(this.url);
                this.statement = connection.createStatement();
            } catch (SQLException | ClassNotFoundException throwables) {
                System.out.println("COULD NOT CONNECT TO SERVER");
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            }
            String str = "INSERT INTO othelloServers (name, ip) VALUES ('" + nameOfGame + "', '" + localHost + "')";
            sendToServer(str);
        }

        //Executes the querys, either publish or remove game from database
        protected void sendToServer(String str) {
            try{
                this.statement.executeUpdate(str);
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }
    }
}
