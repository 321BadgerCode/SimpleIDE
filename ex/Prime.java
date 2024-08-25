import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class Prime {
	public static void main(String[] args) {
		// Declare the accuracy variables.
		long accuracyApprox = 0;
		long accuracyTheorem = 0;

		// Run the nth prime methods many times to get an average accuracy.
		int n = 4;
		int m = 1000;
		for (int i = n; i <= m; i++) {
			BigInteger primeSieve = nthPrimeSieve(i);
			System.out.println("The " + toOrdinal(i) + " prime number is " + primeSieve + ".");
	
			BigInteger primeApprox = nthPrimeApprox(i);
			BigInteger primeTheorem = nthPrimeTheorem(i);

			// Calculate the accuracy of the methods.
			long accuracyApproxTemp = Math.abs(primeSieve.subtract(primeApprox).longValue()) * 100 / primeSieve.longValue();
			long accuracyTheoremTemp = Math.abs(primeSieve.subtract(primeTheorem).longValue()) * 100 / primeSieve.longValue();

			// Add the accuracy to the total.
			accuracyApprox += accuracyApproxTemp;
			accuracyTheorem += accuracyTheoremTemp;
		}

		// Calculate the average accuracy.
		accuracyApprox /= m - n + 1;
		accuracyApprox = 100 - accuracyApprox;
		accuracyTheorem /= m - n + 1;
		accuracyTheorem = 100 - accuracyTheorem;

		// Output the average accuracy.
		System.out.println();
		System.out.println("The average accuracy of the nth prime number for the approximation method is " + accuracyApprox + "%.");
		System.out.println("The average accuracy of the nth prime number for the theorem method is " + accuracyTheorem + "%.");
	}

	public static BigInteger nthPrimeSieve(int n) {
		int upperBound = (int) Math.ceil(n * (Math.log(n) + Math.log(Math.log(n))));

		boolean[] prime = new boolean[upperBound + 1];
		for (int i = 0; i <= upperBound; i++) {
			prime[i] = true;
		}

		for (int p = 2; p * p <= upperBound; p++) {
			if (prime[p]) {
				for (int i = p * p; i <= upperBound; i += p) {
					prime[i] = false;
				}
			}
		}

		List<Integer> primes = new ArrayList<>();
		for (int p = 2; p <= upperBound; p++) {
			if (prime[p]) {
				primes.add(p);
			}
		}

		return BigInteger.valueOf(primes.get(n - 1));
	}

	// NOTE: // https://en.wikipedia.org/wiki/Prime_number_theorem#Approximations_for_the_nth_prime_number
	public static BigInteger nthPrimeApprox(int n) {
		// $\frac{p_{n}}{n}=\log n+\log\log n-1+\frac{\log\log n-2}{\log n}-\frac{(\log\log n)^{2}-6\log\log n+11}{2(\log n)^{2}}+o\left(\frac{1}{(\log n)^{2}}\right).$
		double logN = Math.log(n);
		double logLogN = Math.log(logN);
		double prime = n * (logN + logLogN - 1 + (logLogN - 2) / logN - (Math.pow(logLogN, 2) - 6 * logLogN + 11) / (2 * Math.pow(logN, 2)));
		return new BigInteger(String.valueOf((long) Math.ceil(prime)));
	}

	public static BigInteger nthPrimeTheorem(int n) {
		int upperBound = (int) Math.ceil(n * (Math.log(n) + Math.log(Math.log(n))));

		double prime = n * (Math.log(n) + Math.log(Math.log(n)));
		return new BigInteger(String.valueOf((long) Math.ceil(prime)));
	}

	public static String toOrdinal(int n) {
		if (n < 0) {
			throw new IllegalArgumentException("Number must be non-negative.");
		}

		String suffix;
		int lastTwoDigits = n % 100;

		if (11 <= lastTwoDigits && lastTwoDigits <= 13) {
			suffix = "th";
		} else {
			switch (n % 10) {
				case 1:
					suffix = "st";
					break;
				case 2:
					suffix = "nd";
					break;
				case 3:
					suffix = "rd";
					break;
				default:
					suffix = "th";
					break;
			}
		}

		return n + suffix;
	}
}