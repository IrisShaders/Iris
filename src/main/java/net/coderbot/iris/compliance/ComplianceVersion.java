package net.coderbot.iris.compliance;

import net.coderbot.iris.Iris;

public enum ComplianceVersion {
	NO_COMPLIANCE,
	v1;

	public int getInternalComplianceLevel() {
		switch (this) {
			case NO_COMPLIANCE -> {
				return 0;
			}
			case v1 -> {
				return 1;
            }
			default -> throw new IllegalStateException("Impossible, compliance is not existing? " + this.name());
		}
	}

	public static ComplianceVersion getComplianceLevel(String compliance) {
		try {
			int complianceL = Integer.parseInt(compliance);
			return ComplianceVersion.valueOf("v" + complianceL);
		} catch (IllegalArgumentException e) {
			Iris.logger.warn("Unknown compliance: " + compliance + "; defaulting to NONCOMPLIANT.");
			return NO_COMPLIANCE;
		}
	}
}
