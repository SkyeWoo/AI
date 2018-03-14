package controllers.Astar;

import java.util.ArrayList;
import java.util.PriorityQueue;

import core.game.StateObservation;
import core.player.AbstractPlayer;
import ontology.Types.ACTIONS;
import ontology.Types.WINNER;
import tools.ElapsedCpuTimer;

/**
 * @author 昕
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
	 * Goal position.
	 */
	// private Vector2d goalpos;

	/**
	 * Key position.
	 */
	// private Vector2d keypos;

	private boolean continueFlag = true;

	PriorityQueue<MyEntry> frontier;

	/**
	 * Public constructor with state observation and time due.
	 * 
	 * @param so
	 *            state observation of the current game.
	 * @param elapsedTimer
	 *            Timer for the controller creation.
	 */
	public Agent(StateObservation so, ElapsedCpuTimer elapsedTimer) {
		avlActions = so.getAvailableActions();
		exeActions = new ArrayList<>();
		soList = new ArrayList<>();

		/**
		 * 通过以下这段代码测试可以得到 wall - 0 goal - 7 avatar - 1 (without key) avatar - 4 (with
		 * key) box - 8 hole - 2 key - 6
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

	public void AStarSearch(StateObservation stateObs) {

		frontier = new PriorityQueue<>();
		frontier.add(new MyEntry(stateObs, null));

		while (!frontier.isEmpty() && continueFlag) {
			MyEntry current = frontier.poll();
			soList.add(current.getKey());

			for (ACTIONS action : avlActions) {
				StateObservation stCopy = current.getKey().copy();
				stCopy.advance(action);

				boolean been = false;
				for (MyEntry history : frontier) {
					if (history.getKey().equalPosition(stCopy)) {
						been = true;
						break;
					}
				}

				if (been == false && stCopy.isGameOver() == false) {
					// int depth = current.getDepth() + 1;
					// double value = depth * 50 + heuristic(stCopy);
					frontier.add(new MyEntry(stCopy, current));
				} else {
					if (stCopy.getGameWinner().equals(WINNER.PLAYER_WINS)) {
						continueFlag = false;
						exeActions.add(stCopy.getAvatarLastAction());

						MyEntry it = current;
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
