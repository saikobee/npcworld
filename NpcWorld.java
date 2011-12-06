import java.util.ArrayList;
import java.util.Set;
import java.util.Collections;
/*
 * @author afisher
 */
public class NpcWorld implements World {
    private NpcPopulation population;

    private ArrayList<NpcIndividual> matingPoolMales;
    private ArrayList<NpcIndividual> matingPoolFemales;

    private int oldAge;

    private double mutationChance;
    private double crossoverChance;
    private double deathChance;

    // max capacities
    private int eatingCapacity;
    private int sleepingCapacity;
    private int matingCapacity;

    // current availability
    private int eatingAvailability;
    private int sleepingAvailability;
    private int matingAvailability;

    private int maxHunger;
    private int maxSleepiness;

    private int stepNumber;
    private int currentID;

    public NpcWorld() {
        population = new NpcPopulation();

        // initialize the population
        for (int i = 0; i < Settings.POPULATION_SIZE; i++) {
            population.add(new NpcIndividual(currentID));
            currentID++;
        }

        oldAge = Settings.OLD_AGE;

        stepNumber = 0;
        currentID  = 0;

        mutationChance  = Settings.MUTATION_CHANCE;
        crossoverChance = Settings.CROSSOVER_CHANCE;
        deathChance     = Settings.DEATH_CHANCE;

        eatingCapacity   = Settings.EATING_CAPACITY;
        sleepingCapacity = Settings.SLEEPING_CAPACITY;
        matingCapacity   = Settings.MATING_CAPACITY;

        eatingAvailability   = eatingCapacity;
        sleepingAvailability = sleepingCapacity;
        matingAvailability   = matingCapacity;

        maxHunger     = Settings.MAX_HUNGER;
        maxSleepiness = Settings.MAX_SLEEPINESS;
    }

    public void setMutationChance(double c) {
        mutationChance = c / 100;
    }

    public void setCrossoverChance(double c) {
        crossoverChance = c / 100;
    }

    public void setDeathChance(double c) {
        deathChance = c / 100;
    }

    // setters for capacities
    public void setEatingCapacity(int c) {
        eatingAvailability += c - eatingCapacity; 
        eatingCapacity = c;
    }

    public void setSleepingCapacity(int c) {
        sleepingAvailability += c - sleepingCapacity; 
        sleepingCapacity = c;
    }

    public void setMatingCapacity(int c) {
        matingAvailability += c - matingCapacity; 
        matingCapacity = c;
    }

    // genetic operators
    public Dna crossover(Dna d1, Dna d2) {
        boolean[] n1;
        boolean[] n2;

        // 50/50 chance of which DNA to start out with
        if (Math.random() < 0.5) {
            n1 = d1.getNucleotides();
            n2 = d2.getNucleotides();
        } else {
            n1 = d2.getNucleotides();
            n2 = d1.getNucleotides();
        }

        boolean[] newDNA = new boolean[n1.length];

        boolean crossover = true;

        for (int i = 0; i < newDNA.length; i++) {
            if (Math.random() < crossoverChance) {
                crossover = !crossover;
            }

            if (crossover) {
                newDNA[i] = n1[i];
            } else {
                newDNA[i] = n2[i];
            }
        }

        return new NpcDna(newDNA);
    }

    public Dna mutate(Dna d) {
        boolean[] nucleotides = d.getNucleotides();
        for (int i = 0; i < nucleotides.length; i++) {
            if (Math.random() < mutationChance) {
                nucleotides[i] = !nucleotides[i];
            }
        }
        return new NpcDna(nucleotides);
    }

    public Population merge(Population p1, Population p2) {
        System.out.println("Merging two populations");
        return null;
    }

    public void step() {
        System.out.println("Step number " + stepNumber);
        stepNumber++;

        ArrayList<Integer> keys = new ArrayList<Integer>(population.getKeys());
        Collections.shuffle(keys);

        eatingAvailability = eatingCapacity;
        sleepingAvailability = sleepingCapacity;
        matingAvailability = matingCapacity;

        // go through all of the individuals and decrement availabilities
        // if the individual is not done with its action
        for (Integer k : keys) {
            NpcIndividual curIndividual = (NpcIndividual)population.get(k);

            if (curIndividual.getStepsRemaining() != 0) {
                if (curIndividual.getCurrentAction() == Const.EATING) {
                    eatingAvailability--;
                } else if (curIndividual.getCurrentAction() == Const.SLEEPING) {
                    sleepingAvailability--;
                } else if (curIndividual.getCurrentAction() == Const.MATING) {
                    matingAvailability--;
                }
            }
        }

        matingPoolMales = new ArrayList<NpcIndividual>();
        matingPoolFemales = new ArrayList<NpcIndividual>();

        for (Integer k : keys) {
            NpcIndividual curIndividual = (NpcIndividual)population.get(k);

            // make the individual choose an action and act on it
            // based on the current state of the population
            if (curIndividual.getStepsRemaining() == 0) {
                ArrayList<Integer> actions = new ArrayList<Integer>();
                if (eatingAvailability > 0) {
                    actions.add(Const.EATING);
                }
                if (sleepingAvailability > 0) {
                    actions.add(Const.SLEEPING);
                }
                if (matingAvailability > 0) {
                    actions.add(Const.MATING);
                }

                int action = curIndividual.chooseAction(actions);

                //TODO update the individual's icon
                if (action == Const.EATING) {
                    eatingAvailability--;
                } else if (action == Const.SLEEPING) {
                    sleepingAvailability--;
                } else if (action == Const.MATING) {
                    matingAvailability--;
                    if (((NpcDna)curIndividual.getDna()).getGender() == Const.MALE) {
                        matingPoolMales.add(curIndividual);
                    } else {
                        matingPoolFemales.add(curIndividual);
                    }
                }
            }
            curIndividual.decreaseStepsRemaining();

            if (curIndividual.getCurrentAction() == Const.EATING) {
                curIndividual.decreaseHunger();
            } else if (curIndividual.getCurrentAction() == Const.SLEEPING) {
                curIndividual.decreaseSleepiness();
            }

            curIndividual.increaseHunger();
            curIndividual.increaseSleepiness();
            curIndividual.increaseAge();

            //TODO implement death chance
            if (curIndividual.getHunger() > maxHunger || curIndividual.getSleepiness() > maxSleepiness) {
                population.remove(curIndividual);
            }
        }

        reproduce(); // mate the individuals who chose to mate
    }

    public void reproduce() {
        for (int i = 0; i < matingPoolMales.size(); i++) {
            if (i < matingPoolMales.size() && i < matingPoolFemales.size()) {
                NpcIndividual male   = matingPoolMales.get(i);
                NpcIndividual female = matingPoolFemales.get(i);

                System.out.println("HARDCORE MATING ACTION");
                population.add(mate(male, female));

                male.mated();
                female.mated();
                currentID++;
            }
        }
    }

    public Population getPopulation() {
        return population;
    }

    public Individual mate(Individual i1, Individual i2) {
        Dna dna = mutate(crossover(i1.getDna(), i2.getDna()));
        return new NpcIndividual(currentID, (NpcDna)dna);
    }
}