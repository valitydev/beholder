package dev.vality.beholder.testutil;

import lombok.experimental.UtilityClass;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.HardwareAbstractionLayer;

@UtilityClass
public class SystemUtil {

    private static final String ARM = "ARM";

    public static boolean isArmArchitecture() {
        SystemInfo si = new SystemInfo();
        HardwareAbstractionLayer hal = si.getHardware();
        CentralProcessor cpu = hal.getProcessor();
        var cpuId = cpu.getProcessorIdentifier();
        return cpuId.getVendor().toUpperCase().contains(ARM)
                || cpuId.getMicroarchitecture().toUpperCase().contains(ARM);
    }
}
