package MCMBP.Authors.Mining;

//https://stackoverflow.com/questions/6271731/whats-the-best-way-to-return-a-pair-of-values-in-java
public class Pair<T, U> {
	public final T First;
	public final U Second;

	public Pair(T first, U second) {
		this.First = first;
		this.Second = second;
	}

	@Override
	public String toString() {
		return "Pair [t=" + First + ", u=" + Second + "]";
	}

}
