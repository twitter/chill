package com.twitter.chill.avro;

import avro.FiscalRecord;
import com.esotericsoftware.kryo.kryo5.Kryo;
import com.esotericsoftware.kryo.kryo5.Serializer;
import com.esotericsoftware.kryo.kryo5.objenesis.strategy.StdInstantiatorStrategy;
import com.twitter.chill.KryoInstantiator;
import com.twitter.chill.KryoPool;
import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecordBuilder;
import org.junit.Before;
import org.junit.Test;
import scala.reflect.ClassTag;

import static org.junit.Assert.assertEquals;

public class AvroSerializerJavaTest {

    private Schema schema;
    private GenericData.Record user;
    private  FiscalRecord fiscalRecord;

    @Before
    public void setUp() throws Exception {

        schema = SchemaBuilder
                .record("person")
                .fields()
                .name("name").type().stringType().noDefault()
                .name("ID").type().intType().noDefault()
                .endRecord();


        user = new GenericRecordBuilder(schema)
                .set("name", "Jeff")
                .set("ID", 1)
                .build();


        fiscalRecord = FiscalRecord.newBuilder().setCalendarDate("2012-01-01").setFiscalWeek(1).setFiscalYear(2012).build();

    }

    public <T> KryoPool getKryo(final ClassTag<T> tag, final Serializer<T> serializer){
        KryoInstantiator kryoInstantiator = new KryoInstantiator() {
            public Kryo newKryo() {
                Kryo k =super.newKryo();
                k.setInstantiatorStrategy(new StdInstantiatorStrategy());
                k.register(tag.runtimeClass(), serializer);
                return k;
            }
        };

       return KryoPool.withByteArrayOutputStream(1, kryoInstantiator);
    }
    @Test
    public void testSpecificRecordSerializer() throws Exception {
        ClassTag<FiscalRecord> tag = getClassTag(FiscalRecord.class);
        KryoPool kryo = getKryo(tag, AvroSerializer$.MODULE$.SpecificRecordSerializer(tag));
        byte[] bytes = kryo.toBytesWithClass(fiscalRecord);
        FiscalRecord result = (FiscalRecord) kryo.fromBytes(bytes);
        assertEquals(fiscalRecord,result);
    }

    @Test
    public void SpecificRecordBinarySerializer() throws Exception {
        ClassTag<FiscalRecord> tag = getClassTag(FiscalRecord.class);
        KryoPool kryo = getKryo(tag, AvroSerializer$.MODULE$.SpecificRecordBinarySerializer(tag));
        byte[] bytes = kryo.toBytesWithClass(fiscalRecord);
        FiscalRecord result = (FiscalRecord) kryo.fromBytes(bytes);
        assertEquals(fiscalRecord,result);
    }


    @Test
    public void testGenericRecord() throws Exception {
        ClassTag<GenericData.Record> tag = getClassTag(GenericData.Record.class);
        KryoPool kryo = getKryo(tag, AvroSerializer$.MODULE$.GenericRecordSerializer(schema,tag));
        byte[] userBytes = kryo.toBytesWithClass(user);
        GenericData.Record userResult = (GenericData.Record) kryo.fromBytes(userBytes);
        assertEquals(userResult.get("name").toString(),"Jeff");
        assertEquals(userResult.get("ID"),1);
        assertEquals(user.toString(), userResult.toString());

    }

    private <T> ClassTag<T> getClassTag(Class<T> klass) {
        return scala.reflect.ClassTag$.MODULE$.apply(klass);
    }
}