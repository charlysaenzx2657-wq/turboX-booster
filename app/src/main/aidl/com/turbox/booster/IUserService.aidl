// IUserService.aidl
package com.turbox.booster;

interface IUserService {
    void destroy() = 16777114;
    void exit() = 1;

    // Ejecutar comando shell con privilegios elevados
    String executeCommand(String command) = 2;

    // Optimizaciones del sistema
    void setAnimationScale(float scale) = 3;
    void killBackgroundProcesses(String packageName) = 4;
    void killAllBackgroundProcesses() = 5;
    void trimAllCaches() = 6;
    void setGpuRendering(boolean force) = 7;
    void setWindowAnimationScale(float scale) = 8;
    void setTransitionAnimationScale(float scale) = 9;
    void setAnimatorDurationScale(float scale) = 10;

    // Configuración de red
    void setDns(String dns1, String dns2) = 11;
    void setWifiOptimization(boolean enabled) = 12;

    // Game mode
    void setGameMode(String packageName, int mode) = 13;

    // Thermal
    void clearThermalThrottle() = 14;

    // Información del sistema (con acceso privilegiado)
    String getDetailedCpuInfo() = 15;
    String getDetailedMemInfo() = 16;
    String getRunningProcesses() = 17;

    // Performance mode
    void setPerformanceMode(int mode) = 18;

    // Background process limit
    void setBackgroundProcessLimit(int limit) = 19;

    // Disable/Enable packages
    void setPackageEnabled(String packageName, boolean enabled) = 20;
}
