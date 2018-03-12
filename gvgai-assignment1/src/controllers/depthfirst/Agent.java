package controllers.depthfirst;

import java.util.ArrayList;

import core.game.Observation;
import core.game.StateObservation;
import core.player.AbstractPlayer;
import ontology.Types.ACTIONS;
import tools.ElapsedCpuTimer;

/**
 * @author 昕
 * @param <Observation>
 *
 */
public class Agent extends AbstractPlayer {

	/**
	 * Available actions.
	 */
	private ArrayList<ACTIONS> avlActions;

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
		exeActions = new ArrayList<>();
		soList = new ArrayList<>();

		avlActions = so.getAvailableActions();

		dps(so);
		// for (ACTIONS action : exeActions)
		// System.out.println(action);
	}

	/**
	 * Depth first search.
	 * 
	 * @param stateObs
	 *            state observation of the current game.
	 */
	private void dps(StateObservation stateObs) {
		soList.add(stateObs);

		for (ACTIONS action : avlActions) {

			// Path has been found. No need to search.
			if (dpsFlag == false)
				break;

			// Play the game. Advance and record the actions.
			StateObservation stCopy = stateObs.copy();
			stCopy.advance(action);
			exeActions.add(action);

			// Judge whether to continue search.
			if (stCopy.isGameOver())
				dpsFlag = false;
			else {
				boolean been = false; // Indicates whether a loop occurs.
				for (StateObservation so : soList) {
					if (so.equalPosition(stCopy)) {
						// if (so.getAvatarPosition().equals(stCopy.getAvatarPosition())) {
						been = true;
						break;
					}
				}
				if (been == false)
					dps(stCopy);
			}

			if (dpsFlag == false)
				break;
			exeActions.remove(exeActions.size() - 1); // backward
		}

		soList.remove(stateObs);
	}

	@Override
	public ACTIONS act(StateObservation stateObs, ElapsedCpuTimer elapsedTimer) {
		// TODO Auto-generated method stub
		// ArrayList<Observation>[] npcPositions = stateObs.getNPCPositions();
		// ArrayList<Observation>[] fixedPositions = stateObs.getImmovablePositions();
		// ArrayList<Observation>[] movingPositions = stateObs.getMovablePositions();
		// // ArrayList<Observation>[] resourcesPositions =
		// stateObs.getResourcesPositions();
		// // ArrayList<Observation>[] portalPositions = stateObs.getPortalsPositions();
		//
		// // printDebug(npcPositions,"npc");
		// printDebug(fixedPositions,"fix");
		// printDebug(movingPositions,"mov");
		// // printDebug(resourcesPositions,"res");
		// // printDebug(portalPositions,"por");
		// // 输出: fix:2(21,1,); mov:2(1,2,);
		// // fix 有2个, 地面长度为21, 洞长度为1;
		// // mov 有2个, 钥匙长度为1, 箱子长度为2;

		// System.out.println("x:" + stateObs.getAvatarPosition().x + ", y:" +
		// stateObs.getAvatarPosition().y);

		// Reproducing the process.
		return exeActions.remove(0);
	}

	private void printDebug(ArrayList<Observation>[] positions, String str) {
		if (positions != null) {
			System.out.print(str + ":" + positions.length + "(");
			for (int i = 0; i < positions.length; i++) {
				System.out.print(positions[i].size() + ",");
			}
			System.out.print("); ");
		} else
			System.out.print(str + ": 0; ");
	}

}
