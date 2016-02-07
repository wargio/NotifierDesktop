/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package deroad.phone.notifier;

import java.util.Date;
import java.util.Random;

public class ClientInfo {

    private static Random rnd = null;
    private String ip = null;
    private String hostname = null;
    private int access;
    private long timestamp = 0;

    public ClientInfo(String ip, String hostname) {
        if (rnd == null) {
            rnd = new Random();
        }
        this.ip = ip;
        this.hostname = hostname;
        this.access = rnd.nextInt();
        this.timestamp = new Date().getTime();
    }

    public void setAccess() {
        this.access = rnd.nextInt();
    }

    public int getAccess() {
        return access;
    }

    public String getIp() {
        return ip;
    }

    public String getHostname() {
        return hostname;
    }

    public long getTimestamp() {
        return timestamp;
    }
    
}
