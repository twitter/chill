package com.twitter.chill.hadoop;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import org.apache.hadoop.conf.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class KryoFactory {
    final Logger LOG = LoggerFactory.getLogger(KryoFactory.class);
    final Configuration conf;

    public KryoFactory(Configuration conf) {
        this.conf = conf;
    }

    /**
     * KRYO_REGISTRATIONS holds a colon-separated list of classes to register with Kryo.
     * For example, the following value:
     *
     * "someClass,someSerializer:otherClass:thirdClass,thirdSerializer"
     *
     * will direct KryoFactory to register someClass and thirdClass with custom serializers
     * and otherClass with Kryo's FieldsSerializer.
     */
    public static final String KRYO_REGISTRATIONS = "chill.hadoop.registrations";

    /**
     * HIERARCHY_REGISTRATIONS holds a colon-separated list of classes or interfaces to register
     * with Kryo. Hierarchy Registrations are searched after basic registrations, and have the ability
     * to capture objects that are assignable from the hierarchy's superclass.
     * For example, the following value:
     *
     * "someClass,someSerializer:someInterface,otherSerializer"
     *
     * will configure chill.hadoop to serializeobjects that extend from someClass with someSerializer,
     * and objects that extend someInterface with otherSerializer.
     */
    public static final String HIERARCHY_REGISTRATIONS = "chill.hadoop.hierarchy.registrations";

    /**
     * If SKIP_MISSING is set to false, Kryo will throw an error when Cascading tries to register
     * a class or serialization that doesn't exist.
     */
    public static final String SKIP_MISSING = "chill.hadoop.skip.missing";

    /**
     * If ACCEPT_ALL is set to true, Kryo will try to serialize all java objects, not just those
     * with custom serializations registered.
     */
    public static final String ACCEPT_ALL = "chill.hadoop.accept.all";

    public static Serializer resolveSerializerInstance(com.esotericsoftware.kryo.Kryo k,
        Class superClass, Class<? extends Serializer> serializerClass) {
        try {
            try {
                return serializerClass.getConstructor(com.esotericsoftware.kryo.Kryo.class,
                    Class.class).newInstance(k, superClass);
            } catch (NoSuchMethodException ex1) {
                try {
                    return serializerClass.getConstructor(com.esotericsoftware.kryo.Kryo.class).newInstance(k);
                } catch (NoSuchMethodException ex2) {
                    try {
                        return serializerClass.getConstructor(Class.class).newInstance(superClass);
                    } catch (NoSuchMethodException ex3) {
                        return serializerClass.newInstance();
                    }
                }
            }
        } catch (Exception ex) {
            throw new IllegalArgumentException("Unable to create serializer \""
                                               + serializerClass.getName()
                                               + "\" for class: "
                                               + superClass.getName(), ex);
        }
    }

    public void registerHierarchies(Kryo k, Iterable<ClassPair> registrations) {
        for (ClassPair pair: registrations) {
            Class klass = pair.getSuperClass();
            Class<? extends Serializer> serializerClass = pair.getSerializerClass();

            if(serializerClass == null)
                throw new RuntimeException("Serializations are required for Heirarchy registration.");

            k.addDefaultSerializer(klass, resolveSerializerInstance(k, klass, serializerClass));
        }
    }

    public void registerBasic(Kryo k, Iterable<ClassPair> registrations) {
        for (ClassPair pair: registrations) {
            Class klass = pair.getSuperClass();
            Class<? extends Serializer> serializerClass = pair.getSerializerClass();

            if (serializerClass == null) {
                k.register(klass);
            } else {
                k.register(klass, resolveSerializerInstance(k, klass, serializerClass));
            }
        }
    }

    public void populateKryo(Kryo k) {
        k.setRegistrationRequired(!getAcceptAll());
        registerHierarchies(k, getHierarchyRegistrations());
        registerBasic(k, getRegistrations());
    }

    public static class ClassPair {
        final Class superClass;
        final Class<? extends Serializer> serializerClass;

        public ClassPair(Class superClass) {
            this(superClass, null);
        }

        public ClassPair(Class superClass, Class<? extends Serializer> serializerClass) {
            this.superClass = superClass;
            this.serializerClass = serializerClass;
        }

        public Class getSuperClass() {
            return superClass;
        }

        public Class<? extends Serializer> getSerializerClass() {
            return serializerClass;
        }

        @Override public String toString() {
            String ret = superClass.getName();
            if(serializerClass != null)
                ret = ret + "," + serializerClass.getName();
            return ret;
        }
    }

    public String classPairString(Iterable<ClassPair> pairs) {
        StringBuilder builder = new StringBuilder();
        boolean isFirst = true;
        for (ClassPair pair: pairs) {
            if (!isFirst)
                builder.append(":");
            isFirst = false;
            builder.append(pair.toString());
        }
        return builder.toString();
    }

    public Iterable<ClassPair> buildPairs(String base) {
        List<ClassPair> builder = new ArrayList<ClassPair>();

        if (base == null)
            return builder;

        for (String s: base.split(":")) {
            String[] pair = s.split(",");
            try {
                switch (pair.length) {
                    case 1:
                        builder.add(new ClassPair(Class.forName(pair[0])));
                        break;
                    case 2:
                        @SuppressWarnings("unchecked")
                        Class<? extends Serializer> serializerClass = (Class<? extends Serializer>) Class.forName(pair[1]);
                        builder.add(new ClassPair(Class.forName(pair[0]), serializerClass));
                        break;
                    default:
                        throw new RuntimeException(base + " is not well-formed.");
                }
            } catch (ClassNotFoundException e) {
                if (getSkipMissing()) {
                    LOG.info("Could not find serialization or class for " + pair[1]
                             + ". Skipping registration.");
                } else {
                    throw new RuntimeException(e);
                }
            }
        }
        return builder;
    }

    // Configuration Options

    public boolean getSkipMissing() {
        return conf.getBoolean(SKIP_MISSING, false);
    }

    public KryoFactory setSkipMissing(boolean optional) {
        conf.setBoolean(SKIP_MISSING, optional);
        return this;
    }

    public boolean getAcceptAll() {
        return conf.getBoolean(ACCEPT_ALL, true);
    }

    public KryoFactory setAcceptAll(boolean acceptAll) {
        conf.setBoolean(ACCEPT_ALL, acceptAll);
        return this;
    }

    public Iterable<ClassPair> getRegistrations() {
        String serializations = conf.get(KRYO_REGISTRATIONS);
        return buildPairs(serializations);
    }

    public KryoFactory setRegistrations(Iterable<ClassPair> registrations) {
        conf.set(KRYO_REGISTRATIONS, classPairString(registrations));
        return this;
    }

    public Iterable<ClassPair> getHierarchyRegistrations() {
        String hierarchies = conf.get(HIERARCHY_REGISTRATIONS);
        return buildPairs(hierarchies);
    }

    public KryoFactory setHierarchyRegistrations(Iterable<ClassPair> registrations) {
        conf.set(HIERARCHY_REGISTRATIONS, classPairString(registrations));
        return this;
    }
}
