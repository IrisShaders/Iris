package kroppeb.stareval;

import java.util.function.Consumer;

public class Util {
	public static <T> T make(T item, Consumer<T> init) {
		init.accept(item);
		return item;
	}

}


