package net.irisshaders.iris.helpers;

import net.irisshaders.iris.uniforms.CapturedRenderingState;

public class EntityState {
	private static int backupValue;
	private static boolean hasBackup;

	public static void interposeItemId(int newValue) {
		if (hasBackup) return;

		backupValue = CapturedRenderingState.INSTANCE.getCurrentRenderedItem();
		hasBackup = true;
		CapturedRenderingState.INSTANCE.setCurrentRenderedItem(newValue);
	}

	public static void restoreItemId() {
		if (hasBackup) {
			hasBackup = false;
			CapturedRenderingState.INSTANCE.setCurrentRenderedItem(backupValue);
		}
	}
}
