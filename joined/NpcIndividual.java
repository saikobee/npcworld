import java.util.ArrayList;
import java.awt.*;
import javax.swing.JComponent;

/*
 * @author tgriswol
 */
public class NpcIndividual implements Individual {
    private NpcDna dna;
    private int ID, age, currentAction, stepsRemaining, hunger, sleepiness;
    private Icon icon;

    public NpcIndividual(int ID){
        this.ID = ID;
        dna = new NpcDna();
        icon = new Icon(dna.getGender());
        icon.colorize(dna.getColor());
        stepsRemaining = 0;
    }

    public NpcIndividual(int ID, NpcDna dna) {
        this.ID = ID;
        this.dna = dna;
        icon = new Icon(dna.getGender());
        icon.colorize(dna.getColor());
        stepsRemaining = 0;
    }

    public Dna getDna(){
        return dna;
    }
    public JComponent getWidget() {
        return icon;
    }
    public int getAge(){
        return age;
    }
    public int getID(){
        return ID;
    }
    public int getCurrentAction(){
        return currentAction;
    }
    public int getStepsRemaining(){
        return stepsRemaining;
    }
    public int getHunger(){
        return hunger;
    }
    public int getSleepiness(){
        return sleepiness;
    }

    public void increaseAge(){
        age++;
        icon.happyBirthday();
    }
    public void decreaseStepsRemaining(){
        stepsRemaining--;
    }
    public void increaseHunger(){
        hunger++;
    }
    public void decreaseHunger(int change){
        hunger -= change;
    }
    public void increaseSleepiness(){
        sleepiness++;
    }
    public void decreaseSleepiness(int change){
        sleepiness -= change;
    }
    public int chooseAction(ArrayList<Integer> availableActions){
        Debug.echo("Returning a chosen action (first one right now)");
        currentAction = availableActions.get(0);
        int stepsRemaining = 1;
        return currentAction;
    }
}
