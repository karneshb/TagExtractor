import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class TagExtractorFrame extends JFrame {
    JPanel mainPanel, topPanel, middlePanel, bottomPanel, topSubPanel;
    JButton btnAddBook, btnAddFilter, btnFilter, btnSave;
    JTextArea txtTagsFreq;
    JScrollPane scroll;
    JLabel lblFileRead, lblFileFilter;
    JTextField txtReadFile, txtFilterFile;

    Toolkit kit = Toolkit.getDefaultToolkit();
    Dimension screenSize = kit.getScreenSize();
    int screenHeight = screenSize.height;
    int screenWidth = screenSize.width;

    Map<String, Integer> book = new TreeMap<>();
    HashSet<String> stopWords = new HashSet<>();

    public TagExtractorFrame() {
        super("Tag Extractor");
        mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        createTopPanel();
        createMidPanel();
        createBtmPanel();
        add(mainPanel);

        setSize(screenWidth / 2, screenHeight / 2);
        setLocation(screenWidth / 4, screenHeight / 4);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    private void createTopPanel() {
        topPanel = new JPanel();
        topSubPanel = new JPanel();
        topSubPanel.setLayout(new GridLayout(2, 2));

        lblFileRead = new JLabel("Filtered Text From: ");
        lblFileFilter = new JLabel("Filtered Text With: ");

        txtFilterFile = new JTextField();
        txtReadFile = new JTextField();

        topSubPanel.add(lblFileRead);
        topSubPanel.add(txtReadFile);
        topSubPanel.add(lblFileFilter);
        topSubPanel.add(lblFileFilter);
        topPanel.add(topSubPanel);
        mainPanel.add(topPanel, BorderLayout.NORTH);
    }

    private void createMidPanel() {
        middlePanel = new JPanel();
        txtTagsFreq = new JTextArea(18,30);
        txtTagsFreq.setEditable(false);
        scroll = new JScrollPane(txtTagsFreq);
        middlePanel.add(scroll);

        mainPanel.add(middlePanel, BorderLayout.CENTER);
    }

    private void createBtmPanel() {
        bottomPanel = new JPanel();
        bottomPanel.setLayout(new GridLayout(2, 2));

        btnAddBook = new JButton("Add Book");
        btnAddFilter = new JButton("Add Filter");
        btnFilter = new JButton("Filter Text");
        btnSave = new JButton("Save");

        btnAddBook.addActionListener((ActionEvent ae) -> {addBook();});
        btnAddFilter.addActionListener((ActionEvent ae) -> {addFilter();});
        btnFilter.addActionListener((ActionEvent ae) -> {filterTxt();});
        btnSave.addActionListener((ActionEvent ae) -> {saveFiltered();});

        bottomPanel.add(btnAddBook);
        bottomPanel.add(btnAddFilter);
        bottomPanel.add(btnFilter);
        bottomPanel.add(btnSave);

        mainPanel.add(bottomPanel, BorderLayout.SOUTH);
    }

    private void addBook() {
        File selectedFile;
        JFileChooser fileChooser = new JFileChooser();
        try {
            if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                selectedFile = fileChooser.getSelectedFile();
                Scanner in = new Scanner(selectedFile);
                txtReadFile.setText(selectedFile.getName());
                while(in.hasNext()){
                    String word = cleanBook(in.next());
                    book.merge(word, 1, (oldValue, value) -> oldValue + value);
                }
                in.close();
            } else{
                JOptionPane.showMessageDialog(null, "Cancelled by User.  Exiting...");
            }
        } catch(FileNotFoundException ex){
            System.err.println("Error! File Not Found!");
        } catch(IOException ex){
            ex.printStackTrace();
        }
    }

    private void addFilter() {
        File selectedFile;
        JFileChooser fileChooser = new JFileChooser();
        try{
            File workingDirectory = new File(System.getProperty("user.dir"));
            fileChooser.setCurrentDirectory(workingDirectory);
            if(fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION){
                selectedFile = fileChooser.getSelectedFile();
                Scanner in = new Scanner(selectedFile);
                txtReadFile.setText(selectedFile.getName());
                while(in.hasNext()){
                    stopWords.add(in.next());
                }
                in.close();
            } else{
                JOptionPane.showMessageDialog(null, "Cancelled by User.  Exiting...");
            }
        } catch(FileNotFoundException ex){
            System.err.println("Error! File Not Found!");
        } catch(IOException ex){
            ex.printStackTrace();
        }
    }

    private void filterTxt() {
        boolean ready = true;
        if(book.size() < 1){
            ready = false;
        }
        if(stopWords.size() < 1){
            ready = false;
        }

        if(ready == true){
            book.keySet().removeAll(stopWords);
            book.keySet().remove("");
            for(String key : book.keySet()){
                txtTagsFreq.append(String.format("%-30s\t%d%n", key, book.get(key)));
            }
            } else{
            JOptionPane.showMessageDialog(this, "Please choose both a book and a filter.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void saveFiltered() {
        JFileChooser fileChooser = new JFileChooser();
        String outFileName = "";
        PrintWriter out;
        String defaultFileName = "default.txt";
        String confirm = "File Saved to: ";

        try{
            File workingDirectory = new File(System.getProperty("user.dir"));
            fileChooser.setCurrentDirectory(workingDirectory);
            outFileName = JOptionPane.showInputDialog(null, "Enter filename to write: ");
            if(outFileName.trim().equals(""))
            {
                outFileName = defaultFileName;
            }
            confirm += workingDirectory + "\\" + outFileName;
            out = new PrintWriter(outFileName);
            for (Map.Entry<String, Integer> entry : book.entrySet()) {
                out.println(entry.getKey() + ", " + entry.getValue());
                out.flush();
            }
            out.close();
        } catch(FileNotFoundException ex){
            System.err.println("Error! File Not Found!");
        } catch(NullPointerException ex){
            confirm = "File not saved!";
        } catch(IOException ex){
        ex.printStackTrace();
        }
    }

    private static String cleanBook(String data){
        String cleanData = "";
        for(int i = 0; i < data.length(); i++){
            char character = data.charAt(i);
            if(Character.isLetter(character)){
                cleanData = cleanData + character;
                cleanData = cleanData.trim();
            }
        }
        return cleanData.toLowerCase();
    }
}
