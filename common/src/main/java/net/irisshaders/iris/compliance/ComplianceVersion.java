package net.irisshaders.iris.compliance;

import net.irisshaders.iris.Iris;

public enum ComplianceVersion {
	NO_COMPLIANCE,
	v1;

	public static ComplianceVersion getComplianceLevel(String compliance) {
		try {
			int complianceL = Integer.parseInt(compliance);
			return ComplianceVersion.valueOf("v" + complianceL);
		} catch (IllegalArgumentException e) {
			Iris.logger.warn("Unknown compliance: " + compliance + "; defaulting to NONCOMPLIANT.");
			return NO_COMPLIANCE;
		}
	}

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
}
