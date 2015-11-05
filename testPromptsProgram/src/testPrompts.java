/**
 * Created by tuckerkirven on 11/5/15.
 */
import javax.swing.*;
import javax.swing.text.DateFormatter;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

public class testPrompts {
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


            writer.close();
            getPrompts();

            JFrame guiFrame = new JFrame();
            guiFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            guiFrame.setTitle("Jot speed test prompts");
            guiFrame.setSize(1000, 600);

            guiFrame.setLocationRelativeTo(null);


            JLabel listLbl = new JLabel("");

            listLbl.setHorizontalAlignment(SwingConstants.CENTER);

            JButton nextPromptBut = new JButton( "Next Prompt");
            JButton backBut = new JButton( "<-- Back");
            JButton forwardBut = new JButton( "Forward -->");





            JPanel buttonFrame = new JPanel(new FlowLayout(FlowLayout.CENTER,200,10));
            buttonFrame.setSize(500, 200);

            buttonFrame.add(backBut);
            buttonFrame.add(nextPromptBut);
            buttonFrame.add(forwardBut);
            guiFrame.add(buttonFrame, BorderLayout.SOUTH);
            guiFrame.add(listLbl, BorderLayout.CENTER);



            nextPromptBut.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent event) {
                    //When the fruit of veg button is pressed
                    // the setVisible value of the listPanel
                    // and //comboPanel is switched from true to
                    // value or vice versa.
                    current = count;
                    count++;
                    prompt p = getRandomPrompt();
                    doneList.add(p);
                    listLbl.setText(count + ".) " + p.body);
                    writer.println("Prompt: " + p.number + " at " + df.format(new Date()));
                    System.out.println(count);

                }
            });

            backBut.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (current>0) {
                        prompt p = doneList.get(--current);
                        listLbl.setText(current+1 + ".) " + p.body);
                    }



                }
            });
            forwardBut.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (current<doneList.size()-1) {
                        prompt p = doneList.get(++current);
                        listLbl.setText(current+1 + ".) " + p.body);
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
