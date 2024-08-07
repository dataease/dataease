package io.dataease.utils;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class LongArray2StringSerialize extends JsonSerializer<List<Long>> {

    @Override
    public void serialize(List<Long> longs, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        List<String> list = new ArrayList<>();
        for (Long str : longs) {
            if (str != null) {
                list.add(str.toString());
            }
        }
        jsonGenerator.writeObject(list);
    }
}
