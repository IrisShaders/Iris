package net.irisshaders.iris.helpers;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * An absurdly simple class for storing pairs of strings because Java lacks pair / tuple types.
 */
public record StringPair(String key, String value) {
}
