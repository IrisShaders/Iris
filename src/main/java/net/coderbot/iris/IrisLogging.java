package net.coderbot.iris;

import com.mojang.logging.LogUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IrisLogging {
	public static boolean ENABLE_SPAM = false; // FabricLoader.getInstance().isDevelopmentEnvironment();

	private Logger logger;

	public IrisLogging(String loggerName) {
		this.logger = LoggerFactory.getLogger(loggerName);
	}

	public void fatal(String fatal) {
		this.logger.error(LogUtils.FATAL_MARKER, fatal);
	}

	public void error(String error) {
		this.logger.error(error);
	}

	public void error(String error, Object... o) {
		this.logger.error(error, o);
	}

	public void error(String error, Throwable t) {
		this.logger.error(error, t);
	}

	public void warn(String warning) {
		this.logger.warn(warning);
	}

	public void warn(String warning, Throwable t) {
		this.logger.warn(warning, t);
	}

	public void warn(Throwable o) {
		this.logger.warn("", o);
	}

	public void info(String info) {
		this.logger.warn(info);
	}

	public void info(String info, Object... o) {
		this.logger.warn(info, o);
	}

	public void debug(String debug) {
		this.logger.debug(debug);
	}
}
