package controllers.Astar;

import java.util.ArrayList;

import core.game.Observation;
import core.game.StateObservation;
import ontology.Types.WINNER;
import tools.Vector2d;

final class MyEntry implements Comparable<MyEntry> {
	private StateObservation key;

	/**
	 * 需要一个链表来记录到达目标的过程中avatar的actions
	 */
	private MyEntry comeFrom;
	private Double value;
	private int depth;

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

		ArrayList<Observation>[] fixedPositions = so.getImmovablePositions();
		ArrayList<Observation>[] movingPositions = so.getMovablePositions();
		// Vector2d goalpos = fixedPositions[1].get(0).position;
		Vector2d goalpos = new Vector2d();
		Vector2d keypos;

		int loss = 0;
		for (ArrayList<Observation> obj : fixedPositions) {
			if (obj.size() > 0) {
				int itype = obj.get(0).itype;
				switch (itype) {
				case 2: // hole
					loss += 500 * obj.size();
					break;
				case 5: // mushroom
					loss += 100 * obj.size();
				case 6: // key
					goalpos = obj.get(0).position;
				default:
					break;
				}
			}
		}

		Vector2d avatarPosition = so.getAvatarPosition();
		boolean withKey = (so.getAvatarType() == 4);
		if (withKey == false) {
			keypos = movingPositions[0].get(0).position;
			// here 10 is a artificial factor that consider the loss avoiding boxes.
			return Manhattan(avatarPosition, keypos) + Manhattan(keypos, goalpos) + loss + 500;
		} else
			return Manhattan(avatarPosition, goalpos) + loss;
	}

	public MyEntry(StateObservation key, MyEntry comeFrom) {
		this.key = key;
		this.comeFrom = comeFrom;
		this.depth = 0;

		if (comeFrom != null) {
			this.depth = comeFrom.getDepth() + 1;
			this.value = depth * 50 + heuristic(key);
		}
	}

	public StateObservation getKey() {
		return key;
	}

	public Double getValue() {
		return value;
	}

	public int getDepth() {
		return depth;
	}

	public Double setValue(Double value) {
		Double old = this.value;
		this.value = value;
		return old;
	}

	public MyEntry getComeFrom() {
		return comeFrom;
	}

	@Override
	public int compareTo(MyEntry o) {
		return (int) (this.value - o.value);
	}
}
