package tracks.singlePlayer;

import java.util.Arrays;
import java.util.Random;

import core.logging.Logger;
import tools.Utils;
import tracks.ArcadeMachine;

/**
 * Created with IntelliJ IDEA. User: Diego Date: 04/10/13 Time: 16:29 This is a
 * Java port from Tom Schaul's VGDL - https://github.com/schaul/py-vgdl
 */
public class Test {

    public static void main(String[] args) {

		// Available tracks:
		String sampleRandomController = "tracks.singlePlayer.simple.sampleRandom.Agent";
		String doNothingController = "tracks.singlePlayer.simple.doNothing.Agent";
		String sampleOneStepController = "tracks.singlePlayer.simple.sampleonesteplookahead.Agent";
		String sampleFlatMCTSController = "tracks.singlePlayer.simple.greedyTreeSearch.Agent";

		String sampleMCTSController = "tracks.singlePlayer.advanced.sampleMCTS.Agent";
        String sampleRSController = "tracks.singlePlayer.advanced.sampleRS.Agent";
        String sampleRHEAController = "tracks.singlePlayer.advanced.sampleRHEA.Agent";
		String sampleOLETSController = "tracks.singlePlayer.advanced.olets.Agent";
		String customAgentController = "tracks.singlePlayer.advanced.student.Agent";

		//Load available games
		String spGamesCollection =  "src/tracks/singlePlayer/gameSelection.csv";
		String[][] games = Utils.readGames(spGamesCollection);

		// Run experiments
		int N = 1, L = 1, M = 100;
		boolean saveActions = false;
		String[] levels = new String[L];
		String[] actionFiles = new String[L*M];
		String[] agentsToCompete = new String[]{customAgentController, sampleMCTSController}; //sampleOLETSController
		String gameName = null;
		String game = null;

		for (String currentAgent : agentsToCompete) {
			System.out.println("running experiments for agent " + currentAgent);
			for (int i = 0; i < N; ++i) {
				int actionIdx = 0;
				game = games[i][0];
				gameName = games[i][1];
				for (int j = 0; j < L; ++j) {
					levels[j] = game.replace(gameName, gameName + "_lvl" + j);
					if (saveActions){
						for (int k = 0; k < M; ++k){
							System.out.println("raar");
							actionFiles[actionIdx++] = "actions_game_" + i + "_level_" + j + "_" + k + ".txt";
						}
					}
				}
				ArcadeMachine.runGames(game, levels, M, currentAgent, saveActions ? actionFiles : null);
			}
		}

    }
}
