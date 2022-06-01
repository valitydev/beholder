package dev.vality.beholder.converter;

import dev.vality.beholder.exception.BadFormatException;
import dev.vality.beholder.model.NetworkLog;
import dev.vality.beholder.model.NetworkMethod;
import org.json.JSONException;
import org.json.JSONObject;
import org.openqa.selenium.logging.LogEntry;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class LogEntriesToNetworkLogsConverter implements Converter<List<LogEntry>, List<NetworkLog>> {

    @Override
    public List<NetworkLog> convert(List<LogEntry> source) {
        Map<String, NetworkLog> networkLogs = new HashMap<>();
        for (LogEntry logEntry : source) {
            try {
                //Message format doc: https://chromedevtools.github.io/devtools-protocol/tot/Network/
                JSONObject log = new JSONObject(logEntry.getMessage());
                JSONObject message = log.getJSONObject("message");
                JSONObject params = message.getJSONObject("params");
                if (params.has("requestId")) {
                    String requestId = params.getString("requestId");
                    String method = message.getString("method");
                    if (NetworkMethod.REQUEST_WILL_BE_SENT.getValue().equals(method)) {
                        double time = params.getDouble("timestamp") * 1000;
                        String resource = params.getJSONObject("request").getString("url");
                        networkLogs.put(requestId, new NetworkLog(resource, time, null));
                    } else if (NetworkMethod.LOADING_FINISHED.getValue().equals(method)) {
                        double time = params.getDouble("timestamp") * 1000;
                        NetworkLog networkLog =
                                networkLogs.getOrDefault(requestId, new NetworkLog(requestId, null, null));
                        networkLog.setEnd(time);
                    }
                }
            } catch (JSONException e) {
                throw new BadFormatException("Error during parsing network logs:", e);
            }
        }
        return new ArrayList<>(networkLogs.values());
    }
}
