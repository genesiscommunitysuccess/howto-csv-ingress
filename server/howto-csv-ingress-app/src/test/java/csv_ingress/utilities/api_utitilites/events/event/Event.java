package csv_ingress.utilities.api_utitilites.events.event;

import csv_ingress.utilities.api_utitilites.event_base.EventBase;

import java.nio.file.Path;

public class Event extends EventBase {
    private static final Path path = Path.of(config_Properties.readProperty("payloadPath") + "/CsvIngress/");

    public Event(String endpoint, String fileName) {
        super(Path.of(path + fileName));
        super.setEndPoint(endpoint);
    }
}