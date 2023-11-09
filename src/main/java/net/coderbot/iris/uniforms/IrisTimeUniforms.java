package net.coderbot.iris.uniforms;

import net.coderbot.iris.Iris;
import net.coderbot.iris.gl.uniform.UniformHolder;
import net.coderbot.iris.gl.uniform.UniformUpdateFrequency;
import org.joml.Vector2i;
import org.joml.Vector3i;

import java.time.LocalDateTime;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalField;
import java.util.GregorianCalendar;

public class IrisTimeUniforms {
	private static LocalDateTime dateTime;

	public static void updateTime() {
		dateTime = LocalDateTime.now();
	}

	public static void addTimeUniforms(UniformHolder uniforms) {
		Vector3i date = new Vector3i();
		Vector3i time = new Vector3i();
		Vector2i yearTime = new Vector2i();
		uniforms.uniform3i(UniformUpdateFrequency.PER_TICK, "currentDate", () -> date.set(dateTime.getYear(), dateTime.getMonthValue(), dateTime.getDayOfMonth()));
		uniforms.uniform3i(UniformUpdateFrequency.PER_TICK, "currentTime", () -> time.set(dateTime.getHour(), dateTime.getMinute(), dateTime.getSecond()));
		uniforms.uniform2i(UniformUpdateFrequency.PER_TICK, "currentYearTime", () -> yearTime.set(
			((dateTime.getDayOfYear() - 1) * 86400) + (dateTime.getHour() * 3600) + (dateTime.getMinute() * 60) + dateTime.getSecond(),
			(dateTime.toLocalDate().lengthOfYear() * 86400) - (((dateTime.getDayOfYear() - 1) * 86400) + (dateTime.getHour() * 3600) + (dateTime.getMinute() * 60) + dateTime.getSecond())
		));
	}

	static {
	}
}
