package csv_ingress.utilities.api_utitilites.events.authentication;

import csv_ingress.utilities.api_utitilites.event_base.EventBase;
import csv_ingress.utilities.config_utilities.ConfigReader;

import java.nio.file.Path;

import static csv_ingress.utilities.json_utilities.JsonModifier.modifyJSON;

public class EventLoginAuth extends EventBase {
    private static final Path path = Path.of(config_Properties.readProperty("payloadPath") + "/CsvIngress/P_login.json");

    public EventLoginAuth(String username, String password) {
        super(path);
        modifyJSON(path, "USER_NAME", username);
        modifyJSON(path, "PASSWORD", password);
        super.setEndPoint("EVENT_LOGIN_AUTH");
    }
}