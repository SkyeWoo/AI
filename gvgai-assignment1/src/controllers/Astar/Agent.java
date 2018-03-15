package controllers.Astar;

import java.util.ArrayList;
import java.util.PriorityQueue;

import core.game.StateObservation;
import core.player.AbstractPlayer;
import ontology.Types.ACTIONS;
import ontology.Types.WINNER;
import tools.ElapsedCpuTimer;

/**
 * @author ê¿
 *
 */

public class Agent extends AbstractPlayer {

	/**
	 * Available actions.
	 */
	private ArrayList<ACTIONS> avlActions;

	/**
	 * The action that are actually executed.
	 */
	private ArrayList<ACTIONS> exeActions;

	/**
	 * Record the states to avoid loops.
	 */
	private ArrayList<StateObservation> soList;

	/**
	 * Indicates whether to continue AstarSearch.
	 */
	private boolean continueFlag = true;

	/**
	 * A list of state's entries, information about every state's depth, last state,
	 * and heuristic value.
	 */
	PriorityQueue<MyEntry> frontier;

	/**
	 * Public constructor with state observation and time due.
	 * 
	 * @param so
	 *            state observation of the last game.
	 * @param elapsedTimer
	 *            Timer for the controller creation.
	 */
	public Agent(StateObservation so, ElapsedCpuTimer elapsedTimer) {
		avlActions = so.getAvailableActions();
		exeActions = new ArrayList<>();
		soList = new ArrayList<>();

		/**
		 * By testing the code below we get: wall - 0 goal - 7 avatar - 1 (without key)
		 * avatar - 4 (with key) box - 8 hole - 2 key - 6 mushroom - 5
		 */
		// ArrayList<Observation> grid[][];
		// grid = so.getObservationGrid();
		// for (int i = 0; i < grid[0].length; i++) {
		// for (int j = 0; j < grid.length; j++) {
		// if (grid[j][i].size() > 0)
		// System.out.print(grid[j][i].get(0).itype);
		// else
		// System.out.print(" ");
		// }
		// System.out.println();
		// }

		AStarSearch(so);
	}

	/**
	 * A* algorithm.
	 * 
	 * @param stateObs
	 *            state observation of the last game.
	 */
	public void AStarSearch(StateObservation stateObs) {

		frontier = new PriorityQueue<>();
		frontier.add(new MyEntry(stateObs, null)); // start point

		while (!frontier.isEmpty() && continueFlag) {

			MyEntry last = frontier.poll(); // Get current heuristic best state.
			soList.add(last.getKey()); // Mark as has visited.

			for (ACTIONS action : avlActions) {

				// Advance and update frontier.
				StateObservation current = last.getKey().copy();
				current.advance(action);

				boolean been = false;
				for (MyEntry history : frontier) {
					if (history.getKey().equalPosition(current)) {
						been = true;
						break;
					}
				}

				if (been == false && current.isGameOver() == false)
					frontier.add(new MyEntry(current, last));
				else {

					// Exit of algorithm.
					if (current.getGameWinner().equals(WINNER.PLAYER_WINS)) {
						continueFlag = false;
						exeActions.add(current.getAvatarLastAction());

						// Upback to record the actions on the path.
						MyEntry it = last;
						while (it != null) {
							exeActions.add(it.getKey().getAvatarLastAction());
							it = it.getComeFrom();
						}
					}
				}
			}
		}
	}

	@Override
	public ACTIONS act(StateObservation stateObs, ElapsedCpuTimer elapsedTimer) {
		return exeActions.remove(exeActions.size() - 1);
	}

}
