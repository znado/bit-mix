package cs2951e;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONString;

// simple IP/port struct
public class MixerNetworkAddress implements JSONString {
    private String ipAddress;
    private int port;

    public MixerNetworkAddress(String ipAddress, int port) {
        this.ipAddress = ipAddress;
        this.port = port;
    }

    public MixerNetworkAddress(JSONObject json) throws JSONException {
        this.ipAddress = json.getString("ip");
        this.port = json.getInt("port");
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    @Override
    public String toJSONString() {
        return "{'port' : " + port + ", 'ip' : '" + ipAddress + "'}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MixerNetworkAddress that = (MixerNetworkAddress) o;
        if (port != that.port) return false;
        if (!ipAddress.equals(that.ipAddress)) return false;
        return true;
    }

    @Override
    public int hashCode() {
        int result = ipAddress.hashCode();
        result = 31 * result + port;
        return result;
    }

    @Override
    public String toString() {
        return toJSONString();
    }
}
