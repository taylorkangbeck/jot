/**
 * Created by tuckerkirven on 11/5/15.
 */
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

public class testPrompts {
    int TRIALS_PER_FEATURE=4;

    String[] trialNames = {"Grey List", "Colored List", "Grey Graph", "Colored Graph"};
    Integer[] trainEntries= {18, 28, 47, 24};
    Integer[] testEntries = {5,8,16,22,35,40,43,52, 3, 11, 14, 19, 31, 36, 46, 49};
    List<String> randTrials;
    int trainCount =0;
    boolean first = true;

    List<prompt> fullList = new ArrayList<prompt>();
    int count = 0;
    List<prompt> doneList = new ArrayList<>();
    int current = -1;
    public static void main(String[] args){

        new testPrompts();
    }
    public testPrompts(){


        Date d  = new Date();
        SimpleDateFormat df = new SimpleDateFormat("dd-mm-yyyy hh:mm:ss");

        try {
            final PrintWriter writer = new PrintWriter("src/test--"+df.format(d)+".txt", "UTF-8");

            long seed = System.nanoTime();
            randTrials = Arrays.asList(trialNames);
            Collections.shuffle(randTrials, new Random(seed));

            getPrompts();


            JFrame guiFrame = new JFrame();
            guiFrame.setLayout(new GridLayout(3,1));

            guiFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            guiFrame.setTitle("Jot speed trial prompter");
            guiFrame.setSize(1000, 400);

            guiFrame.setLocationRelativeTo(null);


            JLabel headerLabel = new JLabel("<html><h2>Find this Entry: </h2></html>",JLabel.CENTER );
            headerLabel.setBorder(new EmptyBorder(30, 0, 0, 0));


            JLabel promptArea = new JLabel();

            promptArea.setBorder(new EmptyBorder(0, 20, 0, 20));

            JButton nextPromptBut = new JButton( "Next New Prompt");
            JButton backBut = new JButton( "<-- Back");
            JButton forwardBut = new JButton( "Forward -->");


            JPanel buttonFrame = new JPanel(new FlowLayout(FlowLayout.CENTER,200,10));


            buttonFrame.add(backBut);
            buttonFrame.add(nextPromptBut);
            buttonFrame.add(forwardBut);


            guiFrame.add(headerLabel);
            guiFrame.add(promptArea, BorderLayout.CENTER);
            guiFrame.add(buttonFrame);

            nextPromptBut.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent event) {

                    if (trainCount < trainEntries.length) {
                        prompt p = getTrainPrompt();
                        doneList.add(p);
                        promptArea.setText("<html><h3>" + "Training: " + p.body + "</h3></html");
                        writer.println("Prompt: " + p.number + " at " + df.format(new Date()));
                        ++trainCount;
                    } else {

                        current = count;
                        count++;

                        if (current >= TRIALS_PER_FEATURE * trialNames.length) {
                            promptArea.setText("");
                            JOptionPane.showMessageDialog(null, "All done.\n Thank You!");
                        } else {

                            prompt p = getRandomPrompt();
                            doneList.add(p);
                            promptArea.setText("<html><h3>" + count + ".) " + p.body + "</h3></html");
                            writer.println("Prompt: " + p.number + " at " + df.format(new Date()));
                        }
                        if (current % TRIALS_PER_FEATURE == 0 && current < TRIALS_PER_FEATURE * trialNames.length) {
                            String formPrompt = "\nFill out survey for previous trial!";
                            if (first) {
                                formPrompt = "";
                                first=false;
                            }
                            JOptionPane.showMessageDialog(null, "Next Trial: " + randTrials.get(current / TRIALS_PER_FEATURE) + formPrompt);
                        }
                    }

                }
            });

            backBut.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (current>0 && trainCount >=trainEntries.length) {
                        prompt p = doneList.get(--current+ trainEntries.length);
                        promptArea.setText("<html><h3>"+(current+1) + ".) " + p.body+"</h3></html");
                    }
                }
            });
            forwardBut.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (current<doneList.size()-trainEntries.length-1 && trainCount >=trainEntries.length) {
                        prompt p = doneList.get(++current+trainEntries.length);
                        promptArea.setText("<html><h3>"+(current+1)  + ".) " + p.body+"</h3></html");
                    }
                }
            });
            guiFrame.setVisible(true);
        }
        catch (Exception e){
            System.out.println(e);
        }

    }
    private void getPrompts(){
        File r = new File("/");
        try {
            r = new File("src/prompts.txt");
        }catch (Exception e){

        }
        System.out.println(r.getAbsolutePath());

        try (BufferedReader br = new BufferedReader(new FileReader(r))) {
            String line;

            int countPrompts = 0;
            while ((line = br.readLine()) != null) {
                if(line.length() > 1){
                    if(Arrays.asList(testEntries).contains(countPrompts) || Arrays.asList(trainEntries).contains(countPrompts))
                        fullList.add(new prompt(line, countPrompts));

                    ++countPrompts;
                }
            }
        }catch (Exception e){
            System.out.println("FILE MESS: "+ e);
        }
    }
    private prompt getTrainPrompt(){

        for(prompt p: fullList){

            if(Arrays.asList(trainEntries).contains(p.number)) {

                fullList.remove(p);
                return p;
            }
        }

        return null;
    }
    private prompt getRandomPrompt() {
        Random rand = new Random();
        prompt p1 = new prompt("No Entries Remaining", -1);
        if (!fullList.isEmpty()) {
            int index = rand.nextInt(fullList.size());
            p1 = fullList.get(index);
            fullList.remove(index);
        }
        return p1;
    }
    class prompt{
        public String body;
        public int number;

        prompt(String body , int number){
            this.body = body;
            this.number = number;
        }
    }
}
