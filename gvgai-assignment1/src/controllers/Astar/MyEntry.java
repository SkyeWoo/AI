package controllers.Astar;

import core.game.StateObservation;

final class MyEntry implements Comparable<MyEntry> {
	private StateObservation key;
	private Double value;

	public MyEntry(StateObservation key, Double value) {
		this.key = key;
		this.value = value;
	}

	public StateObservation getKey() {
		return key;
	}

	public Double getValue() {
		return value;
	}

	public Double setValue(Double value) {
		Double old = this.value;
		this.value = value;
		return old;
	}

	@Override
	public int compareTo(MyEntry o) {
		return (int) (this.value - o.value);
	}
}
