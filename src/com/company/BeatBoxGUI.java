package com.company;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

public class BeatBoxGUI {
    private static int BIT_SIZE = 16;

    private String clientName;
    private MusicPlayer player = new MusicPlayer();
    private ChatClient chatClient;
    private JFrame frame;
    private JTextArea messageTextArea;
    private JList incomingList;
    private Vector<String> listVector = new Vector<>();
    private Map<String, Map<Integer, ArrayList<Integer>>> receivedMessagesAndTracks = new HashMap<>();
    private Map<Integer, ArrayList<JCheckBox>> checkboxes = new HashMap<>();
    private Map<Integer, ArrayList<Integer>> trackSelected;
    private Map<Integer, String> instruments = new HashMap<Integer, String>() {{
        put(35, "Bass Drum");
        put(42, "Closed Hi-Hat");
        put(46, "Open Hi-Hat");
        put(38, "Acoustic Snare");
        put(49, "Crash Cymbal");
        put(39, "Hand Clap");
        put(50, "High Tom");
        put(60, "Hi Bondo");
        put(70, "Maracas");
        put(72, "Whistle");
        put(64, "Low Conda");
        put(56, "Cowbell");
        put(58, "Vibraslap");
        put(47, "Low-mid Tom");
        put(67, "High Agogo");
        put(63, "Open Hi Conga");
    }};

    public void go() {
        this.chatClient = new ChatClient();
        enterClientName();
        this.sendClientName();
        Thread receiveThread = new Thread(new ReadMessageRunnable());
        receiveThread.start();
        this.buildGUI();
    }

    public void buildGUI() {
        this.frame = new JFrame();
        this.frame.setTitle(String.format("Cyber Beat Box for %s", this.clientName));
        this.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel panelEast = new JPanel();
        JPanel panelCenter = new JPanel();
        JPanel panelWest = new JPanel();

        //Set up the WEST panel
        panelWest.setLayout(new GridLayout(BIT_SIZE, 1));
        for (Integer key : this.instruments.keySet()) {
            panelWest.add(new JLabel(this.instruments.get(key)));
        }

        //Set up the CENTER panel
        panelCenter.setLayout(new GridLayout(BIT_SIZE, BIT_SIZE));

        for (Integer key : this.instruments.keySet()) {
            ArrayList<JCheckBox> instrumentCheckboxes = new ArrayList<>();
            for (int i = 0; i < BIT_SIZE; i++) {
                instrumentCheckboxes.add(new JCheckBox());
                instrumentCheckboxes.get(i).setSelected(false);
                panelCenter.add(instrumentCheckboxes.get(i));
            }
            this.checkboxes.put(key, instrumentCheckboxes);
        }

        //Set up the EAST panel
        //Set up buttons on the EAST panel
        panelEast.setLayout(new BoxLayout(panelEast, BoxLayout.Y_AXIS));
        JButton startButton = new JButton("Start");
        JButton stopButton = new JButton("Stop");
        JButton tempoUpButton = new JButton("Tempo Up");
        JButton tempoDownButton = new JButton("Tempo Down");
        JButton randomTrackButton = new JButton("Random Track");
        JButton sendButton = new JButton("Send");

        startButton.addActionListener(new StartButtonListener());
        stopButton.addActionListener(new StopButtonListener());
        tempoUpButton.addActionListener(new TempoUpButtonListener());
        tempoDownButton.addActionListener(new TempoDownButtonListener());
        randomTrackButton.addActionListener(new RandomTrackButtonListener());
        sendButton.addActionListener(new SendButtonListener());

        panelEast.add(startButton);
        panelEast.add(stopButton);
        panelEast.add(tempoUpButton);
        panelEast.add(tempoDownButton);
        panelEast.add(randomTrackButton);
        panelEast.add(sendButton);

        //Set up chat Text Areas on the EAST panel
        Font bigFont = new Font("sanserif", Font.PLAIN, 12);

        //Set up Incoming Messages area
        JLabel answerLabel = new JLabel("Main Beat Box Dialog:");
        incomingList = new JList();
        incomingList.addListSelectionListener(new IncomingListSelectionListener());
        incomingList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        incomingList.setVisibleRowCount(-1);
        incomingList.setLayoutOrientation(JList.VERTICAL);
        incomingList.setFont(bigFont);
        incomingList.setCellRenderer(new WrappingForListCellRender(170));
        JScrollPane incomingListScroller = new JScrollPane(incomingList);
        incomingListScroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        incomingListScroller.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        incomingList.setListData(listVector);


        //Set up Message Text Area
        JLabel messageLabel = new JLabel("Your Message:");
        this.messageTextArea = new JTextArea(2, 10);
        this.messageTextArea.setLineWrap(true);
        this.messageTextArea.setWrapStyleWord(true);
        this.messageTextArea.setFont(bigFont);
        this.messageTextArea.setEditable(true);
        this.messageTextArea.addKeyListener(new EnterButtonListener());

        JScrollPane scrollerForMessageText = new JScrollPane(messageTextArea);
        scrollerForMessageText.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        scrollerForMessageText.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        //Add chat Text Areas to the panels
        panelEast.add(answerLabel);
        panelEast.add(incomingListScroller);
        panelEast.add(messageLabel);
        panelEast.add(scrollerForMessageText);

        //Set up Menu bar
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        JMenuItem openMenuItem = new JMenuItem("Open");
        JMenuItem saveMenuItem = new JMenuItem("Save");
        openMenuItem.addActionListener(new RestoreItButtonListener());
        saveMenuItem.addActionListener(new SerializeItButtonListener());
        fileMenu.add(openMenuItem);
        fileMenu.add(saveMenuItem);
        menuBar.add(fileMenu);
        this.frame.setJMenuBar(menuBar);

        //Add panels to the frame
        this.frame.getContentPane().add(BorderLayout.WEST, panelWest);
        this.frame.getContentPane().add(BorderLayout.CENTER, panelCenter);
        this.frame.getContentPane().add(BorderLayout.EAST, panelEast);

        this.frame.setSize(600, 600);
        this.frame.setVisible(true);
    }

    public void defineTrackSelected() {
        this.trackSelected = new HashMap<>();
        for (Integer key : this.checkboxes.keySet()) {
            int tickNumber = 0;
            ArrayList<Integer> ticks = new ArrayList<>();
            for (JCheckBox checkBox : this.checkboxes.get(key)) {
                if (checkBox.isSelected()) {
                    ticks.add(tickNumber);
                }
                tickNumber++;
            }
            trackSelected.put(key, ticks);
        }
    }

    public void selectCheckBoxesForTrackSelected() {
        clearCheckBoxes();
        for (Integer key : this.trackSelected.keySet()) {
            for (int tick : trackSelected.get(key)) {
                this.checkboxes.get(key).get(tick).setSelected(true);
            }
        }
    }

    private void clearCheckBoxes() {
        for (Integer key : this.checkboxes.keySet()) {
            for (JCheckBox checkBox : this.checkboxes.get(key)) {
                checkBox.setSelected(false);
            }
        }
    }

    private void enableCheckBoxes() {
        for (Integer key : this.checkboxes.keySet()) {
            for (JCheckBox checkBox : this.checkboxes.get(key)) {
                checkBox.setEnabled(true);
            }
        }
    }

    private void disableCheckBoxes() {
        for (Integer key : this.checkboxes.keySet()) {
            for (JCheckBox checkBox : this.checkboxes.get(key)) {
                checkBox.setEnabled(false);
            }
        }
    }

    private void saveObjectToFile(File file, Object object) {
        try {
            FileOutputStream fileStream = new FileOutputStream(file);
            ObjectOutputStream os = new ObjectOutputStream(fileStream);
            os.writeObject(object);
            os.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Object restoreObjectFromFile(File file) {
        Object object = null;
        try {
            FileInputStream fileStream = new FileInputStream(file);
            ObjectInputStream is = new ObjectInputStream(fileStream);
            object = is.readObject();
            is.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return object;
    }

    private void sendClientName() {
        chatClient.sendMessage(this.clientName);
    }

    private void sendMessage() {
        incomingList.clearSelection();
        defineTrackSelected();
        chatClient.sendMessage(messageTextArea.getText(), this.trackSelected);
        messageTextArea.setText("");
        messageTextArea.requestFocusInWindow();
    }

    private void saveTrackToFile() {
        defineTrackSelected();
        JFileChooser fileSave = new JFileChooser();
        fileSave.showSaveDialog(frame);
        saveObjectToFile(fileSave.getSelectedFile(), trackSelected);
    }

    private void runSaveTheTrackDialog() {
        JDialog.setDefaultLookAndFeelDecorated(true);
        int response = JOptionPane.showConfirmDialog(null, "Do you want to save the track, before loading the track from the list", "Save the track",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (response == JOptionPane.NO_OPTION) {
        } else if (response == JOptionPane.YES_OPTION) {
            saveTrackToFile();
        } else if (response == JOptionPane.CLOSED_OPTION) {
        }

    }

    private void enterClientName() {
        this.clientName = JOptionPane.showInputDialog(frame, "Enter your name:");
        if (this.clientName == null) {
            this.enterClientName();
        }
    }

    public void randomTrackGeneration(double variety) {
        this.trackSelected = new HashMap<>();
        for (int instrument : this.instruments.keySet()) {
            if (Math.random() < variety) {
                ArrayList<Integer> ticks = new ArrayList<>();
                for (int i = 0; i < 16; i++) {
                    if (Math.random() < variety / 2) {
                        ticks.add(i);
                    }
                }
                this.trackSelected.put(instrument, ticks);
            }
        }
        this.selectCheckBoxesForTrackSelected();

    }

    private class ReadMessageRunnable implements Runnable {
        @Override
        public void run() {
            while (true) {
                ArrayList result = chatClient.readMessage();
                String textMessage = (String) result.get(0);
                Map<Integer, ArrayList<Integer>> trackMessage = (Map<Integer, ArrayList<Integer>>) result.get(1);
                System.out.println(String.format("Incoming to client: %s %s", textMessage, trackMessage.getClass()));
                receivedMessagesAndTracks.put(textMessage, trackMessage);
                listVector.add(textMessage);
                incomingList.setListData(listVector);
            }
        }
    }

    private class StartButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            player.setUpPlayer();
            defineTrackSelected();
            disableCheckBoxes();
            player.createTrack(trackSelected);
            player.startPlayer();
        }
    }

    private class StopButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            player.stopPlayer();
            enableCheckBoxes();
        }
    }

    private class TempoUpButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            player.tempUp();
        }
    }

    private class TempoDownButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            player.tempDown();
        }
    }

    private class SerializeItButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            saveTrackToFile();
        }
    }

    private class RestoreItButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            JFileChooser fileOpen = new JFileChooser();
            fileOpen.showOpenDialog(frame);
            trackSelected = (Map<Integer, ArrayList<Integer>>) restoreObjectFromFile(fileOpen.getSelectedFile());
            selectCheckBoxesForTrackSelected();
        }
    }

    private class RandomTrackButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            runSaveTheTrackDialog();
            randomTrackGeneration(Math.random());
        }
    }

    private class IncomingListSelectionListener implements ListSelectionListener {
        @Override
        public void valueChanged(ListSelectionEvent e) {
            if (!(e.getValueIsAdjusting())) {
                String selectedText = (String) incomingList.getSelectedValue();
                if (selectedText != null) {
                    runSaveTheTrackDialog();
                    trackSelected = receivedMessagesAndTracks.get(selectedText);
                    selectCheckBoxesForTrackSelected();
                }
            }
        }
    }

    private class SendButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            sendMessage();
        }
    }

    private class EnterButtonListener implements KeyListener {

        @Override
        public void keyPressed(KeyEvent e) {
            if (e.getKeyChar() == KeyEvent.VK_ENTER) {
                e.consume();
                sendMessage();
            }
        }

        @Override
        public void keyReleased(KeyEvent e) {

        }

        @Override
        public void keyTyped(KeyEvent e) {

        }
    }
}
