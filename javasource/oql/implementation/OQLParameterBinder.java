package oql.implementation;

import com.mendix.core.Core;
import com.mendix.datastorage.OqlStatement;
import com.mendix.logging.ILogNode;
import com.mendix.systemwideinterfaces.core.IMendixIdentifier;
import com.mendix.systemwideinterfaces.core.IMendixObject;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collection;
import java.util.Map;

public final class OQLParameterBinder {

	private static final ILogNode logger = Core.getLogger(OQL.class.getSimpleName());

	private OQLParameterBinder() {
	}

	public static void bindAll(OqlStatement stmt, Map<String, ?> params) {
		if (params == null || params.isEmpty()) {
			logger.trace("No parameters to bind.");
			return;
		}

		for (Map.Entry<String, ?> e : params.entrySet()) {
			final String name = e.getKey();
			final Object value = e.getValue();

			// Nulls are ambiguous for overload resolution among reference types—fail fast.
			if (value == null) {
				throw new IllegalArgumentException("Parameter '" + name + "' is null; "
					+ "cannot infer correct OqlStatement#setVariable overload.");
			}

			if (params == null || params.isEmpty()) {
				return;
			}

			// Exact types first (to avoid accidental widening)
			if (value instanceof BigDecimal) {
				stmt.setVariable(name, (BigDecimal) value);
			} else if (value instanceof BigInteger) {
				stmt.setVariable(name, new BigDecimal((BigInteger) value));
			} else if (value instanceof Boolean) {
				stmt.setVariable(name, (Boolean) value);
			} else if (value instanceof Byte || value instanceof Short || value instanceof Integer) {
				// small integral types—bind as int
				stmt.setVariable(name, ((Number) value).intValue());
			} else if (value instanceof Long) {
				stmt.setVariable(name, (Long) value);
			} else if (value instanceof Float || value instanceof Double) {
				// bind as double; avoids BigDecimal(double) precision traps
				stmt.setVariable(name, ((Number) value).doubleValue());
			} else if (value instanceof CharSequence) {
				// Treat as string verbatim (no implicit numeric parsing)
				stmt.setVariable(name, value.toString());
			} else if (value instanceof IMendixIdentifier) {
				stmt.setVariable(name, (IMendixIdentifier) value);
			} else if (value instanceof IMendixObject) {
				stmt.setVariable(name, (IMendixObject) value);
			} else if (value instanceof Collection<?>) {
				stmt.setVariable(name, (Collection<?>) value);
			} else if (value instanceof Number) {
				// Fallback for uncommon Number subclasses
				final Number n = (Number) value;
				if (fitsInInt(n)) {
					stmt.setVariable(name, n.intValue());
				} else if (fitsInLong(n)) {
					stmt.setVariable(name, n.longValue());
				} else {
					// last resort: bind as double
					stmt.setVariable(name, n.doubleValue());
				}
			} else {
				throw new IllegalArgumentException("Unsupported parameter type for '" + name
					+ "': " + value.getClass().getName());
			}
		}
	}

	// Optional helpers to avoid overflowing when we decide int/long in the generic Number branch.
	private static boolean fitsInInt(Number n) {
		// Only meaningful when n is integral; for non-integral types this returns false
		if (n instanceof Byte || n instanceof Short || n instanceof Integer) return true;
		if (n instanceof Long) {
			long v = n.longValue();
			return v >= Integer.MIN_VALUE && v <= Integer.MAX_VALUE;
		}
		return false;
	}

	private static boolean fitsInLong(Number n) {
		// Long always fits in long; other Number subtypes generally do too for integral values
		return n instanceof Long;
	}
}
