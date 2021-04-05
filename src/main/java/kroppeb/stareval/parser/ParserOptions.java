package kroppeb.stareval.parser;

import it.unimi.dsi.fastutil.chars.Char2ObjectMap;
import it.unimi.dsi.fastutil.chars.Char2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import java.util.Map;

/**
 * --
 */
public class ParserOptions {
	public ParserOptions(Char2ObjectMap<OpResolver<UnaryOp>> unaryOpResolvers, Char2ObjectMap<OpResolver<BinaryOp>> binaryOpResolvers) {
		this.unaryOpResolvers = unaryOpResolvers;
		this.binaryOpResolvers = binaryOpResolvers;
	}
	
	final Char2ObjectMap<OpResolver<UnaryOp>> unaryOpResolvers;
	final Char2ObjectMap<OpResolver<BinaryOp>> binaryOpResolvers;
	
	
	public static class Builder {
		final Char2ObjectMap<Map<String, UnaryOp>> unaryOpResolvers = new Char2ObjectOpenHashMap<>();
		final Char2ObjectMap<Map<String, BinaryOp>> binaryOpResolvers = new Char2ObjectOpenHashMap<>();
		
		public void addUnaryOp(String s, UnaryOp op) {
			Map<String, UnaryOp> mp = this.unaryOpResolvers
					.computeIfAbsent(s.charAt(0), (c) -> new Object2ObjectOpenHashMap<>());
			UnaryOp previous = mp.put(s.substring(1), op);
			assert previous == null;
		}
		
		public void addBinaryOp(String s, BinaryOp op) {
			Map<String, BinaryOp> mp = this.binaryOpResolvers
					.computeIfAbsent(s.charAt(0), (c) -> new Object2ObjectOpenHashMap<>());
			BinaryOp previous = mp.put(s.substring(1), op);
			assert previous == null;
		}
		
		private static <T extends Op> Char2ObjectMap<OpResolver<T>> convertOp(Char2ObjectMap<Map<String, T>> ops) {
			Char2ObjectMap<OpResolver<T>> result = new Char2ObjectOpenHashMap<>();
			
			ops.char2ObjectEntrySet().forEach(entry -> {
						Map<String, T> map = entry.getValue();
						if (map.size() > 2) {
							throw new RuntimeException("Not supported atm");
						}
						if (map.containsKey("")) {
							if (map.size() == 1) {
								result.put(entry.getCharKey(), new SingleCharOpResolver<>(map.get("")));
							} else {
								for (Map.Entry<String, T> subEntry : map.entrySet()) {
									if (!subEntry.getKey().equals("")) {
										if (subEntry.getKey().length() != 1)
											throw new RuntimeException("Not supported atm");
										result.put(entry.getCharKey(), new SingleDualCharOpResolver<T>(
												map.get(""),
												subEntry.getValue(),
												subEntry.getKey().charAt(0)
										));
									}
								}
							}
						} else {
							for (Map.Entry<String, T> subEntry : map.entrySet()) {
								if (subEntry.getKey().length() != 1)
									throw new RuntimeException("Not supported atm");
								result.put(entry.getCharKey(), new SingleDualCharOpResolver<T>(
										map.get(""),
										subEntry.getValue(),
										subEntry.getKey().charAt(0)
								));
							}
						}
					}
			
			);
			return result;
		}
		
		public ParserOptions build() {
			return new ParserOptions(
					convertOp(this.unaryOpResolvers),
					convertOp(this.binaryOpResolvers)
			);
		}
	}
}

