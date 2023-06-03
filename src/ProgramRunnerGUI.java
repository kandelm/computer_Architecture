import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;

public class ProgramRunnerGUI extends JFrame implements ActionListener {

    private JTextArea programArea;
    private JButton submitButton;
    private JButton runButton;

    public ProgramRunnerGUI() {
        super("Program Runner");

        programArea = new JTextArea(10, 20);
        JScrollPane scrollPane = new JScrollPane(programArea);
        submitButton = new JButton("Submit Instructions");
        submitButton.addActionListener(this);
        runButton = new JButton("Run Program");
        runButton.addActionListener(this);
        runButton.setEnabled(false);

        JPanel panel = new JPanel();
        panel.add(new JLabel("Enter program instructions:"));
        panel.add(scrollPane);
        panel.add(submitButton);
        panel.add(runButton);
        add(panel);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 300);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == submitButton) {
            String program = programArea.getText();

            try {
                FileWriter writer = new FileWriter("program.txt");
                writer.write(program);
                writer.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }

            runButton.setEnabled(true);
        } else if (e.getSource() == runButton) {
            try {
                Computer computer = new Computer();
                computer.dataMemory.populateWithRandomValues();
                computer.runProgram("program.txt");
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        new ProgramRunnerGUI();
    }
}