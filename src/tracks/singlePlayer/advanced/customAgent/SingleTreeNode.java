package tracks.singlePlayer.advanced.customAgent;

import core.game.StateObservation;
import ontology.Types;
import tools.ElapsedCpuTimer;
import tools.Utils;

import java.util.Random;

public class SingleTreeNode
{
    private final double HUGE_NEGATIVE = -10000000.0;
    private final double HUGE_POSITIVE =  10000000.0;
    public double epsilon = 1e-6;
    public SingleTreeNode parent;
    public SingleTreeNode[] children;
    public double totValue;
    public int nVisits;
    public Random m_rnd;
    public int m_depth;
    protected double[] bounds = new double[]{Double.MAX_VALUE, -Double.MAX_VALUE};

    public int num_actions;
    Types.ACTIONS[] actions;

    //MCTS Parameters
    public int ROLLOUT_DEPTH = 25; //How far do we look ahead in a simulation?
    public int EXPANSION_DEPTH = 15; //How deep may the tree be at best?
    public double K = Math.sqrt(2); //Exploration parameter, the larger it is, the more exploration we do

    public StateObservation rootState;

    public SingleTreeNode(Random rnd, int num_actions, Types.ACTIONS[] actions) {
        this(null, rnd, num_actions, actions);
    }

    public SingleTreeNode(SingleTreeNode parent, Random rnd, int num_actions, Types.ACTIONS[] actions) {
        this.parent = parent;
        this.m_rnd = rnd;
        this.num_actions = num_actions;
        this.actions = actions;
        children = new SingleTreeNode[num_actions];
        totValue = 0.0;

        if(parent != null)
            m_depth = parent.m_depth+1;
        else
            m_depth = 0;
    }

    //Searches for the best move while there is still time (I think based on game clock ticks)
    public void mctsSearch(ElapsedCpuTimer elapsedTimer) {
        double avgTimeTaken = 0;
        double acumTimeTaken = 0;
        long remaining = elapsedTimer.remainingTimeMillis();
        int numIters = 0;

        int remainingLimit = 5;
        //While there is still enough time
        while(remaining > 2 * avgTimeTaken && remaining > remainingLimit){
            StateObservation state = rootState.copy();

            ElapsedCpuTimer elapsedTimerIteration = new ElapsedCpuTimer();
            SingleTreeNode selected = treePolicy(state); //Find a node to expand and expand it
            double delta = selected.rollOut(state); //roll out the node
            backUp(selected, delta); //backpropagate

            numIters++;
            acumTimeTaken += (elapsedTimerIteration.elapsedMillis()) ;
            avgTimeTaken  = acumTimeTaken/numIters;
            remaining = elapsedTimer.remainingTimeMillis();
        }
    }

    public SingleTreeNode treePolicy(StateObservation state) {

        SingleTreeNode curNode = this;
        while (!state.isGameOver() && curNode.m_depth < EXPANSION_DEPTH)
        {
            if (curNode.notFullyExpanded()) {
                return curNode.expand(state);

            } else {
                SingleTreeNode next = curNode.uct(state);
                curNode = next;
            }
        }
        return curNode;
    }

    //Expands the tree
    public SingleTreeNode expand(StateObservation state) {
        int[] posChild = new int[children.length - (nVisits-1)];
        int options = 0;
        for (int i = 0; i < children.length; i++){
            if (children[i] == null) {
                posChild[options] = i;
                options += 1;
            }
        }
        int selection = posChild[m_rnd.nextInt(options)];

        //Roll the state
        state.advance(actions[selection]);

        SingleTreeNode tn = new SingleTreeNode(this, this.m_rnd, num_actions, actions);
        children[selection] = tn;
        return tn;
    }

    //Applies Upper Confidence Bound for Trees
    public SingleTreeNode uct(StateObservation state) {
        int selection = -1;
        double bestValue = -Double.MAX_VALUE;
        for (int i = 0; i < children.length; i++)
        {
            double hvVal = children[i].totValue;
            double childValue =  hvVal / children[i].nVisits;

            //"Number of wins" estimation
            childValue = Utils.normalise(childValue, bounds[0], bounds[1]);

            double uctValue = childValue +
                    K * Math.sqrt(Math.log(this.nVisits + 1) / (children[i].nVisits));

            // small sampleRandom numbers: break ties in unexpanded nodes
            if (uctValue > bestValue || (uctValue == bestValue && this.m_rnd.nextBoolean())) {
                selection = i;
                bestValue = uctValue;
            }
        }

        if (selection == -1)
        {
            throw new RuntimeException("Warning! returning null: " + bestValue + " : " + this.children.length + " " +
            + bounds[0] + " " + bounds[1]);
        }

        //Roll the state:
        state.advance(actions[selection]);

        return children[selection];
    }

    //Handles roll out of a newly expanded node
    public double rollOut(StateObservation state)
    {
        int thisDepth = this.m_depth;

        while (!finishRollout(state,thisDepth)) {
            int action = m_rnd.nextInt(num_actions);
            state.advance(actions[action]);
            thisDepth++;
        }


        double delta = value(state);

        if(delta < bounds[0])
            bounds[0] = delta;
        if(delta > bounds[1])
            bounds[1] = delta;

        //double normDelta = utils.normalise(delta ,lastBounds[0], lastBounds[1]);

        return delta;
    }

    public double value(StateObservation a_gameState) {
        if(a_gameState.isGameOver())
        {
            if (a_gameState.getGameWinner() == Types.WINNER.PLAYER_WINS)
            {
                return HUGE_POSITIVE;
            }
            else {
                return HUGE_NEGATIVE;
            }
        }
        return a_gameState.getGameScore();
    }

    public boolean finishRollout(StateObservation rollerState, int depth)
    {
        if(depth >= ROLLOUT_DEPTH)      //rollout end condition.
            return true;

        if(rollerState.isGameOver())               //end of game
            return true;

        return false;
    }

    //Handles backpropagation
    public void backUp(SingleTreeNode node, double result)
    {
        SingleTreeNode n = node;
        while(n != null)
        {
            n.nVisits++;
            n.totValue += result;
            if (result < n.bounds[0]) {
                n.bounds[0] = result;
            }
            if (result > n.bounds[1]) {
                n.bounds[1] = result;
            }
            n = n.parent;
        }
    }

    //Selects the most visited child of the root
    public int mostVisitedAction() {
        int selected = -1;
        double bestValue = -Double.MAX_VALUE;
        boolean allEqual = true;
        double first = -1;

        for (int i=0; i<children.length; i++) {

            if(children[i] != null)
            {
                if(first == -1)
                    first = children[i].nVisits;
                else if(first != children[i].nVisits)
                {
                    allEqual = false;
                }

                double childValue = children[i].nVisits;
                if (childValue > bestValue) {
                    bestValue = childValue;
                    selected = i;
                }
            }
        }

        if (selected == -1)
        {
            System.out.println("Unexpected selection!");
        }else if(allEqual)
        {
            //If all are equal, we opt to choose for the one with the best Q.
            selected = bestAction();
        }
        return selected;
    }

    //selects the highest valued child of the root (Only used when all children are equally visited)
    public int bestAction()
    {
        int selected = -1;
        double bestValue = -Double.MAX_VALUE;

        for (int i=0; i<children.length; i++) {

            if(children[i] != null) {
                //double tieBreaker = m_rnd.nextDouble() * epsilon;
                double childValue = children[i].totValue / (children[i].nVisits + this.epsilon);
                childValue = Utils.noise(childValue, this.epsilon, this.m_rnd.nextDouble());     //break ties randomly
                if (childValue > bestValue) {
                    bestValue = childValue;
                    selected = i;
                }
            }
        }

        if (selected == -1)
        {
            System.out.println("Unexpected selection!");
            selected = 0;
        }

        return selected;
    }


    public boolean notFullyExpanded() {
        for (SingleTreeNode tn : children) {
            if (tn == null) {
                return true;
            }
        }

        return false;
    }

    public void updateDepth(){
        if (this.parent == null && this.m_depth > 0)
        {
            int diff = this.m_depth;
            this.updateDepth(diff);
        }
    }

    //Updates each nodes depth
    public void updateDepth(int difference)
    {
        this.m_depth = this.m_depth - difference;
        for (int i = 0; i < this.children.length; i++)
        {
            if (this.children[i] != null)
            {
                this.children[i].updateDepth(difference+1);
            }
        }
    }
}
