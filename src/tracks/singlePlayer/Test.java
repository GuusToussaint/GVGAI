package tracks.singlePlayer;

import java.io.FileWriter;
import java.util.Arrays;
import java.util.Random;

import core.logging.Logger;
import tools.Utils;
import tools.com.google.gson.JsonArray;
import tools.com.google.gson.JsonObject;
import tracks.ArcadeMachine;

/**
 * Created with IntelliJ IDEA. User: Diego Date: 04/10/13 Time: 16:29 This is a
 * Java port from Tom Schaul's VGDL - https://github.com/schaul/py-vgdl
 */
public class Test {

    public static void main(String[] args) {

		// Available tracks:
		String sampleRandomController = "tracks.singlePlayer.simple.sampleRandom.Agent";
		String sampleMCTSController = "tracks.singlePlayer.advanced.sampleMCTS.Agent";

		//Load available games
		String spGamesCollection =  "src/tracks/singlePlayer/gameSelection.csv";
		String[][] games = Utils.readGames(spGamesCollection);

		// Run experiments
		if (args.length != 1){
			System.out.println("Incorrect number of parameters");
			System.exit(-1);
		}


		int gameIndex = Integer.parseInt(args[0]);
		int L = 1, M = 2;
		boolean saveActions = false;
		String[] levels = new String[L];
		String[] actionFiles = new String[L*M];
		String[] agentsToCompete = new String[10]; //sampleOLETSController
		int agentsAdded = 0;
		for (String saveTree : new String[]{"true", "false"}){
			for (String expansionDepth : new String[]{"5", "10", "15", "20"}){
				agentsToCompete[agentsAdded] = "tracks.singlePlayer.advanced.student.Agent_ST_" + saveTree + "_ED_" + expansionDepth;
				agentsAdded++;
			}
		}
		agentsToCompete[agentsAdded] = sampleRandomController;
		agentsToCompete[agentsAdded+1] = sampleMCTSController;

		String game = games[gameIndex][0];
		String gameName = games[gameIndex][1];
		JsonObject final_results = new JsonObject();

		for (String currentAgent : agentsToCompete) {
			JsonArray scores = new JsonArray();

			String level = game.replace(gameName, gameName + "_lvl" + 0);

			System.out.println("running experiments for agent " + currentAgent);
			System.out.println("In game " + gameName);

			for (int g = 0; g < M; ++g){
				double[] result = ArcadeMachine.runOneGame(game, level, false, currentAgent, null, new Random().nextInt(), 0);
				JsonObject result_object = new JsonObject();
				result_object.addProperty("result", result[0]);
				result_object.addProperty("score", result[1]);
				result_object.addProperty("timesteps", result[2]);
				scores.add(result_object);
			}
			final_results.add(currentAgent, scores);
		}

		try{
			FileWriter results_file = new FileWriter( gameName + "_results.json");
			results_file.write(final_results.toString());
			results_file.close();
		}catch(Exception e){
			System.out.println("an error occured when creating the results file");
			e.printStackTrace();
		}finally {
			System.out.println("Here are the results:");
			System.out.println(final_results.toString());
		}


    }
}
