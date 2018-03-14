package controllers.limitdepthfirst;

import java.util.ArrayList;

import core.game.Observation;
import core.game.StateObservation;
import core.player.AbstractPlayer;
import ontology.Types.ACTIONS;
import tools.ElapsedCpuTimer;
import tools.Vector2d;

/**
 * @author ê¿
 *
 */
public class Agent extends AbstractPlayer {

	/**
	 * Goal position.
	 */
	private Vector2d goalpos;

	/**
	 * Key position.
	 */
	private Vector2d keypos;

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
		ArrayList<Observation>[] fixedPositions = so.getImmovablePositions();
		ArrayList<Observation>[] movingPositions = so.getMovablePositions();
		goalpos = fixedPositions[1].get(0).position;
		keypos = movingPositions[0].get(0).position;

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
	private double Manhattan(Vector2d v0, Vector2d v1) {
		return Math.abs(v0.x - v1.x) + Math.abs(v0.y - v1.y);
	}

	/**
	 * Heuristic function to judge the situation. In this program, we use Manhattan
	 * function.
	 * 
	 * @param avatarPosition
	 *            avatar's position
	 * @param withKey
	 *            indicates whether avatar has got the key
	 * @return heuristic value.
	 */
	private double heuristic(Vector2d avatarPosition, boolean withKey) {
		if (withKey == false)
			return Manhattan(avatarPosition, keypos) * 10 + Manhattan(keypos, goalpos);
		else
			return Manhattan(avatarPosition, goalpos);
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
		if (limit == depth) { // cutoff

			// Type equals 4 for having key, 1 for otherwise, which is test in
			// controllers.depthfirst
			boolean withKey = (stateObs.getAvatarType() == 4);
			double update = heuristic(stateObs.getAvatarPosition(), withKey);
			if (update < heuristicFactor) {
				heuristicFactor = update;
				exeAction = tempActions.get(0); // Necessarily, because tempActions will be empty after the function.
			}
			return;
		}

		soList.add(stateObs);

		for (ACTIONS action : avlActions) {
			// Path has been found. No need to search.
			if (dpsFlag == false)
				break;

			// Advance and record.
			StateObservation stCopy = stateObs.copy();
			stCopy.advance(action);
			tempActions.add(action);

			if (stCopy.isGameOver()) {
				dpsFlag = false;
				exeAction = tempActions.get(0); // Necessarily, because tempActions will be empty after the function.
			} else {
				boolean been = false;
				for (StateObservation so : soList) {
					if (so.equalPosition(stCopy)) {
						been = true;
						break;
					}
					// else
					// System.out.println(so.getAvatarPosition() + " " +
					// stCopy.getAvatarPosition());
				}
				if (been == false)
					dls(stCopy, limit, depth + 1);
			}

			if (dpsFlag == false)
				break;
			tempActions.remove(tempActions.size() - 1); // backward
		}

		soList.remove(stateObs);
	}

	@Override
	public ACTIONS act(StateObservation stateObs, ElapsedCpuTimer elapsedTimer) {
		if (dpsFlag == true) { // hasn't got a way to goal yet...
			heuristicFactor = Double.MAX_VALUE;

			// clear ArrayList to start a new dls
			tempActions.clear();
			soList.clear();
			dls(stateObs, 3, 0);

			return exeAction;
			// System.out.println(tempActions);
		} else // path has been found, act in accordance with the search path.
			return tempActions.remove(0);
	}

}
