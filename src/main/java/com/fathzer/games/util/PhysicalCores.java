package com.fathzer.games.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Static utility class for finding the number of physical CPU cores.
 * <br>It was greatly inspired by <a href="https://github.com/veddan/java-physical-cores">veddan's contribution</a>
 */
public final class PhysicalCores {
	private static final NumberOfCores CORES = findPhysicalCoreCount();

	private PhysicalCores() {
		super();
		// Just to prevent instantiation
	}

	/**
     * Returns the number of "physical" hardware cores available.
     * <p>On a machine with hyper threading, this value may be less than the number
     * reported by {@link Runtime#availableProcessors()}.
     * If you are running on a virtual machine, the value returned will be the
     * number of cores assigned to the VM, not the actual number of physical
     * cores on the machine.
     * Likewise, if the number of cores available to this process is less than the
     * installed number, the available number will be returned.
     * </p>
     * <p>
     * The method is threadsafe.
     * </p>
     * @return number of physical cores
 	 * @throws UnreachableException if it could not be determined
     */
    public static int count() {
    	if (CORES.e!=null) {
    		throw CORES.e;
    	}
        return CORES.count;
    }
    
    public static class UnreachableException extends RuntimeException {
		private static final long serialVersionUID = 1L;

		public UnreachableException(String message, Throwable cause) {
			super(message, cause);
		}

		public UnreachableException(String message) {
			super(message);
		}
    }

    private static class NumberOfCores {
    	private final UnreachableException e;
        private final int count;
        
		private NumberOfCores(int count) {
			this.e = null;
			this.count = count;
		}

		private NumberOfCores(String message) {
			this(new UnreachableException(message));
		}

		private NumberOfCores(UnreachableException e) {
			this.e = e;
			this.count = -1;
		}
    }

    private static NumberOfCores findPhysicalCoreCount() {
    	String osName;
        try {
        	osName = System.getProperty("os.name");
        } catch (SecurityException e) {
            return new NumberOfCores(new UnreachableException("Could not read system property 'os.name'",e));
        }
        try {
	        if (osName == null) {
	            throw new UnsupportedOperationException("Failed to read OS name.");
	        } else if (isLinux(osName)) {
	            return readFromProc();
	        } else if (isWindows(osName)) {
	            return readFromWMIC();
	        } else if (isMacOsX(osName)) {
	            return readFromSysctlOsX();
	        } else if (isFreeBsd(osName)) {
	            return readFromSysctlFreeBSD();
	        } else {
	        	return new NumberOfCores(String.format("Unknown OS '%s'. Please report this so a case can be added.", osName));
	        }
        } catch (UnreachableException e) {
        	return new NumberOfCores(e);
        }
    }

    @SuppressWarnings("java:S1075")
    private static NumberOfCores readFromProc() {
        final String path = "/proc/cpuinfo";
        File cpuinfo = new File(path);
        if (!cpuinfo.exists()) {
            return new NumberOfCores(String.format("Old Linux without %s. Will not be able to provide core count.", path));
        }
        try (InputStream in = new FileInputStream(cpuinfo)) {
            String s = readToString(in, StandardCharsets.UTF_8);
            // Count number of different tuples (physical id, core id) to discard hyper threading and multiple sockets  
            Map<String, Set<String>> physicalIdToCoreId = new HashMap<>();

            int coreIdCount = 0;
            String[] split = s.split("\n");
            String latestPhysicalId = null;
            for (String row : split)
                if (row.startsWith("physical id")) {
                    latestPhysicalId = row;
                    if (physicalIdToCoreId.get(row) == null)
                        physicalIdToCoreId.put(latestPhysicalId, new HashSet<>());

                } else if (row.startsWith("core id"))
                    // "physical id" row should always come before "core id" row, so that physicalIdToCoreId should
                    // not be null here.
                    physicalIdToCoreId.get(latestPhysicalId).add(row);

            for (Set<String> coreIds : physicalIdToCoreId.values())
                coreIdCount += coreIds.size();

            return new NumberOfCores(coreIdCount);
        } catch (SecurityException | IOException e) {
            return new NumberOfCores(String.format("Error %s while reading %s", e, path));
        }
    }

    private static NumberOfCores readFromWMIC() {
    	String wmic = System.getenv("windir")+"\\system32\\wbem\\WMIC";
        ProcessBuilder pb = new ProcessBuilder(wmic, "/OUTPUT:STDOUT", "CPU", "Get", "/Format:List");
        pb.redirectErrorStream(true);
        Process wmicProc;
        try {
            wmicProc = pb.start();
            wmicProc.getOutputStream().close();
        } catch (IOException | SecurityException e) {
            return new NumberOfCores(String.format("Failed to spawn WMIC process. " +
                      "Will not be able to provide physical core count: %s", e));
        }
        waitFor(wmicProc);
        try (InputStream in = wmicProc.getInputStream()) {
            String wmicOutput = readToString(in, StandardCharsets.US_ASCII);
            return new NumberOfCores(parseWmicOutput(wmicOutput));
        } catch (UnsupportedEncodingException e) {
            // Java implementations are required to support US-ASCII, so this shouldn't happen
            throw new RuntimeException(e);
        } catch (SecurityException | IOException e) {
            return new NumberOfCores(String.format("Error while reading WMIC output file: %s", e));
        }
    }

    private static Integer parseWmicOutput(String wmicOutput) {
        String[] rows = wmicOutput.split("\n");
        int coreCount = 0;
        for (String row : rows) {
            if (row.startsWith("NumberOfCores")) {
                String num = row.split("=")[1].trim();
                try {
                    coreCount += Integer.parseInt(num);
                } catch (NumberFormatException e) {
                    throw new NumberFormatException(String.format("Unexpected output from WMIC: \"{}\". " +
                              "Will not be able to provide physical core count.", wmicOutput));
                }
            }
        }
        return coreCount > 0 ? coreCount : null;
    }

    private static NumberOfCores readFromSysctlOsX() {
        String result = readSysctl("hw.physicalcpu", "-n");
        if (result == null) {
            return new NumberOfCores("sysctl hw.physicalcpu -n returned no data");
        }
        try {
            return new NumberOfCores(Integer.parseInt(result));
        } catch (NumberFormatException e) {
            return new NumberOfCores(String.format("sysctl returned something that was not a number: '%s'", result));
        }
    }

    private static NumberOfCores readFromSysctlFreeBSD() {
        String result = readSysctl("dev.cpu");
        if (result == null) {
            return null;
        }
        Set<String> cpuLocations = new HashSet<>();
        for (String row : result.split("\n")) {
            if (row.contains("location")) {
                cpuLocations.add(row.split("\\\\")[1]);
            }
        }
        return cpuLocations.isEmpty() ? new NumberOfCores("sysctl dev.cpu returns no cores") : new NumberOfCores(cpuLocations.size());
    }

    private static String readSysctl(String variable, String... options) {
        List<String> command = new ArrayList<>();
        command.add("sysctl");
        command.addAll(Arrays.asList(options));
        command.add(variable);
        ProcessBuilder pb = new ProcessBuilder(command);
        pb.redirectErrorStream(true);
        Process sysctlProc;
        try {
            sysctlProc = pb.start();
        } catch (IOException | SecurityException e) {
            throw new UnreachableException("Failed to spawn sysctl process. " +
                      "Will not be able to provide physical core count.", e);
        }
        String result;
        try {
            result = readToString(sysctlProc.getInputStream(), StandardCharsets.UTF_8).trim();
        } catch (UnsupportedEncodingException e) {
            // Java implementations are required to support UTF-8, so this can't happen
            throw new RuntimeException(e);
        } catch (IOException e) {
        	throw new UnreachableException("Error while reading from sysctl process", e);
        }
        int exitStatus = waitFor(sysctlProc);
        if (exitStatus != 0) {
        	throw new UnreachableException(String.format("Could not read sysctl variable %s. Exit status was %s", variable, exitStatus));
        }
        return result;
    }

    private static boolean isLinux(String osName) {
        return osName.startsWith("Linux") || osName.startsWith("LINUX");
    }

    private static boolean isWindows(String osName) {
        return osName.startsWith("Windows");
    }

    private static boolean isMacOsX(String osName) {
        return osName.startsWith("Mac OS X");
    }

    private static boolean isFreeBsd(String osName) {
        return osName.startsWith("FreeBSD");
    }

    private static String readToString(InputStream in, Charset charset) throws IOException {
        try (InputStreamReader reader = new InputStreamReader(in , charset)) {
            StringWriter sw = new StringWriter();
            char[] buf = new char[10000];
            while (reader.read(buf) != -1) {
                sw.write(buf);
            }
            return sw.toString();
        }
    }

    private static int waitFor(Process proc) {
        try {
            return proc.waitFor();
        } catch (InterruptedException e) {
        	Thread.currentThread().interrupt();
            throw new UnreachableException("Interrupted while waiting for process", e);
        }
    }
}