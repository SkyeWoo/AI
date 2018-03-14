package controllers.Astar;

import java.util.ArrayList;
import java.util.PriorityQueue;

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

	// private HashMap<Vector2d, Double> costSoFar;

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
	private Vector2d goalpos;

	/**
	 * Key position.
	 */
	private Vector2d keypos;

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

		ArrayList<Observation>[] fixedPositions = so.getImmovablePositions();
		ArrayList<Observation>[] movingPositions = so.getMovablePositions();
		goalpos = fixedPositions[1].get(0).position;
		keypos = movingPositions[0].get(0).position;

		// frontier = new PriorityQueue<>(new Comparator<MyEntry>() {
		// @Override
		// public int compare(MyEntry o1, MyEntry o2) {
		// return (int) (o1.getValue() - o2.getValue());
		// }
		// });

		frontier = new PriorityQueue<>();

		// cameFrome = new HashMap<>();
		// costSoFar = new HashMap<>();

		// cameFrome.put(so.getAvatarPosition(), so.getAvatarPosition());
		// costSoFar.put(so.getAvatarPosition(), 0.0);
		frontier.add(new MyEntry(so, 0.0));
		AStarSearch(so, 0);

		for (ACTIONS action : exeActions) {
			System.out.println(action.name());
		}
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
	private double heuristic(StateObservation so) {
		if (so.isGameOver()) {
			if (so.getGameWinner().equals(WINNER.PLAYER_WINS))
				return 0;
			else
				return Double.MAX_VALUE;
		}

		Vector2d avatarPosition = so.getAvatarPosition();
		boolean withKey = (so.getAvatarType() == 4);
		if (withKey == false)
			return Manhattan(avatarPosition, keypos) * 10 + Manhattan(keypos, goalpos);
		else
			return Manhattan(avatarPosition, goalpos);
	}

	public void AStarSearch(StateObservation stateObs, int depth) {
		soList.add(stateObs);

		double newCost = 50.0 * depth;

		Vector2d current = stateObs.getAvatarPosition();
		// newCost = costSoFar.get(current) + 50.0;
		// Set<Vector2d> keySet = costSoFar.keySet();
		// for (Vector2d key : keySet) {
		// if (key.equals(current)) {
		// newCost = costSoFar.get(key) + 50.0;
		// break;
		// }
		// }

		for (ACTIONS action : avlActions) {
			StateObservation stCopy = stateObs.copy();
			stCopy.advance(action);

			// for (Vector2d key : keySet) {
			// if (key.equals(stCopy.getAvatarPosition()) && costSoFar.get(key) > newCost) {
			// costSoFar.remove(key);
			// break;
			// }
			// }

			// costSoFar.put(stCopy.getAvatarPosition(), newCost);

			double priority = newCost + heuristic(stCopy);
			frontier.add(new MyEntry(stCopy, priority));
		}

		while (!frontier.isEmpty() && continueFlag) {
			StateObservation so = frontier.poll().getKey();

			if (so.getGameWinner().equals(WINNER.PLAYER_WINS)) {
				continueFlag = false;
				exeActions.add(so.getAvatarLastAction());
			} else {
				boolean been = false;
				for (StateObservation state : soList) {
					if (state.equalPosition(so)) {
						been = true;
						break;
					}
				}

				if (been == false) {

					System.out.println(so.getAvatarLastAction());
					AStarSearch(so, depth + 1);
				}
			}
		}

		if (continueFlag == false)
			exeActions.add(stateObs.getAvatarLastAction());

		soList.remove(stateObs);
	}

	@Override
	public ACTIONS act(StateObservation stateObs, ElapsedCpuTimer elapsedTimer) {
		return exeActions.remove(exeActions.size() - 1);
	}

}
