package controllers.limitdepthfirst;

import java.util.ArrayList;
import java.util.Random;

import core.game.Observation;
import core.game.StateObservation;
import core.player.AbstractPlayer;
import ontology.Types.ACTIONS;
import ontology.Types.WINNER;
import tools.ElapsedCpuTimer;
import tools.Vector2d;

/**
 * @author ê¿
 *
 */
public class Agent extends AbstractPlayer {

	/**
	 * Random generator for the agent.
	 */
	protected Random randomGenerator;

	/**
	 * Available actions.
	 */
	private ArrayList<ACTIONS> avlActions;

	/**
	 * Record the actions when doing limited depth first search.
	 */
	private ArrayList<ACTIONS> tempActions;

	/**
	 * The action that are actually executed.
	 */
	private ACTIONS exeAction;

	/**
	 * Record the states to avoid loops.
	 */
	private ArrayList<StateObservation> soList;

	/**
	 * Flag to decide whether to continue the search, true to continue, false to
	 * over.
	 */
	private boolean dpsFlag = true;

	/**
	 * Factor to indicate minimize heuristic value. Set to maximum double before
	 * doing the limited depth first search.
	 */
	private double heuristicFactor;

	/**
	 * Public constructor with state observation and time due.
	 * 
	 * @param so
	 *            state observation of the current game.
	 * @param elapsedTimer
	 *            Timer for the controller creation.
	 */
	public Agent(StateObservation so, ElapsedCpuTimer elapsedTimer) {
		randomGenerator = new Random();
		// ArrayList<Observation>[] fixedPositions = so.getImmovablePositions();
		// ArrayList<Observation>[] movingPositions = so.getMovablePositions();
		// goalpos = fixedPositions[1].get(0).position;
		// keypos = movingPositions[0].get(0).position;

		avlActions = so.getAvailableActions();
		tempActions = new ArrayList<>();
		soList = new ArrayList<>();

		// cutoff = failure = null;
	}

	/**
	 * Manhattan function suitable to calculate the distance in grid where sprites'
	 * move is limited to 4 directions.
	 * 
	 * @param v0
	 *            position 1
	 * @param v1
	 *            position 2
	 * @return Manhattan distance of 2 points in grid.
	 */
	static double Manhattan(Vector2d v0, Vector2d v1) {
		return Math.abs(v0.x - v1.x) + Math.abs(v0.y - v1.y);
	}

	/**
	 * Heuristic function to judge the situation. In this program, we use Manhattan
	 * function.
	 * 
	 * @param so
	 *            state observation of the current game.
	 * @param withKey
	 *            indicates whether avatar has got the key
	 * @return heuristic value.
	 */
	private double heuristic(StateObservation so, boolean withKey) {
		ArrayList<Observation>[] fixedPositions = so.getImmovablePositions();
		ArrayList<Observation>[] movingPositions = so.getMovablePositions();
		// error occurs if goalpos set as null
		Vector2d goalpos = new Vector2d();
		Vector2d keypos;

		// calculate the loss cause by immovable sprites
		for (ArrayList<Observation> obj : fixedPositions) {
			if (obj.size() > 0) {
				int itype = obj.get(0).itype;
				if (itype == 7)
					goalpos = obj.get(0).position;
			}
		}

		Vector2d avatarPosition = so.getAvatarPosition();
		if (withKey == false) {
			keypos = movingPositions[0].get(0).position;
			// here 1000 must be added, or OutOfMemoryError occurs
			return Manhattan(avatarPosition, keypos) + Manhattan(keypos, goalpos) + 500;
		} else
			return Manhattan(avatarPosition, goalpos);
	}

	/**
	 * Judge if the node has been reached.
	 * 
	 * @param so
	 *            state observation of the current game.
	 * @return true if the state has been reached, false otherwise.
	 */
	private boolean isVisited(StateObservation so) {
		for (StateObservation state : soList)
			if (state.equalPosition(so))
				return true;

		return false;
	}

	/**
	 * Limited depth first search.
	 * 
	 * @param stateObs
	 *            state observation of the current game.
	 * @param limit
	 *            limited depth. The program is invalid when limit is set to be 1, 2
	 *            or 9.
	 * @param depth
	 *            current searching depth.
	 */
	private void dls(StateObservation stateObs, int limit, int depth) {
		// Path has been found. No need to search.
		if (dpsFlag == false)
			return;
		if (stateObs.isGameOver()) {
			// win. take the steps as tempActions recorded.
			if (stateObs.getGameWinner().equals(WINNER.PLAYER_WINS)) {
				dpsFlag = false;
				exeAction = tempActions.remove(0);
			} else {
				// if is lose situation, then randomly choose a direction
				// that is different with the first step leading to lose.
				int index;
				StateObservation so;
				do {
					index = randomGenerator.nextInt(avlActions.size());
					exeAction = avlActions.get(index);
					so = stateObs.copy();
					so.advance(exeAction);
				} while (exeAction.equals(tempActions.get(0)) && isVisited(so) && so.isGameOver() == false);
			}
			return;
		}
		if (limit == depth) { // cutoff

			// Type equals 4 for having key, 1 for otherwise, which is test in
			// controllers.depthfirst.Agent.java
			boolean withKey = (stateObs.getAvatarType() == 4);
			double update = heuristic(stateObs, withKey);
			if (update < heuristicFactor) {
				heuristicFactor = update;
				exeAction = tempActions.get(0); // Necessarily, because tempActions will be empty after the function.
			}
			return;
		}

		soList.add(stateObs);

		for (ACTIONS action : avlActions) {
			if (dpsFlag == false)
				return;

			// Advance and record.
			StateObservation stCopy = stateObs.copy();
			stCopy.advance(action);
			tempActions.add(action);

			if (isVisited(stCopy) == false)
				dls(stCopy, limit, depth + 1);

			if (dpsFlag == true)
				tempActions.remove(tempActions.size() - 1); // backward
		}

		// soList.remove(stateObs);
	}

	@Override
	public ACTIONS act(StateObservation stateObs, ElapsedCpuTimer elapsedTimer) {
		if (dpsFlag == true) { // hasn't got a way to goal yet...
			heuristicFactor = Double.MAX_VALUE;

			// clear ArrayList to start a new dls
			tempActions.clear();
			soList.clear();
			dls(stateObs, 5, 0);

			return exeAction;
			// System.out.println(tempActions);
		} else // path has been found, act in accordance with the search path.
			return tempActions.remove(0);
	}

}
