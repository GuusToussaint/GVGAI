package tracks.singlePlayer.advanced.customAgent;

import core.game.StateObservation;
import ontology.Types;
import tools.ElapsedCpuTimer;

import java.util.Random;

/**
 * Created with IntelliJ IDEA.
 * User: Thijs
 * Date: 17/03/21
 * Time: 19:13
 */
public class SingleCustomPlayer
{


    /**
     * Root of the tree.
     */
    public SingleTreeNode m_root;

    /**
     * Random generator.
     */
    public Random m_rnd;

    public int num_actions;
    public Types.ACTIONS[] actions;

    public SingleCustomPlayer(Random a_rnd, int num_actions, Types.ACTIONS[] actions)
    {
        this.num_actions = num_actions;
        this.actions = actions;
        m_rnd = a_rnd;
    }

    /**
     * Inits the tree with the new observation state in the root.
     * @param a_gameState current state of the game.
     */
    public void init(StateObservation a_gameState)
    {
        //m_root = new SingleTreeNode(m_rnd, num_actions, actions);
        //m_root.rootState = a_gameState;
        //Set the game observation to a newly root node.
        if (m_root == null)
        {
            m_root = new SingleTreeNode(m_rnd, num_actions, actions);
        }
        m_root.rootState = a_gameState;
    }

    /**
     * Runs MCTS to decide the action to take. It does not reset the tree.
     * @param elapsedTimer Timer when the action returned is due.
     * @return the action to execute in the game.
     */
    public int run(ElapsedCpuTimer elapsedTimer)
    {
        //Do the search within the available time.
        m_root.mctsSearch(elapsedTimer);

        //Determine the best action to take and return it.
        int action = m_root.mostVisitedAction();
        m_root = m_root.children[action];
        m_root.parent = null;
        m_root.updateDepth();
        //int action = m_root.bestAction();
        return action;
    }

}
