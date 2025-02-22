package tracks.singlePlayer.advanced.student;

import core.game.StateObservation;
import core.player.AbstractPlayer;
import ontology.Types;
import tools.ElapsedCpuTimer;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created with IntelliJ IDEA.
 * User: ssamot
 * Date: 14/11/13
 * Time: 21:45
 * This is an implementation of MCTS UCT
 */
public class Agent_ST_true_ED_5 extends AbstractPlayer {
    public int num_actions;
    public Types.ACTIONS[] actions;

    protected SingleCustomPlayer customPlayer;

    /**
     * Public constructor with state observation and time due.
     * @param so state observation of the current game.
     * @param elapsedTimer Timer for the controller creation.
     */
    public Agent_ST_true_ED_5(StateObservation so, ElapsedCpuTimer elapsedTimer)
    {
        //Get the actions in a static array.
        ArrayList<Types.ACTIONS> act = so.getAvailableActions();
        actions = new Types.ACTIONS[act.size()];
        for(int i = 0; i < actions.length; ++i)
        {
            actions[i] = act.get(i);
        }
        num_actions = actions.length;

        //Create the player.
        customPlayer = getPlayer(so, elapsedTimer);
    }

    public SingleCustomPlayer getPlayer(StateObservation so, ElapsedCpuTimer elapsedTimer) {
        return new SingleCustomPlayer(new Random(), num_actions, actions);
    }


    /**
     * Picks an action. This function is called every game step to request an
     * action from the player.
     * @param stateObs Observation of the current state.
     * @param elapsedTimer Timer when the action returned is due.
     * @return An action for the current state
     */
    public Types.ACTIONS act(StateObservation stateObs, ElapsedCpuTimer elapsedTimer) {

        //Set the state observation object as the new root of the tree.
        Boolean saveTree = true;
        int expansionDepth = 5;

        customPlayer.init(stateObs, saveTree, expansionDepth);

        //Determine the action using MCTS...
        int action = customPlayer.run(elapsedTimer);

        //... and return it.
        return actions[action];
    }

}
