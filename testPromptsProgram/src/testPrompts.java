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

    List<String> randTrials;

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

            writer.close();
            getPrompts();


            JFrame guiFrame = new JFrame();
            guiFrame.setLayout(new GridLayout(3,1));

            guiFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            guiFrame.setTitle("Jot speed test prompts");
            guiFrame.setSize(1000, 400);

            guiFrame.setLocationRelativeTo(null);


            JLabel headerLabel = new JLabel("<html><h2>Find this Entry: </h2></html>",JLabel.CENTER );
            headerLabel.setBorder(new EmptyBorder(30, 0, 0, 0));


            JLabel promptArea = new JLabel();

            promptArea.setBorder(new EmptyBorder(0, 50, 95, 50));

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
                    //When the fruit of veg button is pressed
                    // the setVisible value of the listPanel
                    // and //comboPanel is switched from true to
                    // value or vice versa.
                    current = count;
                    count++;

                    if (current >= TRIALS_PER_FEATURE * trialNames.length) {
                        promptArea.setText("");
                        JOptionPane.showMessageDialog(null, "All done.\n Thank You!");
                    } else {

                        prompt p = getRandomPrompt();
                        doneList.add(p);
                        promptArea.setText("<html><h3>"+count + ".) " + p.body+"</h3></html");
                        writer.println("Prompt: " + p.number + " at " + df.format(new Date()));
                    }
                    if (current % TRIALS_PER_FEATURE == 0 && current < TRIALS_PER_FEATURE * trialNames.length) {
                        JOptionPane.showMessageDialog(null, "Next Test: " + randTrials.get(current / TRIALS_PER_FEATURE));
                    }

                }
            });

            backBut.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (current>0) {
                        prompt p = doneList.get(--current);
                        promptArea.setText(current+1 + ".) " + p.body);
                    }



                }
            });
            forwardBut.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (current<doneList.size()-1) {
                        prompt p = doneList.get(++current);
                        promptArea.setText(current+1 + ".) " + p.body);
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
                    fullList.add(new prompt(line, countPrompts));
                    ++countPrompts;
                }
            }
        }catch (Exception e){
            System.out.println("FILE MESS: "+ e);
        }
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
