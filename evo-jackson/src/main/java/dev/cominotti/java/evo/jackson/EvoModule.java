package dev.cominotti.java.evo.jackson;

import dev.cominotti.java.evo.CpfOrCnpj;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueDeserializer;
import tools.jackson.databind.ValueSerializer;
import tools.jackson.databind.module.SimpleModule;

@Component
public class EvoModule extends SimpleModule {

    public EvoModule() {
        super("EvoModule");

        setSerializerModifier(new SingleValueEvoSerializer());
        setDeserializerModifier(new SingleValueEvoDeserializer());

        addSerializer(CpfOrCnpj.class, new CpfOrCnpjSerializer());
        addDeserializer(CpfOrCnpj.class, new CpfOrCnpjDeserializer());
    }

    private static class CpfOrCnpjSerializer extends ValueSerializer<CpfOrCnpj> {

        @Override
        public void serialize(CpfOrCnpj value, JsonGenerator gen, SerializationContext ctxt)
                throws JacksonException {
            gen.writeString(value.value());
        }
    }

    private static class CpfOrCnpjDeserializer extends ValueDeserializer<CpfOrCnpj> {

        @Override
        public CpfOrCnpj deserialize(JsonParser p, DeserializationContext ctxt)
                throws JacksonException {
            String value = p.getValueAsString();
            try {
                return CpfOrCnpj.of(value);
            } catch (IllegalArgumentException e) {
                throw ctxt.weirdStringException(value, CpfOrCnpj.class, e.getMessage());
            }
        }
    }
}
