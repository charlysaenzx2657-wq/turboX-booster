// IShizukuService.aidl
package com.optimizer.pro;

interface IShizukuService {
    /**
     * Ejecuta un comando shell y retorna el output
     */
    String runCommand(String command);

    /**
     * Ejecuta múltiples comandos y retorna resultados
     */
    String runCommands(in String[] commands);

    /**
     * Limpia caché de una app específica
     */
    boolean clearAppCache(String packageName);

    /**
     * Mata un proceso por nombre de paquete
     */
    boolean killProcess(String packageName);

    /**
     * Retorna true si el servicio está activo
     */
    boolean isAlive();

    /**
     * Destruye el servicio
     */
    void destroy();
}
