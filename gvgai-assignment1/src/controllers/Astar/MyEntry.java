package controllers.Astar;

import core.game.StateObservation;

final class MyEntry implements Comparable<MyEntry> {
	private StateObservation key;
	private MyEntry comeFrom;
	private Double value;
	private int depth;

	public MyEntry(StateObservation key, MyEntry comeFrom, Double value, int depth) {
		this.key = key;
		this.comeFrom = comeFrom;
		this.value = value;
		this.depth = depth;
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
