package com.jgoetsch.eventtrader;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;

public enum TradeType {
	BUY(true, true, "BOUGHT"),
	SELL(false, false, "SOLD"),
	SHORT(false, true, "SHORTED", "RESHORTED"),
	COVER(true, false, "COVERED");

	private final boolean isBuy;
	private final boolean isEntry;
	private final Collection<String> identifiers;

	TradeType(boolean isBuy, boolean isEntry, String... alternates) {
		this.isBuy = isBuy;
		this.isEntry = isEntry;
		this.identifiers = new HashSet<String>(alternates.length + 1);
		this.identifiers.add(this.name());
		this.identifiers.addAll(Arrays.asList(alternates));
	}

	public static TradeType findByIdentifier(String identifier) {
		String id = identifier.trim().toUpperCase();
		for (TradeType type : values()) {
			if (type.identifiers.contains(id))
				return type;
		}
		return null;
	}

	public String getDisplayString() {
		return toString().substring(0, 1).toUpperCase() + toString().substring(1).toLowerCase();
	}

	public boolean isBuy() {
		return isBuy;
	}

	public boolean isSell() {
		return !isBuy;
	}

	public boolean isEntry() {
		return isEntry;
	}

	public boolean isExit() {
		return !isEntry;
	}
}
