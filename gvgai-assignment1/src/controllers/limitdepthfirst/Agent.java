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

	Vector2d goalpos;
	Vector2d keypos;

	// ACTIONS cutoff, failure;

	/**
	 * Available actions.
	 */
	ArrayList<ACTIONS> avlActions;

	/**
	 * The actions that are actually executed.
	 */
	private ArrayList<ACTIONS> exeActions;

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
		exeActions = new ArrayList<>();
		soList = new ArrayList<>();

		// cutoff = failure = null;
	}

	private ACTIONS recursiveDLS(StateObservation stateObs, int limit, int depth) {
		// boolean cutoff_occurred = false;
		// if (stateObs.getAvatarPosition().equals(goalpos))
		// return node;

		if (depth == limit)
			return null;

		soList.add(stateObs);
		ACTIONS result = null;

		for (ACTIONS action : avlActions) {

			// Path has been found. No need to search.
			if (dpsFlag == false)
				break;

			StateObservation stCopy = stateObs.copy();
			stCopy.advance(action);

			if (stCopy.isGameOver())
				dpsFlag = false;
			else {
				boolean been = false;
				for (StateObservation so : soList) {
					if (so.equalPosition(stCopy)) {
						been = true;
						break;
					}
				}
				if (been == false)
					recursiveDLS(stCopy, limit, depth + 1);
			}

			if (dpsFlag == false)
				result = action;
		}

		soList.remove(stateObs);

		return result;
	}

	private ACTIONS dls(StateObservation stateObs, int limit) {
		return recursiveDLS(stateObs, limit, 0);
	}

	@Override
	public ACTIONS act(StateObservation stateObs, ElapsedCpuTimer elapsedTimer) {
		// TODO Auto-generated method stub

		if (dpsFlag == true) {
			dls(stateObs, 4);
		} else
			return exeActions.remove(0);

		return null;
	}

}
