package de.isnow.sqlws.resources;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import java.io.IOException;
import javax.ws.rs.core.Link;

public class LinkSerializer extends JsonSerializer<Link>{

    @Override
    public void serialize(Link link, JsonGenerator jg, SerializerProvider sp)
            throws IOException {
        jg.writeStartObject();
        jg.writeStringField("rel", link.getRel());
        String lnk = link.getUri().toString();
        /* for @QueryParam, Jersey generates useless query
            strings (no param declaration) which we amputate here.
         */
        if (lnk.contains("?")) {
            lnk = lnk.split("\\?")[0];
        }
        jg.writeStringField("href", lnk);
        jg.writeStringField("title", link.getTitle());
        jg.writeStringField("type", link.getType());
        jg.writeEndObject();
    }
}