package helpers;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * Classe utilitaire pour les nombres
 */
public class NumberUtils {

	public static BigInteger toBigInteger(Object val) {
		if (val instanceof BigInteger) {
			return (BigInteger) val;
		} else if (val instanceof BigDecimal) {
			return ((BigDecimal) val).toBigInteger();
		}
		return null;
	}
	
}
